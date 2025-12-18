package org.example;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        DBConnection db = new DBConnection();
        try (Connection conn = db.getDBConnection()) {
            System.out.println("✅ Connected to the database!");
            if (conn.isValid(2)) {
                System.out.println("Connection is valid.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database connection failed", e);
        }
    }
}
