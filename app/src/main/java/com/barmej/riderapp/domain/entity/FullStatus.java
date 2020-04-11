package com.barmej.riderapp.domain.entity;

import java.io.Serializable;

public class FullStatus implements Serializable {
    private Driver driver;
    private Rider rider;
    private Trip trip;

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}
