package com.redhat.integration.kafka.httpsink;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.dsl.MockEndpointBuilderFactory.MockEndpointBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(KafkaHttpSinkConnectorBasicAuthTest.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaHttpSinkConnectorBasicAuthTest extends CamelQuarkusTestSupport {

    @Inject
    Logger log;

    @Inject
    ProducerTemplate producerTemplate;

    @ConfigProperty(name="kafka.topic.incoming", defaultValue = "test")
    String topicName;

    @ConfigProperty(name="kafka.topic.dlq", defaultValue = "dlq")
    String dlqName;

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("camel.main.autoStartup", "false");
    }

    @Override
    public String getConfigProfile() { 
        return "test-basic-auth"; 
    }

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    @BeforeAll
    public void beforeAll() throws Exception {
        AdviceWith.adviceWith(context ,"kafka-http-sink", r -> r.weaveAddLast().to("mock:httpSink"));

        RouteBuilder kafkaProducer = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:kafkaProducer").id("producer")
                .setExchangePattern(ExchangePattern.InOnly)
                .to("kafka:" + topicName);
            }
            
        };

        RouteBuilder dqlConsumer = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("kafka:" + dlqName).id("dlq")
                .log(LoggingLevel.INFO, "Message ended in DLQ")
                .to("mock:dlq");
            }
            
        };

        context.addRoutes(kafkaProducer);
        context.addRoutes(dqlConsumer);

        context.getRouteController().startAllRoutes();
    }

    @SuppressWarnings("null")
    @Test
    @Order(1)
    void testSuccess() throws Exception {        
        MockEndpoint httpSink = getMockEndpoint("mock:httpSink");
        httpSink.expectedMessageCount(1);

        producerTemplate.sendBody("direct:kafkaProducer", "{\"subscribe\":\"netflix\"}");

        httpSink.assertIsSatisfied();        
    }

    /*
    @Test
    @Order(2)
    void testDlq() throws Exception {
        MockEndpoint dlq = getMockEndpoint("mock:dlq");
        dlq.expectedMessageCount(1);

        producerTemplate.sendBody("direct:kafkaProducer", "{\"subscribe\":\"hbo\"}");

        dlq.assertIsSatisfied();
    }*/
}