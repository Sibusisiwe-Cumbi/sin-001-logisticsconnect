package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.List;

public class IngestionServiceApp {

    public static void main(String[] args) throws Exception {
        List<HubRecord> hubs = loadCleanedHubs();

        Javalin app = Javalin.create().start(7050);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(hubs));
    }

    private static List<HubRecord> loadCleanedHubs() throws Exception {
        HubCsvLoader loader = new HubCsvLoader();
        HubRecordCleaner cleaner = new HubRecordCleaner();
        HubDeduplicator deduplicator = new HubDeduplicator();

        List<String[]> rawRows = loader.readRawRows();
        List<HubRecord> cleaned = rawRows.stream()
                .skip(1) // header row: hub_id, Province, sorting_center, active
                .map(cleaner::clean)
                .toList();

        return deduplicator.deduplicate(cleaned);
    }
}
