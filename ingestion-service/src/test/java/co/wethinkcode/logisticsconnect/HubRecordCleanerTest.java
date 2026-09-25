package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HubRecordCleanerTest {
    private final HubRecordCleaner cleaner = new HubRecordCleaner();

    @Test
    void normalizesIdsProvinceSpacingAndBoolean() {
        HubRecord record = cleaner.clean(new String[]{" h-502 ", "gauteng", "Cape Town  Port", "YES"});

        assertEquals("H-502", record.getHubId());
        assertEquals("Gauteng", record.getProvince());
        assertEquals("Cape Town Port", record.getSortingCenter());
        assertTrue(record.isActive());
    }

    @Test
    void treatsKnownFalseAndUnknownFlagsConsistently() {
        assertFalse(cleaner.clean(new String[]{"H-1", "Gauteng", "X", "0"}).isActive());
        assertFalse(cleaner.clean(new String[]{"H-2", "Gauteng", "X", "N/A"}).isActive());
        assertFalse(cleaner.clean(new String[]{"H-3", "Gauteng", "X", "unknown"}).isActive());
    }
}
