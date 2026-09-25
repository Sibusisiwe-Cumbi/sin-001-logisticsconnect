package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.AlertStageSubscriber;
import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

import javax.jms.JMSException;

public class AlertBotApp {

    public static void main(String[] args) {
        // Stage 4 (stretch): subscribe to package-status-topic and post a simulated
        // proactive delay notification whenever a hub crosses the alert threshold.
        // See co.wethinkcode.logisticsconnect.mq.AlertStageSubscriber for the threshold
        // and the (simulated) posting mechanism.
        AlertStageSubscriber alertStageSubscriber = new AlertStageSubscriber();
        try {
            alertStageSubscriber.start();
        } catch (JMSException e) {
            System.err.println("Warning: could not connect to ActiveMQ broker at " + MqConfig.BROKER_URL
                    + " - alertbot won't see stage updates until it's reachable: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(alertStageSubscriber::close));

        Javalin app = Javalin.create().start(7054);

        app.get("/health", ctx -> ctx.result("OK"));
    }
}
