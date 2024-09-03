package org.freeland.wildscan.models;

import java.util.ArrayList;

public class RegionSpeciesImages {
    private String imagePath, credit, license, imageOrder, defaultOrder = "0";
    private String speciesId;

  /*  private ArrayList<RegionSpeciesImages> imagesArrayList;*/


    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getImageOrder() {
        return imageOrder;
    }

    public void setImageOrder(String imageOrder) {
        this.imageOrder = imageOrder;
    }

    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

   /* public ArrayList<RegionSpeciesImages> getImagesArrayList() {
        return imagesArrayList;
    }

    public void setImagesArrayList(ArrayList<RegionSpeciesImages> imagesArrayList) {
        this.imagesArrayList = imagesArrayList;
    }*/
}