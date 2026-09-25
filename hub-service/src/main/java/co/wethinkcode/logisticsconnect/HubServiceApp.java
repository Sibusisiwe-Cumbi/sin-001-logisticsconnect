package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HubServiceApp {

    public static void main(String[] args) throws Exception {
        Map<String, Hub> hubsById = new ConcurrentHashMap<>();
        loadHubsFromIngestion(hubsById);

        Javalin app = Javalin.create().start(7051);

        app.get("/health", ctx -> ctx.result("OK"));

        // Place-name source of truth: cleaned hub records sourced from ingestion-service.
        app.get("/hubs", ctx -> ctx.json(hubsById.values()));

        app.get("/hubs/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();
            Hub hub = hubsById.get(hubId);
            if (hub == null) {
                ctx.status(404).json(Map.of("error", "no hub found for id " + hubId));
                return;
            }
            ctx.json(hub);
        });
    }

    private static void loadHubsFromIngestion(Map<String, Hub> hubsById) throws Exception {
        IngestionClient ingestionClient = new IngestionClient();
        List<Hub> hubs = ingestionClient.fetchHubsWithRetry(5, 1000);
        for (Hub hub : hubs) {
            hubsById.put(hub.hubId, hub);
        }
    }
}
