package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.Map;

public class TransitServiceApp {

    // Placeholder timing model: a flat base transit time plus a fixed penalty per
    // delay stage (0-8). Good enough to produce a legible ETA-shaped response;
    // a real model would vary base time by route/distance.
    private static final int BASE_ETA_HOURS = 24;
    private static final int DELAY_HOURS_PER_STAGE = 4;

    public static void main(String[] args) {
        DownstreamClient downstreamClient = new DownstreamClient();

        Javalin app = Javalin.create().start(7053);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/eta/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();

            Hub hub;
            try {
                hub = downstreamClient.fetchHub(hubId);
            } catch (Exception e) {
                ctx.status(502).json(Map.of("error", "hub-service unreachable: " + e.getMessage()));
                return;
            }
            if (hub == null) {
                ctx.status(404).json(Map.of("error", "no hub found for id " + hubId));
                return;
            }

            DelayStage delayStage;
            try {
                // MQ TODO (stage 3): replace this synchronous call with the stage last
                // received from subscribing to MqConfig.TOPIC instead of calling
                // delay-stage-service directly (see co.wethinkcode.logisticsconnect.mq.MqConfig)
                delayStage = downstreamClient.fetchDelayStage(hubId);
            } catch (Exception e) {
                ctx.status(502).json(Map.of("error", "delay-stage-service unreachable: " + e.getMessage()));
                return;
            }

            int delayHours = delayStage.stage * DELAY_HOURS_PER_STAGE;
            int estimatedEtaHours = BASE_ETA_HOURS + delayHours;

            ctx.json(Map.of(
                    "hubId", hub.hubId,
                    "province", hub.province,
                    "sortingCenter", hub.sortingCenter,
                    "delayStage", delayStage.stage,
                    "baseEtaHours", BASE_ETA_HOURS,
                    "delayHours", delayHours,
                    "estimatedEtaHours", estimatedEtaHours
            ));
        });
    }
}
