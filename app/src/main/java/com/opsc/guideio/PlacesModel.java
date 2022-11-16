package com.opsc.guideio;

public class PlacesModel {
    // Variables
    String id, uid, url, x, y, timestamp;

    public PlacesModel() {
    }

    public PlacesModel(String id, String uid, String url, String x, String y, String timestamp) {
        this.id = id;
        this.uid = uid;
        this.url = url;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
