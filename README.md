# Telco Integration Experiments (telco-IX)

This repository contains a collection a various integrations typically found in the Telco space.

## Technology

The following technologies are (or will be used) for implementing integrations.

- Red Hat build of Apache Camel (https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel) with Quarkus (https://quarkus.io)
- Kaoto (https://kaoto.io)
- Streams for Apache Kafka (https://docs.redhat.com/en/documentation/red_hat_streams_for_apache_kafka)
- Red Hat build of Debezium (https://docs.redhat.com/en/documentation/red_hat_build_of_debezium)

## Use-Cases

Currently the following use-cases have been implemented

| Use-Case  | Description | Repository |
|-----------|-------------|------------|
|Kafka Sink Connector| Provides functionality similar to the Confluent Kafka Sink connector. | [Kafka Sink Connector](./kafka-sink-connector/)
|Syslog Forwarder|Consumes Syslog messages via TCP and puts them onto a Kafka topic| [Syslog Forwarder](./syslog-forwarder/)

# Prerequisites

- Java 21 (OpenJDK or similar)
- Quarkus CLI (https://quarkus.io/get-started/)
- Docker/Podman
- VS Code
- Kaoto (https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.14/html/kaoto_camel_designer/installing-kaoto)
- Camel CLI (https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.14/html/tooling_guide_for_red_hat_build_of_apache_camel/camel-cli-cq#installing-camel-jbang-cq)