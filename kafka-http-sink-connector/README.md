# Kafka HTTP Sink Connector

The Kafka HTTP Sink Connector consumes messages from a Kafka topic and sends the payload to an HTTP endpoint.

It works very similar to the [Confluent HTTP Sink Connecttor](https://docs.confluent.io/kafka-connectors/http/current/overview.html)

## Overview

This implementation uses the [Quarkus](https://quarkus.io/) framework for implementing cloud-native applications in Java and the [Apache Camel](https://camel.apache.org/) framework for cloud-native integration.

## Prerequisites

- Java 17 or 21
- Docker or Podman
- OpenShift
- Kafka-Cluster (f.e. [Red Hat Streams for Apache Kafka](https://access.redhat.com/products/streams-apache-kafka))
- Quarkus CLI (see https://quarkus.io/guides/cli-tooling)

## Configuration
The main source of configuration is **src/main/resources/application.properties**.

For Kubernetes the configuration can be externalized in a **Secret** named **kafka-http-sink-connector-config**:

|Property|Description|Values|Default|
|--------|-----------|------|-------|
|HTTP_URL|The URL of the HTTP endpoint|Text|http://localhost/|
|HTTP_METHOD|The HTTP method to use when calling the HTTP endpoint|POST,GET,PUT|POST|
|HTTP_CONTENT_TYPE|The content-type of the payload|text,application/json|application/json|
|http.auth|The authentication method to use for the HTTP endpoint|none,basic,oidc|none|
|HTTP_AUTH_BASIC_USERNAME|The Basic authentication username|Text|test|
|HTTP_AUTH_BASIC_PASSWORD|The Basic authentication password|Text|test|
|HTTP_AUTH_OIDC|If OIDC should be enabled when authenticating with the HTTP endpoint|true,false|false| 
|HTTP_AUTH_OIDC_SERVER_URL|The Authentication Server URL, f.e. *https://keycloak/realm/realm-name* for Keycloak| URL |-| 
|HTTP_AUTH_OIDC_CLIENT_ID|The OIDC Client ID | Text | - |
|HTTP_AUTH_OIDC_CLIENT_SECRET|The OIDC Client Secret | Text | - |
|KAFKA_BROKERS|The Kafka Broker initial URL|URL|-|
|KAKFA_USERNAME|The Kafka username|Text|kafka-admin|
|KAFKA_PASSWORD|The Kafka password|Text|openshift|
|KAFKA_TOPIC_INCOMING|The Kafka topic to consume messages from|Text|test|
|KAFKA_TOPIC_DLQ|The Kafka Topic to use for DLQ messages|Text|dlq|

For the configuration there are currently these limitations:

- The only authentication mechansim supported for Apache Kafka is SASL/PLAINTEXT
- There is no validation of certificates and hostnames

## Development

During development, start the additional external services via ``podman compose``
```bash
podman compose -f src/test/resources/compose/compose.yaml up
```

Now run ``quarkus dev`` to start Quarkus in development mode 

Tests can also be done in ``dev`` mode, for this purpose open two terminals.

- In **terminal 1** send a message to the Kafka topic **test**
  ```bash
  camel cmd send --uri="kafka:test?brokers=localhost:9092" --body="{\"subscription\":\"netflix\"}"
  ```
  This test shouĺd be successful.
- In **terminal 2** use the following command to receive messages from the dead-letter-queue **dlq**
  ```bash
  camel cmd receive --uri="kafka:dlq?brokers=localhost:9092"
  ```
- In the **terminal 1** send a message that should cause a failure to the Kafka topic **test**
  ```bash
  camel cmd send --uri="kafka:test?brokers=localhost:9092" --body="{\"subscription\":\"hbo\"}"
  ```
  In **terminal 2** you should now have received a message with the content of the second message send to the **test** topic.

## Test

As in development During development, start additional external services via ``podman compose``
```bash
podman compose -f src/test/resources/compose/compose.yaml up -d
```
Then to execute unit tests run ``quarkus test``.

To shutdown the services run
```bash
podman compose -f src/test/resources/compose/compose.yaml down
```

## Build

```bash
quarkus build
```
without executing tests
```bash
quarkus build -DskipTests
```

## Deploy

> **Hint**:
> Deploying the Kafka HTTP Sink Connector requires you to run ```quarkus build``` first.

To directly deploy to Red Hat OpenShift Container Platform just run

```bash
quarkus deploy
```

In case you want to build a container image and then use a  **deployment** resource on Kubernetes, this can also be done:

First build the container image on OpenShift
```bash
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.openshift.namespace=kafka-http-sink-connector -DskipTests
```
The use the OpenShift client (oc) to create a **Deployment**
```bash
oc create -f src/main/resources/openshift/deployment.yaml
```
To launch other instances of the **Kafka HTTP Sink Connector** create a secret with the new settings, another **deployment.yaml**, exchanging the **name** and the **app** label. Don't forget to change the reference to the secret you created just now.