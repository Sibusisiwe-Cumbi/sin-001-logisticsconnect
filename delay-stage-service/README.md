# DelayStageServiceApp

Tracks the current Transit Delay Stage (0–8) in memory and publishes successful stage changes to
the ActiveMQ topic `package-status-topic`.

## Build and run

Start an ActiveMQ broker on `tcp://localhost:61616`, then:

```bash
mvn test package
java -Xms32m -Xmx256m -jar target/delay-stage-service.jar
```

Listens on port `7052`.

## Endpoints

```text
GET /health
GET /delay-stage/{hubId}
POST /delay-stage/{hubId}
```

POST body:

```json
{"stage": 3}
```

Stages must be integers from 0 through 8. Invalid requests return HTTP 400. A stage change is
published as:

```json
{"hubId":"H-500","stage":3,"timestamp":"2026-07-18T10:15:00Z"}
```

The same stage posted twice is idempotent: the second request returns successfully but does not
publish a duplicate event. If the broker is unavailable, the change returns HTTP 503 and the local
state is not advanced.
