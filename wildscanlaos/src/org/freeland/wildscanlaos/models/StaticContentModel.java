package org.freeland.wildscanlaos.models;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class StaticContentModel {
    private ArrayList<StaticContentModel> static_content ;
    private String type;
    private String language;
    private String content;
    private String created_by;
    private String created_date;
    private String updated_by;
    private String updated_date;

    public String getType() {
        return type;
    }
    public String getLanguage() {
        return language;
    }
    public String getContent() {
        return content;
    }
    public String getCreated_by() {
        return created_by;
    }
    public String getCreated_date() {
        return created_date;
    }
    public String getUpdated_by() {
        return updated_by;
    }
    public String getUpdated_date() {
        return updated_date;
    }
    public ArrayList<StaticContentModel> getStatic_content() {
        return static_content;
    }

    public void setType(String type) {
        this.type = type;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }
    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }
    public void setUpdated_date(String updated_date) {
        this.updated_date = updated_date;
    }
}
