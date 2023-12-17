package com.conectabike;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    private double originLat, originLng, destinationLat, destinationLng;
    private String title, user, date;
    private Long position;
    private List<List<Double>> points;
    private Double distance;
    private ArrayList<ArrayList<String>> message;

    public ArrayList<ArrayList<String>> getMessage() {
        return message;
    }
    public void setMessage(ArrayList<ArrayList<String>> message) {
        this.message = message;
    }
    public Long getPosition() {
        return position;
    }
    public void setPosition(Long position) {
        this.position = position;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public void setPoints(List<List<Double>> points) {
        this.points = points;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getOriginLat() {
        return originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public String getTitle() {
        return title;
    }

}
