package co.wethinkcode.logisticsconnect;

public class HubRecord {
    private String hubId;
    private String province;
    private String sortingCenter;
    private boolean active;

    public HubRecord(){

    }
    public HubRecord(String hubId, String province, String sortingCenter, boolean active){
        this.hubId = hubId;
        this.province = province;
        this.sortingCenter = sortingCenter;
        this.active = active;
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
}
