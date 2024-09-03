package org.freeland.wildscan.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nomankhan25dec on 4/3/2016.
 */
public class EventsModel {

    @SerializedName("id")
    private String id;

    @SerializedName("incident_date")
    private String incidentDate;

    @SerializedName("internet_incident")
    private String internetIncident;

    @SerializedName("location_address")
    private String locationAddress;

    @SerializedName("web_address")
    private String webAddress;

    @SerializedName("location_lat")
    private String locationLat;

    @SerializedName("location_lon")
    private String locationLon;

    @SerializedName("region")
    private Object region;

    @SerializedName("incident")
    private String incident;

    @SerializedName("species")
    private String species;

    @SerializedName("number")
    private String number;

    @SerializedName("number_unit")
    private String numberUnit;

    @SerializedName("incident_condition")
    private String incidentCondition;

    @SerializedName("offense_type")
    private String offenseType;

    @SerializedName("offense_description")
    private String offenseDescription;

    @SerializedName("method")
    private String method;

    @SerializedName("value_estimated_usd")
    private String valueEstimatedUsd;

    @SerializedName("origin_address")
    private String originAddress;

    @SerializedName("origin_country")
    private String originCountry;

    @SerializedName("origin_lat")
    private String originLat;

    @SerializedName("origin_lon")
    private String originLon;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("destination_country")
    private String destinationCountry;

    @SerializedName("destination_lat")
    private String destinationLat;

    @SerializedName("destination_lon")
    private String destinationLon;

    @SerializedName("vehicle_vessel_description")
    private String vehicleVesselDescription;

    @SerializedName("vehicle_vessel_license_number")
    private String vehicleVesselLicenseNumber;

    @SerializedName("vessel_name")
    private String vesselName;

    @SerializedName("share_with")
    private String shareWith;

    @SerializedName("syndicate")
    private String syndicate;

    @SerializedName("status")
    private String status;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("updated_by")
    private String updatedBy;

    @SerializedName("updated_date")
    private String updatedDate;

    @SerializedName("image")
    private String image;

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The incidentDate
     */
    public String getIncidentDate() {
        return incidentDate;
    }

    /**
     * @param incidentDate The incident_date
     */
    public void setIncidentDate(String incidentDate) {
        this.incidentDate = incidentDate;
    }

    /**
     * @return The internetIncident
     */
    public String getInternetIncident() {
        return internetIncident;
    }

    /**
     * @param internetIncident The internet_incident
     */
    public void setInternetIncident(String internetIncident) {
        this.internetIncident = internetIncident;
    }

    /**
     * @return The locationAddress
     */
    public String getLocationAddress() {
        return locationAddress;
    }

    /**
     * @param locationAddress The location_address
     */
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    /**
     * @return The webAddress
     */
    public String getWebAddress() {
        return webAddress;
    }

    /**
     * @param webAddress The web_address
     */
    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    /**
     * @return The locationLat
     */
    public String getLocationLat() {
        return locationLat;
    }

