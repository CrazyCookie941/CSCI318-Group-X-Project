package com.csci318.groupx.registrationservice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class Database {
    private static final String URL = "jdbc:h2:file:../Data/events;AUTO_SERVER=TRUE";
    private static boolean ready = false;

    private Database() {
    }

    static synchronized Connection connect() throws SQLException {
        if (!ready) {
            prepareDatabase();
            ready = true;
        }
        return open();
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    // Builds the tables from schema.sql the first time, on any computer
    private static void prepareDatabase() {
        try {
            Files.createDirectories(Path.of("../Data"));

            try (Connection conn = open();
                 ResultSet tables = conn.getMetaData().getTables(null, "PUBLIC", "USERS", null)) {
                if (tables.next()) {
                    return; // already set up
                }
            }

            try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
                if (in == null) {
                    throw new IllegalStateException("schema.sql was not found in src/main/resources");
                }
                String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                try (Connection conn = open(); Statement st = conn.createStatement()) {
                    st.execute(script);
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Could not set up the database", e);
        }
    }
}