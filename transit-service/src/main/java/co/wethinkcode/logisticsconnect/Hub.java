package co.wethinkcode.logisticsconnect;

import java.util.List;

/**
 * Local copy of the hub shape served by hub-service's GET /hubs/{hubId}.
 * Public fields so Jackson can deserialize the response without extra config.
 */
public class Hub {
    public String hubId;
    public String province;
    public String sortingCenter;
    public boolean active;
    public List<String> mergedFrom;
}
