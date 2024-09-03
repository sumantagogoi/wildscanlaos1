package org.freeland.wildscan.models.response;

import org.freeland.wildscan.models.RegionStats;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */
public class StatsResponse {

    private boolean success;
    private RegionStats data;

    public boolean isSuccess() {
        return success;
    }

    public ArrayList<RegionStats> getData() {
        return data.getStats();
    }
}
