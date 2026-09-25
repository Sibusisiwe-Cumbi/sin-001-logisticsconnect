package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Talks to hub-service and delay-stage-service over plain HTTP — this is the
 * stage-2 synchronous wiring. Stage 3 replaces the delay-stage-service leg with
 * an MQ subscription instead of the fetchDelayStage call here.
 */
public class DownstreamClient {

    private static final String HUB_SERVICE_URL = "http://localhost:7051";
    private static final String DELAY_STAGE_SERVICE_URL = "http://localhost:7052";

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

    public DelayStage fetchDelayStage(String hubId) throws Exception {
        HttpResponse<String> response = get(DELAY_STAGE_SERVICE_URL + "/delay-stage/" + hubId);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("delay-stage-service returned status " + response.statusCode());
        }
        return mapper.readValue(response.body(), DelayStage.class);
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
