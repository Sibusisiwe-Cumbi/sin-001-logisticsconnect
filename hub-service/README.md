# HubServiceApp

Serves cleaned hub, province, and sorting-centre data. It does not parse the CSV itself; on startup
it loads the cleaned records from `ingestion-service` at `http://localhost:7050/hubs`.

## Build and run

Start ingestion first, then:

```bash
mvn test package
java -Xms32m -Xmx256m -jar target/hub-service.jar
```

Listens on port `7051`.

## Endpoints

```text
GET /health
GET /hubs
GET /hubs/{hubId}
```

Unknown hub IDs return HTTP 404.
