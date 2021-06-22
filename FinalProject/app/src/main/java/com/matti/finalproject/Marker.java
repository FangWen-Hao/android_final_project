package com.matti.finalproject;

public class Marker {
    // Member variables representing the title and information about the sport.
    private String title;
    private String snippet;
    private String latitude;
    private String longitude;

    public Marker(String title, String snippet, String latitude, String longitude) {
        this.title = title;
        this.snippet = snippet;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    String getTitle() {
        return title;
    }
    String getSnippet() {
        return snippet;
    }
    String getLatitude(){return latitude;}
    String getLongitude(){return longitude;}

}
