package com.redhat.integration.syslog.forwarder;

import java.io.File;
import java.util.Map;

import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.apache.commons.io.FileUtils;
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

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SyslogForwarderTest extends CamelQuarkusTestSupport {

    @Inject
    Logger log;

    @Inject
    ProducerTemplate producerTemplate;

    @ConfigProperty(name="kafka.topic", defaultValue = "test")
    String topicName;

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("camel.main.autoStartup", "false");
    }

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    @BeforeAll
    public void beforeAll() throws Exception {

        RouteBuilder kafkaConsumer = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("kafka:" + topicName).id("consumer")
                .log(LoggingLevel.INFO, "Message reached consumer")
                .to("mock:consumer");
            }
            
        };

        context.addRoutes(kafkaConsumer);

        context.getRouteController().startAllRoutes();
    }

    @SuppressWarnings("null")
    @Test
    @Order(1)
    void testSendingSyslogMessage() throws Exception {        
        MockEndpoint consumer = getMockEndpoint("mock:consumer");
        consumer.expectedMessageCount(1);

        producerTemplate.sendBody("netty:tcp://localhost:1514", FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("syslog/test-rfc3164.txt").getFile()) 
        , "UTF-8"));

        consumer.assertIsSatisfied();        
    }
}