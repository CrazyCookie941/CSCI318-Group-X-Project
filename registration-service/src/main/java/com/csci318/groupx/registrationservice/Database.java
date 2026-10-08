package com.csci318.groupx.registrationservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class Database {
    private static final String URL = "jdbc:h2:file:../Data/events;AUTO_SERVER=TRUE";

    private Database() {}

    static Connection connect() throws SQLException { return DriverManager.getConnection(URL, "sa", ""); }
}