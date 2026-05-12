package com.redhat.integration.kafka.httpsink;

import java.net.URI;
import java.util.Base64;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.runtime.TokensHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@ApplicationScoped
public class KafkaHttpSinkConnectorRouteBuilder extends RouteBuilder {

    @Inject
    CamelContext camelContext;

    @Inject
    Provider<OidcClient> oidcClient;
    TokensHelper tokenHelper = new TokensHelper();

    @ConfigProperty(name="kafka.topic.incoming", defaultValue = "test")
    String topicName;

    @ConfigProperty(name="kafka.topic.dlq", defaultValue = "dlq")
    String dlqName;

    @ConfigProperty(name="http.url", defaultValue = "http://localhost:8280")
    String httpUrl;

    @ConfigProperty(name="http.method", defaultValue = "POST")
    String httpMethod;

    @ConfigProperty(name="http.secure", defaultValue = "false")
    String httpSecure;

    @ConfigProperty(name="http.content-type", defaultValue = "application/json")
    String httpContentType;

    @ConfigProperty(name="http.auth")
    String httpAuth;

    @ConfigProperty(name="http.auth.basic.username", defaultValue = "test")
    String httpAuthBasicUserName;

    @ConfigProperty(name="http.auth.basic.password", defaultValue = "test")
    String httpAuthBasicPassword;

    @Override
    public void configure() throws Exception {

        URI uri = new URI(httpUrl);

        boolean httpSecure = uri.getScheme().equals("https");
        String httpHost = uri.getHost();
        int httpPort = 80;
        if (uri.getPort() == -1) {
            httpPort = (httpSecure) ? 443 : 80;
        } else {
            httpPort = uri.getPort();
        }
        String httpPath = uri.getPath();

        String basicAuthCredentials = Base64.getEncoder().encodeToString((httpAuthBasicUserName + ":" + httpAuthBasicPassword).getBytes());
        
        onException(Exception.class)
        .redeliveryDelay(1000)
        .useExponentialBackOff()
        .maximumRedeliveries(3)
        .to("kafka:" + dlqName).handled(true);
        
        from("kafka:" + topicName )
        .routeId("kafka-http-sink")
        .log(LoggingLevel.INFO, "Received message")
        .removeHeaders("*")
        .choice()
          .when(constant(httpAuth).isEqualTo("oidc"))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    String accessToken = tokenHelper.getTokens(oidcClient.get()).onItem().transform(tokens -> tokens.getAccessToken()).await().indefinitely();

                    exchange.getIn().setHeader("Authorization", "Bearer " + accessToken);
                }
            })
            .when(constant(httpAuth).isEqualTo("basic"))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    exchange.getIn().setHeader("Authorization", "Basic " + basicAuthCredentials);
                }
            })
        .end()
        .setHeader(Exchange.HTTP_PATH).constant(httpPath)
        .setHeader(Exchange.HTTP_METHOD).constant(httpMethod)
        .setHeader("Content-Type").constant(httpContentType)
        .choice()
            .when(constant(httpSecure).isEqualTo(true))
                .to("https:"+httpHost+":"+httpPort)  
            .otherwise()
                .to("http:"+httpHost+":"+httpPort)
        .end();
    }
}