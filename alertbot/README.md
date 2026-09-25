# AlertBotApp

Stage-4 stretch goal. Subscribes to `package-status-topic` and emits a simulated alert when a hub
crosses from below delay stage 5 to stage 5 or above.

No real social-media API is called; the alert is represented by a log line.

## Build and run

Start an ActiveMQ broker on `tcp://localhost:61616`, then:

```bash
mvn test package
java -Xms32m -Xmx256m -jar target/alertbot.jar
```

Listens on port `7054`.

## Endpoint

```text
GET /health
```

The useful work happens asynchronously. The subscriber reconnects if the broker is temporarily
unavailable at startup.
