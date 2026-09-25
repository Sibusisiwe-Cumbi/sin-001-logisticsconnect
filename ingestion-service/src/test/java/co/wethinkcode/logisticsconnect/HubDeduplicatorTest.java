package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubDeduplicatorTest {
    @Test
    void mergesDuplicateRealWorldHubAndKeepsCanonicalIdAndProvenance() {
        List<HubRecord> records = List.of(
                new HubRecord("H-500", "Gauteng", "Johannesburg Central", true),
                new HubRecord("H-504", "Gauteng", "Johannesburg Central", false),
                new HubRecord("H-510", "Gauteng", "Johannesburg Central", false),
                new HubRecord("H-515", "Gauteng", "Johannesburg Central", true)
        );

        HubRecord merged = new HubDeduplicator().deduplicate(records).get(0);

        assertEquals("H-500", merged.getHubId());
        assertEquals("Gauteng", merged.getProvince());
        assertEquals("Johannesburg Central", merged.getSortingCenter());
        assertFalse(merged.isActive(), "A 2/4 tie is intentionally resolved conservatively as inactive");
        assertEquals(List.of("H-500", "H-504", "H-510", "H-515"), merged.getMergedFrom());
    }

    @Test
    void fillsMissingProvinceFromAnotherRecordForSameSortingCenter() {
        List<HubRecord> records = List.of(
                new HubRecord("H-502", "Gauteng", "Pretoria North", false),
                new HubRecord("H-508", "", "Pretoria North", true)
        );

        HubRecord merged = new HubDeduplicator().deduplicate(records).get(0);

        assertEquals("Gauteng", merged.getProvince());
        assertEquals(List.of("H-502", "H-508"), merged.getMergedFrom());
    }
}
