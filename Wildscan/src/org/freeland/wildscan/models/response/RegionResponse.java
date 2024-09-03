package org.freeland.wildscan.models.response;

import org.freeland.wildscan.models.Regions;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/6/2016.
 */
public class RegionResponse {

    private boolean success;
    private Regions data;

    public boolean isSuccess() {
        return success;
    }

    public ArrayList<Regions> getData() {
        return data.getRegions();
    }
}
