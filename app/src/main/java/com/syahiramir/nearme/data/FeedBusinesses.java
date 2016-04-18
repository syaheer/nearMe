package com.syahiramir.nearme.data;

public class FeedBusinesses {
    private String name, distance, imageURL, isClosed, category;
    private Double longitude, latitude;

    public FeedBusinesses(){
    }

    public FeedBusinesses(String name, String distance, String imageURL, String isClosed, String category, Double longitude, Double latitude) {
        this.name = name;
        this.distance = distance;
        this.imageURL = imageURL;
        this.isClosed = isClosed;
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(String isClosed) {
        this.isClosed = isClosed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
