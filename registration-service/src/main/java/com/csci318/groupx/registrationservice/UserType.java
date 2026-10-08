package com.csci318.groupx.registrationservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

final class UserType
{
    private UserType() {}

    static Optional<User> authenticate(String email, String password)
    {
        String sql = "SELECT id, email, display_name, role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("display_name"),
                            Role.valueOf(rs.getString("role"))));
                }
                return Optional.empty();
            }
        }
        catch (SQLException e) { throw new RuntimeException("Login lookup failed", e); }
    }

    static boolean emailExists(String email)
    {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
        catch (SQLException e) { throw new RuntimeException("Email check failed", e); }
    }

    static Optional<User> create(String email, String password, String displayName, Role role)
    {
        String sql = "INSERT INTO users (email, password, display_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, displayName);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (keys.next()) { return Optional.of(new User(keys.getInt(1), email, displayName, role)); }
            }
            return Optional.empty();
        }
        catch (SQLException e) { throw new RuntimeException("Could not create account", e); }
    }

    static String findLocation(int userId)
    {
        String sql = "SELECT location FROM users WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    String location = rs.getString("location");
                    return location == null ? "" : location;
                }
                return "";
            }
        }
        catch (SQLException e) { throw new RuntimeException("Could not load location", e); }
    }

    static boolean emailTakenByOther(String email, int userId)
    {
        String sql = "SELECT 1 FROM users WHERE email = ? AND id <> ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
        catch (SQLException e) { throw new RuntimeException("Email check failed", e); }
    }

    static User updateProfile(User current, String displayName, String email, String location)
    {
        String sql = "UPDATE users SET display_name = ?, email = ?, location = ? WHERE id = ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, displayName);
            ps.setString(2, email);
            ps.setString(3, location);
            ps.setInt(4, current.id());
            ps.executeUpdate();
            return new User(current.id(), email, displayName, current.role());
        }
        catch (SQLException e) { throw new RuntimeException("Could not update account", e); }
    }
}