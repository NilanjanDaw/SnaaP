package com.example.nilanjandaw.snaap;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Date;

/**
 * Created by Nilanjan Daw on 11/04/2016.
 * Project: SnaaP Project
 */
public class Packet {

    public String tagID, timeStamp, macID, bandID;
    Coordinate coordinate;

    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMacID() {
        return macID;
    }

    public void setMacID(String macID) {
        this.macID = macID;
    }

    public String getBandID() {
        return bandID;
    }

    public void setBandID(String bandID) {
        this.bandID = bandID;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
    class Coordinate {
        double latitude, longitude;
    }
}
