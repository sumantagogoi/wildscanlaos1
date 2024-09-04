package org.freeland.wildscanlaos.models.response;

import org.freeland.wildscanlaos.models.RegionSpecies;

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
