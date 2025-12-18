package org.example;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            DBConnection db = new DBConnection();
            Connection conn = db.getDBConnection();
            System.out.println("✅ Connected to the database!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
