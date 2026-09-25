package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.DelayStageSubscriber;
import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

import javax.jms.JMSException;
import java.util.Map;

public class TransitServiceApp {

    // Placeholder timing model: a flat base transit time plus a fixed penalty per
    // delay stage (0-8). Good enough to produce a legible ETA-shaped response;
    // a real model would vary base time by route/distance.
    private static final int BASE_ETA_HOURS = 24;
    private static final int DELAY_HOURS_PER_STAGE = 4;

    public static void main(String[] args) {
        DownstreamClient downstreamClient = new DownstreamClient();

        // Stage 3: subscribe to package-status-topic instead of calling
        // delay-stage-service synchronously for every /eta/{hubId} request.
        DelayStageSubscriber delayStageSubscriber = new DelayStageSubscriber();
        try {
            delayStageSubscriber.start();
        } catch (JMSException e) {
            System.err.println("Warning: could not connect to ActiveMQ broker at " + MqConfig.BROKER_URL
                    + " - delay stages will default to 0 until it's reachable: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(delayStageSubscriber::close));

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

            // Stage 3: read the last stage received over package-status-topic instead of
            // calling delay-stage-service directly. No HTTP round trip, so there's no
            // "unreachable" failure mode here - an unknown hub just reads as stage 0
            // (no delay) until a message for it arrives.
            int stage = delayStageSubscriber.getStage(hubId);

            int delayHours = stage * DELAY_HOURS_PER_STAGE;
            int estimatedEtaHours = BASE_ETA_HOURS + delayHours;

            ctx.json(Map.of(
                    "hubId", hub.hubId,
                    "province", hub.province,
                    "sortingCenter", hub.sortingCenter,
                    "delayStage", stage,
                    "baseEtaHours", BASE_ETA_HOURS,
                    "delayHours", delayHours,
                    "estimatedEtaHours", estimatedEtaHours
            ));
        });
    }
}
