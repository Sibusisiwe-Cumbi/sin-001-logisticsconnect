# Local ActiveMQ broker (no Docker)

This is an optional development-only broker for machines where Docker Desktop is too heavy.
It runs ActiveMQ Classic inside a small Java process and exposes the same endpoint used by the
services: `tcp://localhost:61616`.

## Run

From this directory:

```bash
mvn package
java -Xms32m -Xmx256m -jar target/local-broker.jar
```

Leave it running while `delay-stage-service`, `transit-service`, and (optionally) `alertbot` run.

This broker is non-persistent and has no web console. It is intended for local development only.
For the assignment's documented broker setup, use `../docker-compose.yml` when Docker is available.
