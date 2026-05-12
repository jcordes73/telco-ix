# Syslog Forwarder

The Syslog Forwarder consumes Syslog messages via TCP and puts them on an Apache Kafka topic.

## Development

During development, start the additional external services via ``podman compose``
```bash
podman compose -f src/test/resources/compose/compose.yaml up -d
```

Now run ``quarkus dev`` to start Quarkus in development mode 

Tests can also be done in ``dev`` mode with the Camel CLI:
```bash
camel cmd send --uri="netty://tcp://localhost:1514?useByteBuf=true" --body=file:src/test/resources/syslog/test-rfc5424.txt
```

## Test

As in development During development, start additional external services via ``podman compose``
```bash
podman compose -f src/test/resources/compose/compose.yaml up
```
Then to execute unit tests run ``quarkus test``.

## Production

First you should adapt the configuration settings in **src/main/resources/application.properties** for your environment.

Now you are ready to create the production build
```bash
quarkus build
```
or without executing tests
```bash
quarkus build -DskipTests
```

To run the application in production mode use
```bash
java -jar target/quarkus-app/quarkus-run.jar
```
Prometheus compatible metrics are exposed at http://localhost:8080/q/metrics