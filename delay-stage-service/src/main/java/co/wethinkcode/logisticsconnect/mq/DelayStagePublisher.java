package co.wethinkcode.logisticsconnect.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.time.Instant;
import java.util.Map;

/** Publishes delay-stage changes to the shared ActiveMQ topic. */
public class DelayStagePublisher implements AutoCloseable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Object lock = new Object();
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    /** Connects once at startup. Publish also retries the connection if the broker was unavailable. */
    public void start() throws JMSException {
        synchronized (lock) {
            connect();
        }
    }

    public void publishStageChange(String hubId, int stage) {
        synchronized (lock) {
            try {
                ensureConnected();
                String payload = mapper.writeValueAsString(Map.of(
                        "hubId", hubId,
                        "stage", stage,
                        "timestamp", Instant.now().toString()
                ));
                TextMessage message = session.createTextMessage(payload);
                producer.send(message);
                System.out.println("[mq] published stage update: " + payload);
            } catch (Exception firstFailure) {
                closeResources();
                try {
                    connect();
                    String payload = mapper.writeValueAsString(Map.of(
                            "hubId", hubId,
                            "stage", stage,
                            "timestamp", Instant.now().toString()
                    ));
                    producer.send(session.createTextMessage(payload));
                    System.out.println("[mq] published stage update after reconnect: " + payload);
                } catch (Exception retryFailure) {
                    throw new IllegalStateException("ActiveMQ publish failed: " + retryFailure.getMessage(), retryFailure);
                }
            }
        }
    }

    private void ensureConnected() throws JMSException {
        if (connection == null || session == null || producer == null) {
            connect();
        }
    }

    private void connect() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        Connection newConnection = factory.createConnection();
        Session newSession = newConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = newSession.createTopic(MqConfig.TOPIC);
        MessageProducer newProducer = newSession.createProducer(topic);
        newConnection.start();

        closeResources();
        connection = newConnection;
        session = newSession;
        producer = newProducer;
        System.out.println("[mq] publisher connected to " + MqConfig.BROKER_URL);
    }

    private void closeResources() {
        try { if (producer != null) producer.close(); } catch (Exception ignored) { }
        try { if (session != null) session.close(); } catch (Exception ignored) { }
        try { if (connection != null) connection.close(); } catch (Exception ignored) { }
        producer = null;
        session = null;
        connection = null;
    }

    @Override
    public void close() {
        synchronized (lock) {
            closeResources();
        }
    }
}
