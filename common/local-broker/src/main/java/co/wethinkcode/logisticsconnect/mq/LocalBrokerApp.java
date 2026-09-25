package co.wethinkcode.logisticsconnect.mq;

import org.apache.activemq.broker.BrokerService;

/**
 * Lightweight no-Docker broker for development machines. It exposes the same
 * OpenWire endpoint used by the assignment services: tcp://localhost:61616.
 */
public final class LocalBrokerApp {
    private LocalBrokerApp() { }

    public static void main(String[] args) throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("logisticsconnect-local");
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:61616");
        broker.start();
        broker.waitUntilStarted();
        System.out.println("ActiveMQ is running at tcp://localhost:61616");
        System.out.println("Press Ctrl+C to stop the local broker.");
        broker.waitUntilStopped();
    }
}