    /**
     * @param locationLat The location_lat
     */
    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat;
    }

    /**
     * @return The locationLon
     */
    public String getLocationLon() {
        return locationLon;
    }

    /**
     * @param locationLon The location_lon
     */
    public void setLocationLon(String locationLon) {
        this.locationLon = locationLon;
    }

    /**
     * @return The region
     */
    public Object getRegion() {
        return region;
    }

    /**
     * @param region The region
     */
    public void setRegion(Object region) {
        this.region = region;
    }

    /**
     * @return The incident
     */
    public String getIncident() {
        return incident;
    }

    /**
     * @param incident The incident
     */
    public void setIncident(String incident) {
        this.incident = incident;
    }

    /**
     * @return The species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * @param species The species
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * @return The number
     */
    public String getNumber() {
        return number;
    }

    /**
     * @param number The number
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * @return The numberUnit
     */
    public String getNumberUnit() {
        return numberUnit;
    }

    /**
     * @param numberUnit The number_unit
     */
    public void setNumberUnit(String numberUnit) {
        this.numberUnit = numberUnit;
    }

    /**
     * @return The incidentCondition
     */
    public String getIncidentCondition() {
        return incidentCondition;
    }

    /**
     * @param incidentCondition The incident_condition
     */
    public void setIncidentCondition(String incidentCondition) {
        this.incidentCondition = incidentCondition;
    }

    /**
     * @return The offenseType
     */
    public String getOffenseType() {
        return offenseType;
    }

    /**
     * @param offenseType The offense_type
     */
    public void setOffenseType(String offenseType) {
        this.offenseType = offenseType;
    }

    /**
     * @return The offenseDescription
     */
    public String getOffenseDescription() {
        return offenseDescription;
    }

    /**
     * @param offenseDescription The offense_description
     */
    public void setOffenseDescription(String offenseDescription) {
        this.offenseDescription = offenseDescription;
    }

    /**
     * @return The method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method The method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return The valueEstimatedUsd
     */
    public String getValueEstimatedUsd() {
        return valueEstimatedUsd;
    }

    /**
     * @param valueEstimatedUsd The value_estimated_usd
     */
    public void setValueEstimatedUsd(String valueEstimatedUsd) {
        this.valueEstimatedUsd = valueEstimatedUsd;
    }

    /**
     * @return The originAddress
     */
    public String getOriginAddress() {
        return originAddress;
    }

    /**
     * @param originAddress The origin_address
     */
    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    /**
     * @return The originCountry
     */
    public String getOriginCountry() {
        return originCountry;
    }

    /**
     * @param originCountry The origin_country
     */
    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    /**
     * @return The originLat
     */
    public String getOriginLat() {
        return originLat;
    }

    /**
     * @param originLat The origin_lat
     */
    public void setOriginLat(String originLat) {
        this.originLat = originLat;
    }

    /**
     * @return The originLon
     */
    public String getOriginLon() {
        return originLon;
    }

    /**
     * @param originLon The origin_lon
     */
    public void setOriginLon(String originLon) {
        this.originLon = originLon;
    }

    /**
     * @return The destinationAddress
     */
    public String getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * @param destinationAddress The destination_address
     */
    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    /**
     * @return The destinationCountry
     */
    public String getDestinationCountry() {
        return destinationCountry;
    }

    /**
     * @param destinationCountry The destination_country
     */
    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    /**
     * @return The destinationLat
     */
    public String getDestinationLat() {
        return destinationLat;
    }

    /**
     * @param destinationLat The destination_lat
     */
    public void setDestinationLat(String destinationLat) {
        this.destinationLat = destinationLat;
    }

    /**
     * @return The destinationLon
     */
    public String getDestinationLon() {
        return destinationLon;
    }

    /**
     * @param destinationLon The destination_lon
     */
    public void setDestinationLon(String destinationLon) {
        this.destinationLon = destinationLon;
    }

    /**
     * @return The vehicleVesselDescription
     */
    public String getVehicleVesselDescription() {
        return vehicleVesselDescription;
    }

    /**
     * @param vehicleVesselDescription The vehicle_vessel_description
     */
    public void setVehicleVesselDescription(String vehicleVesselDescription) {
        this.vehicleVesselDescription = vehicleVesselDescription;
    }

    /**
     * @return The vehicleVesselLicenseNumber
     */
    public String getVehicleVesselLicenseNumber() {
        return vehicleVesselLicenseNumber;
    }

    /**
     * @param vehicleVesselLicenseNumber The vehicle_vessel_license_number
     */
    public void setVehicleVesselLicenseNumber(String vehicleVesselLicenseNumber) {
        this.vehicleVesselLicenseNumber = vehicleVesselLicenseNumber;
    }

    /**
     * @return The vesselName
     */
    public String getVesselName() {
        return vesselName;
    }

    /**
     * @param vesselName The vessel_name
     */
    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    /**
     * @return The shareWith
     */
    public String getShareWith() {
        return shareWith;
    }

    /**
     * @param shareWith The share_with
     */
    public void setShareWith(String shareWith) {
        this.shareWith = shareWith;
    }

    /**
     * @return The syndicate
     */
    public String getSyndicate() {
        return syndicate;
    }

    /**
     * @param syndicate The syndicate
     */
    public void setSyndicate(String syndicate) {
        this.syndicate = syndicate;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy The created_by
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return The createdDate
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate The created_date
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return The updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy The updated_by
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return The updatedDate
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * @param updatedDate The updated_date
     */
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * @return The image
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image The image
     */
    public void setImage(String image) {
        this.image = image;
    }

}

