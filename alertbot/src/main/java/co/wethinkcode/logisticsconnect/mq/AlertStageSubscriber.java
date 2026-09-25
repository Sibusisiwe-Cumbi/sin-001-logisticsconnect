package co.wethinkcode.logisticsconnect.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stage-4 subscriber. A simulated alert is emitted when a hub crosses from below
 * the configured threshold to the threshold or above.
 */
public class AlertStageSubscriber implements AutoCloseable {
    public static final int ALERT_THRESHOLD = 5;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Integer> latestStageByHub = new ConcurrentHashMap<>();
    private volatile boolean running;
    private Thread reconnectThread;
    private volatile Connection connection;

    public void start() throws JMSException {
        running = true;
        tryConnect();
        reconnectThread = new Thread(this::reconnectLoop, "alertbot-mq-reconnector");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    private void reconnectLoop() {
        while (running) {
            if (connection == null) {
                try {
                    tryConnect();
                } catch (JMSException e) {
                    System.err.println("[mq] alertbot broker unavailable: " + e.getMessage());
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private synchronized void tryConnect() throws JMSException {
        if (!running || connection != null) return;
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        Connection newConnection = factory.createConnection();
        Session session = newConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(MqConfig.TOPIC);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(this::onMessage);
        newConnection.setExceptionListener(exception -> {
            System.err.println("[mq] connection lost: " + exception.getMessage());
            try { newConnection.close(); } catch (JMSException ignored) { }
            if (connection == newConnection) connection = null;
        });
        newConnection.start();
        connection = newConnection;
        System.out.println("[mq] alertbot subscribed to " + MqConfig.TOPIC);
    }

    private void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) return;
            JsonNode json = mapper.readTree(textMessage.getText());
            String hubId = json.path("hubId").asText("").trim().toUpperCase();
            int stage = json.path("stage").asInt(-1);
            if (hubId.isEmpty() || stage < 0) return;

            int previous = latestStageByHub.getOrDefault(hubId, 0);
            latestStageByHub.put(hubId, stage);

            if (previous < ALERT_THRESHOLD && stage >= ALERT_THRESHOLD) {
                System.out.println("[ALERT] simulated notification: " + hubId
                        + " reached delay stage " + stage + " (threshold " + ALERT_THRESHOLD + ")");
            }
        } catch (Exception e) {
            System.err.println("[mq] alertbot failed to process message: " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        running = false;
        if (reconnectThread != null) reconnectThread.interrupt();
        try { if (connection != null) connection.close(); } catch (JMSException ignored) { }
        connection = null;
    }
}
