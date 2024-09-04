package org.freeland.wildscanlaos.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/6/2016.
 */
public class Regions {

    private ArrayList<Regions> regions;

    private String id;
    private String code;
    private String name;
    private String description;
    @SerializedName("report_email")
    private String reportEmail;

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getReportEmail() {
        return reportEmail;
    }

    public ArrayList<Regions> getRegions() {
        return regions;
    }
}
