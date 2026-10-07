package com.csci318.groupx.registrationservice;

public record EventListing(
        String title,
        String type,
        String location,
        String price,
        String size,
        String description,
        String imageLocation
) {
}
