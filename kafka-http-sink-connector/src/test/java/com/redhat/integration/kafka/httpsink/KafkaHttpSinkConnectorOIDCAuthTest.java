package com.redhat.integration.kafka.httpsink;

import java.util.concurrent.TimeUnit;

import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
@TestProfile(KafkaHttpSinkConnectorOIDCAuthTest.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaHttpSinkConnectorOIDCAuthTest extends CamelQuarkusTestSupport {

    @Inject
    ProducerTemplate producerTemplate;


    @ConfigProperty(name="kafka.topic.incoming", defaultValue = "test")
    String topicName;

    @ConfigProperty(name="kafka.topic.dlq", defaultValue = "dlq")
    String dlqName;

    @Override
    public String getConfigProfile() { 
        return "test-oidc-auth"; 
    }

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    /*

    @BeforeAll
    public void beforeAll() throws Exception {
        context.getRouteController().stopRoute("kafka-http-sink", 10, TimeUnit.SECONDS);

        AdviceWith.adviceWith(context ,"kafka-http-sink", r -> r.weaveAddLast().to("mock:httpSinkResult"));

        RouteBuilder kafkaProducer = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:kafkaProducer").id("producer")
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

        context.getRouteController().startRoute("kafka-http-sink");
    }

    @SuppressWarnings("null")
    @Test
    @Order(1)
    void testSuccess() throws Exception {
        MockEndpoint httpSink = getMockEndpoint("mock:httpSinkResult");
        httpSink.expectedMessageCount(1);

        producerTemplate.sendBody("direct:kafkaProducer", "{\"subscribe\":\"netflix\"}");

        httpSink.assertIsSatisfied();
    }

    @Test
    @Order(2)
    void testDlq() throws Exception {
        MockEndpoint dlq = getMockEndpoint("mock:dlq");
        dlq.expectedMessageCount(1);

        producerTemplate.sendBody("direct:kafkaProducer", "{\"subscribe\":\"hbo\"}");

        dlq.assertIsSatisfied();
    }*/
}