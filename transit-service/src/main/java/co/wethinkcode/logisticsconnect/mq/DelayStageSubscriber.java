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
 * Stage-3 MQ decoupling: subscribes to {@code package-status-topic} and keeps the
 * most recently seen delay stage per hub in memory. This replaces the old synchronous
 * GET :7052/delay-stage/{hubId} call to delay-stage-service - GET /eta/{hubId} now
 * reads whatever this subscriber last received instead of calling out over HTTP.
 */
public class DelayStageSubscriber implements AutoCloseable {

    /** Stage to report for a hub we haven't received any message for yet - "no known delay". */
    private static final int DEFAULT_STAGE = 0;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Integer> latestStageByHub = new ConcurrentHashMap<>();

    private Connection connection;

    /** Opens the connection to the broker and starts listening. Called once at startup. */
    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(MqConfig.TOPIC);
        MessageConsumer consumer = session.createConsumer(topic);

        consumer.setMessageListener(this::onMessage);
        connection.start();
        System.out.println("[mq] subscribed to " + MqConfig.TOPIC + " at " + MqConfig.BROKER_URL);
    }

    private void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) {
                return;
            }
            JsonNode json = mapper.readTree(textMessage.getText());
            String hubId = json.get("hubId").asText().toUpperCase();
            int stage = json.get("stage").asInt();
            latestStageByHub.put(hubId, stage);
            System.out.println("[mq] received stage update: " + hubId + " -> " + stage);
        } catch (Exception e) {
            System.err.println("[mq] failed to process message from " + MqConfig.TOPIC + ": " + e.getMessage());
        }
    }

    /** Latest stage received for this hub over the topic, or DEFAULT_STAGE if none yet. */
    public int getStage(String hubId) {
        return latestStageByHub.getOrDefault(hubId.toUpperCase(), DEFAULT_STAGE);
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            System.err.println("[mq] error closing connection: " + e.getMessage());
        }
    }
}
