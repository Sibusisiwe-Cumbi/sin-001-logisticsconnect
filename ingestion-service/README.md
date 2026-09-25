# IngestionServiceApp

Loads the legacy `hubs-global.csv`, cleans it, resolves duplicate real-world hubs, and exposes
the cleaned records over REST.

## Data-cleaning policy

- IDs are trimmed and uppercased.
- Repeated whitespace is collapsed.
- Known province spelling/casing variants are canonicalised.
- Sorting-centre names are normalised to title case.
- Boolean flags are normalised to `true`/`false`.
- `unknown` and `N/A` active values are treated as inactive because the API contract exposes a
  boolean field; this conservative rule is documented rather than silently leaving placeholders.
- Duplicate records are grouped by canonical province + sorting centre.
- A missing province is recovered from another record for the same sorting centre where possible.
- The smallest canonical hub ID becomes the representative ID; all contributing IDs are retained
  in `mergedFrom`.

The supplied CSV contains 18 rows and produces 10 cleaned records.

## Build and run

```bash
mvn test package
java -Xms32m -Xmx256m -jar target/ingestion-service.jar
```

Listens on port `7050`.

## Endpoints

```text
GET /health
GET /hubs
```
