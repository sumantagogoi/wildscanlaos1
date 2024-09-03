package org.freeland.wildscan.models;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */
public class RegionStats {

    private ArrayList<RegionStats> stats;


    private String region;
    private String total_species;
    private String total_contacts;
    private String total_users;

    public ArrayList<RegionStats> getStats() {
        return stats;
    }

    public String getRegion() {
        return region;
    }

    public String getTotal_species() {
        return total_species;
    }

    public String getTotal_contacts() {
        return total_contacts;
    }

    public String getTotal_users() {
        return total_users;
    }


}
