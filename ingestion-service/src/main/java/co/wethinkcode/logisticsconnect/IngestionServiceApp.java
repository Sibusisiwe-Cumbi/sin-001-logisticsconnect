package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.Comparator;
import java.util.List;

public class IngestionServiceApp {

    public static void main(String[] args) throws Exception {
        List<HubRecord> hubs = loadCleanedHubs();

        Javalin app = Javalin.create().start(7050);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(hubs));
    }

    static List<HubRecord> loadCleanedHubs() throws Exception {
        HubCsvLoader loader = new HubCsvLoader();
        HubRecordCleaner cleaner = new HubRecordCleaner();
        HubDeduplicator deduplicator = new HubDeduplicator();

        List<String[]> rawRows = loader.readRawRows();
        if (rawRows.isEmpty()) {
            return List.of();
        }

        List<HubRecord> cleaned = rawRows.stream()
                .skip(1)
                .filter(row -> row.length > 0 && !String.join("", row).trim().isEmpty())
                .map(row -> {
                    if (row.length < 4) {
                        throw new IllegalArgumentException("CSV row has fewer than 4 columns");
                    }
                    return cleaner.clean(row);
                })
                .toList();

        return deduplicator.deduplicate(cleaned).stream()
                .sorted(Comparator.comparing(HubRecord::getHubId))
                .toList();
    }
}
