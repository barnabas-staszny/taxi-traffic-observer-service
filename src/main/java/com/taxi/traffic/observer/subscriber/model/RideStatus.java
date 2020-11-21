package com.taxi.traffic.observer.subscriber.model;

public enum RideStatus {
    PICKUP,
    DROPOFF,
    ENROUTE,
    UNKNOWN;

    public static RideStatus fromString(String status) {
        for (RideStatus rideStatus : RideStatus.values()) {
            if (rideStatus.name().equalsIgnoreCase(status)) {
                return rideStatus;
            }
        }
        return UNKNOWN;
    }
}
