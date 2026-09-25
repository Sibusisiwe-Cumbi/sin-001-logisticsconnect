package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Talks to hub-service over plain HTTP for hub/location data - this is the stage-2
 * synchronous wiring that stage 3 leaves in place. The old delay-stage-service leg
 * (fetchDelayStage, formerly GET :7052/delay-stage/{hubId}) has been removed: as of
 * stage 3, delay-stage data arrives via the package-status-topic MQ subscription in
 * {@link co.wethinkcode.logisticsconnect.mq.DelayStageSubscriber} instead.
 */
public class DownstreamClient {

    private static final String HUB_SERVICE_URL = "http://localhost:7051";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Returns null if hub-service reports 404 for this hubId. */
    public Hub fetchHub(String hubId) throws Exception {
        HttpResponse<String> response = get(HUB_SERVICE_URL + "/hubs/" + hubId);
        if (response.statusCode() == 404) {
            return null;
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException("hub-service returned status " + response.statusCode());
        }
        return mapper.readValue(response.body(), Hub.class);
    }

    private HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
