package com.matti.finalproject;

public class Marker {
    // Member variables representing the title and information about the sport.
    private final String title;
    private final String snippet;
    private final String latitude;
    private final String longitude;
    private final String mode;

    public Marker(String title, String snippet, String latitude, String longitude, String mode) {
        this.title = title;
        this.snippet = snippet;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mode = mode;
    }

    String getTitle() {
        return title;
    }
    String getSnippet() {
        return snippet;
    }
    String getLatitude(){return latitude;}
    String getLongitude(){return longitude;}
    String getMode(){return mode;}

}
