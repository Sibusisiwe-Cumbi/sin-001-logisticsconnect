package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Pulls the cleaned hub records from ingestion-service (GET :7050/hubs) so
 * hub-service serves data sourced from the cleaned CSV output instead of
 * re-parsing hubs-global.csv itself.
 */
public class IngestionClient {

    private static final String INGESTION_URL = "http://localhost:7050/hubs";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Hub> fetchHubs() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(INGESTION_URL))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("ingestion-service returned status " + response.statusCode());
        }

        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Hub.class);
        return mapper.readValue(response.body(), listType);
    }

    /**
     * ingestion-service needs to be up first per the run order in the root README;
     * retry a few times so hub-service doesn't just crash if it's a beat slow to start.
     */
    public List<Hub> fetchHubsWithRetry(int attempts, long delayMillis) throws Exception {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return fetchHubs();
            } catch (Exception e) {
                lastFailure = e;
                System.out.println("ingestion-service not ready yet (attempt " + attempt + "/" + attempts + "): " + e.getMessage());
                Thread.sleep(delayMillis);
            }
        }
        throw lastFailure;
    }
}
