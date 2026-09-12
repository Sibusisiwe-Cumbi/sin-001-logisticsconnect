package co.wethinkcode.logisticsconnect;

import java.util.List;

public class HubRecord {
    private String hubId;
    private String province;
    private String sortingCenter;
    private boolean active;
    
    private List<String> mergedFrom;

    public HubRecord(){

    }
    public HubRecord(String hubId, String province, String sortingCenter, boolean active){
        this(hubId, province, sortingCenter, active, List.of(hubId));
    }

    public HubRecord(String hubId, String province, String sortingCenter, boolean active, List<String> mergedFrom){
        this.hubId = hubId;
        this.province = province;
        this.sortingCenter = sortingCenter;
        this.active = active;
        this.mergedFrom = mergedFrom;
    }

    public String getHubId(){
        return hubId;
    }
    public String getProvince(){
        return province;
    }
    public String getSortingCenter(){
        return sortingCenter;
    }
    public boolean isActive(){
        return active;
    }
    public List<String> getMergedFrom(){
        return mergedFrom;
    }
}
