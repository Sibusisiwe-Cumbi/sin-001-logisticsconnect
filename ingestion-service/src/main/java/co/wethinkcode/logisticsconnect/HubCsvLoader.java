package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Loads the bundled CSV without applying domain-cleaning rules. */
public class HubCsvLoader {
    public List<String[]> readRawRows() throws Exception {
        InputStream inputStream = HubCsvLoader.class.getResourceAsStream("/hubs-global.csv");
        if (inputStream == null) {
            throw new IllegalStateException("hubs-global.csv was not found on the classpath");
        }

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll();
        }
    }
}
