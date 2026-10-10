package com.csci318.groupx.registrationservice;

import java.time.LocalDateTime;

// One-time helper: adds the four sample events to an empty event service (owned by user 2, the test manager)
final class InitialiseEvents {

    private InitialiseEvents() {
    }

    private static void seed(String name, String description, String category, String location,
                             LocalDateTime start, double price, int capacity, String image,
                             Double latitude, Double longitude) {
        EventServiceClient.create(new EventServiceClient.ServiceEvent(
                null, name, description, category, location, start, null, price, capacity, 0,
                2L, image, latitude, longitude));
        System.out.println("Added " + name);
    }

    public static void main(String[] args) {
        if (!EventServiceClient.getAll().isEmpty()) {
            System.out.println("The event service already has events, so nothing was added.");
            return;
        }
        seed("Radiohead concert", "Radiohead's latest tour to Australia starts in Sydney.",
                "Concert", "Afterpay Arena", LocalDateTime.of(2026, 12, 12, 19, 30), 50, 20000,
                "/Images/394f75e3cb710d620129ea9fef2754f83b5bc54b8e8f4ffcfc2badd46c297059.jpeg",
                -33.8442890, 151.0622385);
        seed("Nightclub", "An 18+ nightclub with loud DJ music and alcohol.",
                "Nightclub", "Wollongong Central", LocalDateTime.of(2026, 11, 14, 22, 0), 35, 1500,
                "/Images/Wikipedia_space_ibiza(03).jpg", -34.4245774, 150.8941979);
        seed("Community barbecue", "Fun lunch for family and friends. All food and refreshments are included with your ticket price entry.",
                "Community", "Sydney Central Park", LocalDateTime.of(2026, 11, 1, 12, 0), 15, 150,
                "/Images/park-art-P1020661.jpg", null, null);
        seed("Bingo night", "A classic bingo night for everyone to enjoy.",
                "Games", "Wollongong Community Hall", LocalDateTime.of(2026, 10, 30, 18, 30), 0, 40,
                "/Images/Bingo_night.jpg", null, null);
    }
}