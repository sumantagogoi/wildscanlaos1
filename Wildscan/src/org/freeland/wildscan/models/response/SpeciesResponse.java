package org.freeland.wildscan.models.response;

import org.freeland.wildscan.models.RegionSpecies;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/7/2016.
 */
public class SpeciesResponse {

    private boolean success;
    private RegionSpecies data;

    public ArrayList<RegionSpecies> getData() {
        return data.getSpecies();
    }

    public boolean isSuccess() {
        return success;
    }
}
