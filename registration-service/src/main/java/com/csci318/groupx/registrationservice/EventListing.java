package com.csci318.groupx.registrationservice;

public record EventListing(
        int id,
        String title,
        String type,
        String location,
        String price,
        String size,
        String description,
        String imageLocation,
        Integer ownerId,
        Double latitude,
        Double longitude,
        String status
) {
    public EventListing(
            int id, String title, String type, String location, String price, String size,
            String description, String imageLocation, Integer ownerId, Double latitude, Double longitude
    ) {
        this(id, title, type, location, price, size, description, imageLocation,
                ownerId, latitude, longitude, "SCHEDULED");
    }

    public EventListing(String title, String type, String location, String price, String size, String description, String imageLocation)
    {
        this(0, title, type, location, price, size, description, imageLocation, null, null, null, "SCHEDULED");
    }

    public EventListing(String title, String type, String location, String price, String size,
                        String description, String imageLocation, Integer ownerId,
                        Double latitude, Double longitude) {
        this(0, title, type, location, price, size, description, imageLocation, ownerId, latitude, longitude, "SCHEDULED");
    }
}