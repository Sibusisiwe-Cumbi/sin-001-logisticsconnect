package co.wethinkcode.logisticsconnect;

import java.util.Map;
import java.util.Set;


public class HubRecordCleaner {

    
    private static final Map<String, String> PROVINCE_CANONICAL = Map.ofEntries(
            Map.entry("gauteng", "Gauteng"),
            Map.entry("western cape", "Western Cape"),
            Map.entry("eastern cape", "Eastern Cape"),
            Map.entry("kwazulu-natal", "KwaZulu-Natal"),
            Map.entry("kwa-zulu natal", "KwaZulu-Natal"),
            Map.entry("kwazulu natal", "KwaZulu-Natal"),
            Map.entry("free state", "Free State"),
            Map.entry("limpopo", "Limpopo"),
            Map.entry("north west", "North West"),
            Map.entry("mpumalanga", "Mpumalanga"),
            Map.entry("northern cape", "Northern Cape")
    );

    private static final Set<String> TRUE_VALUES = Set.of("y", "yes", "1", "true");
    private static final Set<String> FALSE_VALUES = Set.of("n", "no", "0", "false");
    

    public HubRecord clean(String[] rawRow) {
        String hubId = normalizeSpacing(rawRow[0]).toUpperCase();
        String province = canonicalProvince(rawRow[1]);
        
        String sortingCenter = titleCase(normalizeSpacing(rawRow[2]));
        boolean active = parseActive(rawRow[3]);

        return new HubRecord(hubId, province, sortingCenter, active);
    }


    private String normalizeSpacing(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String canonicalProvince(String rawProvince) {
        String cleaned = normalizeSpacing(rawProvince);
        if (cleaned.isEmpty()) {
            return "";
        }
        String key = cleaned.toLowerCase();
        return PROVINCE_CANONICAL.getOrDefault(key, titleCase(cleaned));
    }

    private String titleCase(String value) {
        String[] words = value.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private boolean parseActive(String rawActive) {
        String key = normalizeSpacing(rawActive).toLowerCase();
        if (TRUE_VALUES.contains(key)) {
            return true;
        }
        if (FALSE_VALUES.contains(key)) {
            return false;
        }
        return false; 
    }
}