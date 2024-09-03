package org.freeland.wildscan.models;

import java.util.ArrayList;

/**
 * Created by nomankhan25dec on 3/6/2016.
 */
public class RegionContacts {

    private ArrayList<RegionContacts> contacts;
    private String s3Url;

    private String id;
    private String name;
    private String avatar;
    private String type;
    private String agency;
    private String jurisdiction_scope;
    private String specialcapacity_note;
    private String email;
    private String phone;
    private String address1;
    private String address2;
    private String city;
    private String country;
    private String region;
    private String website;
    private String availability;
    private String lat;
    private String lon;
    private String utm;
    private String created_by;
    private String created_date;
    private String updated_by;

    public ArrayList<RegionContacts> getContacts() {
        return contacts;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getType() {
        return type;
    }

    public String getAgency() {
        return agency;
    }

    public String getJurisdiction_scope() {
        return jurisdiction_scope;
    }

    public String getSpecialcapacity_note() {
        return specialcapacity_note;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getWebsite() {
        return website;
    }

    public String getAvailability() {
        return availability;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getUtm() {
        return utm;
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

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getS3Url() {
        return this.s3Url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setJurisdiction_scope(String jurisdiction_scope) {
        this.jurisdiction_scope = jurisdiction_scope;
    }

    public void setSpecialcapacity_note(String specialcapacity_note) {
        this.specialcapacity_note = specialcapacity_note;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setUtm(String utm) {
        this.utm = utm;
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
}
