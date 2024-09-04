package org.freeland.wildscanlaos.models.response;

import org.freeland.wildscanlaos.models.Regions;

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
