package com.redhat.integration.kafka.httpsink;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.kafka.client.runtime.KafkaAdminClient;
import io.quarkus.kafka.client.runtime.KafkaCreateTopicRequest;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.config.SmallRyeConfig;

import org.eclipse.microprofile.config.ConfigProvider;

import org.jboss.logging.Logger;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ApplicationScoped
@IfBuildProfile(anyOf = {"dev","test","test-basic-auth","test-oidc-auth"})
public class AppLifecycleBean {

    @Inject
    KafkaAdminClient kafkaAdminClient;

    private static final Logger log = Logger.getLogger(AppLifecycleBean.class);

    private SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);

    void onStart(@Observes StartupEvent ev) throws Exception {
        /*kafkaAdminClient
            .createTopic(
                new KafkaCreateTopicRequest(
                    config.getValue(
                        "kafka.topic.dlq", String.class), 
                        Integer.valueOf(1), 
                        Short.valueOf("1"), 
                        null));

        kafkaAdminClient
            .createTopic(
                new KafkaCreateTopicRequest(
                    config.getValue(
                        "kafka.topic.incoming",
                        String.class),
                        Integer.valueOf(1),
                        Short.valueOf("1"),
                        null));

        log.info("Kafka-Topics: " + kafkaAdminClient.getTopics());*/
        
        configureFor(config.getValue("quarkus.wiremock.devservices.port", Integer.class));
        
        removeAllMappings();

        stubFor(
            post("/test")
            .withHeader("Content-Type", equalToIgnoreCase("application/json"))
            .withBasicAuth("test", "test")
            .withHeader("Authorization", matching("(Bearer|Basic) .*"))
            .withRequestBody(containing("netflix"))
            .willReturn(okJson("{\"status\":\"success\"}"))).shouldBePersisted();
        
        stubFor(
            post("/test")
            .withHeader("Content-Type", equalToIgnoreCase("application/json"))
            .withBasicAuth("test", "test")
            .withRequestBody(containing("hbo"))
            .willReturn(jsonResponse("{\"status\":\"error\"}", 422))).shouldBePersisted();

        stubFor(
            post("/test")
            .withHeader("Content-Type", equalToIgnoreCase("application/json"))
            .withHeader("Authorization", matching("Bearer .*"))
            .withRequestBody(containing("netflix"))
            .willReturn(okJson("{\"status\":\"success\"}"))).shouldBePersisted();
        
        stubFor(
            post("/test")
            .withHeader("Content-Type", equalToIgnoreCase("application/json"))
            .withHeader("Authorization", matching("Bearer .*"))
            .withRequestBody(containing("hbo"))
            .willReturn(jsonResponse("{\"status\":\"error\"}", 422))).shouldBePersisted();

        log.info("Mocks: " + WireMock.listAllStubMappings());
    }
        
    void onStop(@Observes ShutdownEvent ev) {               
        log.error("The application is stopping...");
    }
}