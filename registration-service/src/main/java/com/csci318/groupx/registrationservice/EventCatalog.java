package com.csci318.groupx.registrationservice;

import java.util.ArrayList;
import java.util.List;

final class EventCatalog {
    private static final List<EventListing> EVENTS = new ArrayList<>(List.of(
            new EventListing(
                    "Radiohead concert",
                    "Concert",
                    "Afterpay Arena",
                    "$50",
                    "Large",
                    "Radiohead's latest tour to Australia starts in Sydney.",
                    "/Images/394f75e3cb710d620129ea9fef2754f83b5bc54b8e8f4ffcfc2badd46c297059.jpeg"),
            new EventListing(
                    "Nightclub",
                    "Nightclub",
                    "Wollongong Central",
                    "$35",
                    "Large",
                    "An 18+ nightclub with loud DJ music and alcohol.",
                    "/Images/Wikipedia_space_ibiza(03).jpg"),
            new EventListing(
                    "Community barbecue",
                    "Community",
                    "Sydney Central Park",
                    "$15",
                    "Medium",
                    "Fun lunch for family and friends. All food and refreshments are included with your ticket price entry.",
                    "/Images/park-art-P1020661.jpg"),
            new EventListing(
                    "Bingo night",
                    "Games",
                    "Wollongong Community Hall",
                    "Free",
                    "Small",
                    "A classic bingo night for everyone to enjoy.",
                    "/Images/Bingo_night.jpg")
    ));

    private EventCatalog() {
    }

    static List<EventListing> getEvents() {
        return List.copyOf(EVENTS);
    }

    static void add(EventListing event) {
        EVENTS.add(event);
    }
}
