# TransitServiceApp

Calculates an ETA using hub information from `hub-service` and the latest delay stage received
asynchronously from `package-status-topic`.

## Stage 3 architecture

Transit no longer calls `delay-stage-service` over HTTP. `DelayStageSubscriber` listens to the
ActiveMQ topic and keeps the latest stage per hub in memory. If the broker is temporarily down at
startup, the subscriber retries in the background.

## ETA model

The assignment does not prescribe a real route model, so this implementation uses:

```text
base ETA = 24 hours
delay = stage * 4 hours
estimated ETA = 24 + delay
```

## Build and run

Start the broker and `hub-service` first, then:

```bash
mvn test package
java -Xms32m -Xmx256m -jar target/transit-service.jar
```

Listens on port `7053`.

## Endpoints

```text
GET /health
GET /eta/{hubId}
```

Unknown hubs return HTTP 404. If hub-service is unavailable, ETA requests return HTTP 502.
