package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngestionPipelineTest {
    @Test
    void bundledCsvProducesTenDeterministicCleanedRecords() throws Exception {
        List<HubRecord> hubs = IngestionServiceApp.loadCleanedHubs();

        assertEquals(10, hubs.size());
        assertEquals("H-500", hubs.get(0).getHubId());
        assertEquals("Gauteng", hubs.get(0).getProvince());
        assertEquals("Johannesburg Central", hubs.get(0).getSortingCenter());
        assertEquals(List.of("H-500", "H-504", "H-510", "H-515"), hubs.get(0).getMergedFrom());

        HubRecord durban = hubs.stream()
                .filter(h -> h.getSortingCenter().equals("Durban Harbour"))
                .findFirst()
                .orElseThrow();
        assertEquals("KwaZulu-Natal", durban.getProvince());
        assertEquals(List.of("H-503", "H-506", "H-516"), durban.getMergedFrom());
    }
}
