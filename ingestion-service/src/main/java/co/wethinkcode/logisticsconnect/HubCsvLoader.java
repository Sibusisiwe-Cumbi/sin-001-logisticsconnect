package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class HubCsvLoader {
    public List<String[]> readRawRows() throws Exception{
        InputStream inputStream = HubCsvLoader.class.getResourceAsStream("/hubs-global.csv");
        Reader reader = new InputStreamReader(inputStream);
        CSVReader csvReader = new CSVReader(reader);

        List<String[]> allRows = csvReader.readAll();
        csvReader.close();
        return allRows;
    }
}