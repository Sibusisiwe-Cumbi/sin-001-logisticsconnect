package co.wethinkcode.logisticsconnect;

import java.util.List;

/**
 * Local copy of the cleaned hub shape served by ingestion-service's GET /hubs.
 * Public fields (rather than the private+getter style over in ingestion-service)
 * so Jackson can deserialize incoming JSON into this without any extra config.
 */
public class Hub {
    public String hubId;
    public String province;
    public String sortingCenter;
    public boolean active;
    public List<String> mergedFrom;
}
