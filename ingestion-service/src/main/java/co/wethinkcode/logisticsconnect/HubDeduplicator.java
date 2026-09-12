package co.wethinkcode.logisticsconnect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HubDeduplicator {

    public List<HubRecord> deduplicate(List<HubRecord> cleanedRecords) {
        Map<String, List<HubRecord>> groups = new LinkedHashMap<>();

        for (HubRecord record : cleanedRecords) {
            String key = groupKey(record, cleanedRecords);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }

        List<HubRecord> merged = new ArrayList<>();
        for (List<HubRecord> group : groups.values()) {
            merged.add(mergeGroup(group));
        }
        return merged;
    }

    private String groupKey(HubRecord record, List<HubRecord> allRecords) {
        if (!record.getProvince().isEmpty()) {
            return record.getProvince() + "|" + record.getSortingCenter();
        }
    
        for (HubRecord other : allRecords) {
            if (other != record
                    && other.getSortingCenter().equals(record.getSortingCenter())
                    && !other.getProvince().isEmpty()) {
                return other.getProvince() + "|" + other.getSortingCenter();
            }
        }
        
        return "UNKNOWN-PROVINCE|" + record.getSortingCenter();
    }

    private HubRecord mergeGroup(List<HubRecord> group) {
        if (group.size() == 1) {
            return group.get(0);
        }

        
        String canonicalId = group.stream()
                .map(HubRecord::getHubId)
                .sorted()
                .findFirst()
                .orElseThrow();

        String province = group.stream()
                .map(HubRecord::getProvince)
                .filter(p -> !p.isEmpty())
                .findFirst()
                .orElse("");

        String sortingCenter = group.get(0).getSortingCenter();

        long activeVotes = group.stream().filter(HubRecord::isActive).count();
        boolean active = activeVotes > group.size() / 2.0; // ties -> false

        List<String> mergedFrom = group.stream()
                .map(HubRecord::getHubId)
                .sorted()
                .toList();

        return new HubRecord(canonicalId, province, sortingCenter, active, mergedFrom);
    }
}
