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
        Integer ownerId
) {
    public EventListing(String title, String type, String location, String price, String size, String description, String imageLocation)
    {
        this(0, title, type, location, price, size, description, imageLocation, null);
    }
}