package com.csci318.groupx.registrationservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

final class EventCatalog {
    private static final List<EventListing> EVENTS = new ArrayList<>(loadEvents());

    private EventCatalog() {
    }

    private static List<EventListing> loadEvents() {
        List<EventListing> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM events ORDER BY id")) {
            while (rs.next()) {
                int ownerValue = rs.getInt("owner_id");
                Integer ownerId = rs.wasNull() ? null : ownerValue;
                list.add(new EventListing(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("location"),
                        formatPrice(rs.getDouble("price")),
                        rs.getString("size"),
                        rs.getString("description"),
                        rs.getString("image_location"),
                        ownerId));
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
        String sql = "INSERT INTO events (title, type, location, price, size, description, image_location, owner_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        User owner = Session.current();
        Integer ownerId = owner == null ? null : owner.id();

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.title());
            ps.setString(2, event.type());
            ps.setString(3, event.location());
            ps.setDouble(4, parsePrice(event.price()));
            ps.setString(5, event.size());
            ps.setString(6, event.description());
            ps.setString(7, event.imageLocation());
            if (ownerId == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, ownerId);
            }
            ps.executeUpdate();

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }

            EVENTS.add(new EventListing(newId, event.title(), event.type(), event.location(),
                    event.price(), event.size(), event.description(), event.imageLocation(), ownerId));
        } catch (SQLException e) {
            throw new RuntimeException("Could not save event", e);
        }
    }

    static double parsePrice(String text) {
        if (text == null) {
            return 0;
        }
        String cleaned = text.replace("$", "").replace(",", "").trim();
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("free")) {
            return 0;
        }
        double value = Double.parseDouble(cleaned); // throws NumberFormatException if it isn't a number
        if (value < 0) {
            throw new NumberFormatException("Price can't be negative");
        }
        return value;
    }

    static boolean delete(EventListing listing) {
        User user = Session.current();
        if (user == null || listing.ownerId() == null || listing.ownerId() != user.id()) {
            return false;
        }

        String sql = "DELETE FROM events WHERE id = ? AND owner_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listing.id());
            ps.setInt(2, user.id());
            if (ps.executeUpdate() == 0) {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete event", e);
        }

        EVENTS.removeIf(existing -> existing.id() == listing.id());
        return true;
    }
}