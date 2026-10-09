package com.csci318.groupx.registrationservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// The app's single source of events. It now gets them from the event service.
final class EventCatalog {
    private static final String DEFAULT_IMAGE = "/Images/event-ideas-for-party-eventbookings.jpeg";
    private static final long REFRESH_MS = 5000;

    private static List<EventListing> cache = List.of();
    private static long loadedAt = 0;
    private static boolean reachable = true;

    private EventCatalog() {
    }

    // Returns the events, re-fetching from the service at most every 5 seconds
    static synchronized List<EventListing> getEvents() {
        if (System.currentTimeMillis() - loadedAt > REFRESH_MS) {
            refresh();
        }
        return cache;
    }

    // False if the last attempt to reach the event service failed
    static synchronized boolean isServiceReachable() {
        return reachable;
    }

    private static void refresh() {
        try {
            List<EventListing> loaded = new ArrayList<>();
            for (EventServiceClient.ServiceEvent event : EventServiceClient.getAll()) {
                loaded.add(toListing(event));
            }
            cache = List.copyOf(loaded);
            reachable = true;
        } catch (RuntimeException e) {
            reachable = false;
            System.err.println("Could not reach the event service: " + e.getMessage());
        }
        loadedAt = System.currentTimeMillis();
    }

    static synchronized EventListing add(EventListing listing) {
        Long ownerId = CurrentUserSession.getAccountId();

        if (ownerId == null || !CurrentUserSession.isManager()) {
            throw new IllegalStateException(
                    "Only logged-in event managers can create events."
            );
        }

        EventServiceClient.ServiceEvent toSend = new EventServiceClient.ServiceEvent(
                null,
                listing.title(),
                listing.description(),
                listing.type(),
                listing.location(),
                LocalDateTime.now().plusDays(7).withHour(18).withMinute(0).withSecond(0).withNano(0),
                null,
                parsePrice(listing.price()),
                capacityFromSize(listing.size()),
                0,
                ownerId,
                listing.imageLocation(),
                listing.latitude(),
                listing.longitude()
        );

        EventServiceClient.ServiceEvent saved =
                EventServiceClient.create(toSend);

        refresh();

        return toListing(saved);
    }

    // Only the manager who created an event can delete it
    static synchronized boolean delete(EventListing listing) {
        Long accountId = CurrentUserSession.getAccountId();

        if (listing == null
                || accountId == null
                || !CurrentUserSession.isManager()
                || listing.ownerId() == null
                || !accountId.equals(listing.ownerId().longValue())) {
            return false;
        }

        EventServiceClient.delete(listing.id());

        refresh();

        return true;
    }

    private static EventListing toListing(EventServiceClient.ServiceEvent event) {
        Integer ownerId = event.ownerId() == null
                ? null
                : Integer.valueOf(event.ownerId().intValue());

        String image = (event.imageLocation() == null || event.imageLocation().isBlank())
                ? DEFAULT_IMAGE
                : event.imageLocation();

        return new EventListing(
                event.id() == null ? 0 : event.id().intValue(),
                event.name(),
                event.category(),
                event.location(),
                formatPrice(event.price() == null ? 0 : event.price()),
                sizeLabel(event.capacity()),
                event.description() == null ? "" : event.description(),
                image,
                ownerId,
                event.latitude(),
                event.longitude()
        );
    }

    private static String formatPrice(double price) {
        if (price == 0) {
            return "Free";
        }

        return price % 1 == 0
                ? String.format("$%d", (long) price)
                : String.format("$%.2f", price);
    }

    static double parsePrice(String text) {
        if (text == null) {
            return 0;
        }

        String cleaned = text.replace("$", "").replace(",", "").trim();

        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("free")) {
            return 0;
        }

        double value = Double.parseDouble(cleaned);

        if (value < 0) {
            throw new NumberFormatException("Price can't be negative");
        }

        return value;
    }

    // The service stores a number; the pages show Small, Medium or Large
    private static String sizeLabel(Integer capacity) {
        if (capacity == null) {
            return "Not specified";
        }

        if (capacity <= 50) {
            return "Small";
        }

        if (capacity <= 500) {
            return "Medium";
        }

        return "Large";
    }

    // The Create form's size box can hold a number or the words small, medium or large
    private static Integer capacityFromSize(String size) {
        if (size == null || size.isBlank()) {
            return null;
        }

        String text = size.trim();

        try {
            return Integer.parseInt(text.replace(",", ""));
        } catch (NumberFormatException ignored) {
            // Not a number, so try the words below.
        }

        return switch (text.toLowerCase()) {
            case "small" -> 50;
            case "medium" -> 200;
            case "large" -> 1000;
            default -> null;
        };
    }
}