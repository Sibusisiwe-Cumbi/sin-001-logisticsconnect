package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelayStageServiceApp {

    private static final int MIN_STAGE = 0;
    private static final int MAX_STAGE = 8;

    public static void main(String[] args) {
        // In-memory only: hubId -> current delay stage. Unknown hubs default to 0 (no delay).
        Map<String, Integer> stageByHub = new ConcurrentHashMap<>();

        Javalin app = Javalin.create().start(7052);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/delay-stage/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();
            int stage = stageByHub.getOrDefault(hubId, MIN_STAGE);
            ctx.json(Map.of("hubId", hubId, "stage", stage));
        });

        // The stage/state-change endpoint referenced in common/README.md — this is also
        // where the stage-3 MQ publish to package-status-topic happens.
        app.post("/delay-stage/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();

            StageUpdateRequest body;
            try {
                body = ctx.bodyAsClass(StageUpdateRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "body must be JSON like {\"stage\": 3}"));
                return;
            }

            if (body.stage < MIN_STAGE || body.stage > MAX_STAGE) {
                ctx.status(400).json(Map.of(
                        "error", "stage must be an integer between " + MIN_STAGE + " and " + MAX_STAGE));
                return;
            }

            stageByHub.put(hubId, body.stage);

            // MQ TODO (stage 3): publish {hubId, stage, timestamp} to MqConfig.TOPIC here on
            // every successful stage change, using MqConfig.BROKER_URL
            // (see co.wethinkcode.logisticsconnect.mq.MqConfig)

            ctx.status(200).json(Map.of("hubId", hubId, "stage", body.stage));
        });
    }

    /** Request body shape for POST /delay-stage/{hubId}. */
    public static class StageUpdateRequest {
        public int stage;
    }
}
