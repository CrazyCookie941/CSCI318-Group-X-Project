package com.csci318.groupx.registrationservice;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;


final class EventCatalog {
    private static final String DB_URL = "jdbc:sqlite:../Data/events.db";

    private static final List<EventListing> EVENTS = new ArrayList<>(loadEvents());

    private EventCatalog() {
    }

    private static List<EventListing> loadEvents()
    {
        List<EventListing> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM events ORDER BY id")) {
            while (rs.next())
            {
                list.add(new EventListing(
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("location"),
                        formatPrice(rs.getDouble("price")),
                        rs.getString("size"),
                        rs.getString("description"),
                        rs.getString("image_location")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load events from database", e);
        }
        return list;
    }

    private static String formatPrice(double price) {
        if (price == 0) {
            return "Free";
        }
        return price % 1 == 0
                ? String.format("$%d", (long) price)
                : String.format("$%.2f", price);
    }

    static List<EventListing> getEvents() {
        return List.copyOf(EVENTS);
    }

    static void add(EventListing event) {
        EVENTS.add(event);
    }
}