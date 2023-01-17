package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Groundstation {

    private Connection connection;
    private ArrayList<Robot> robots = new ArrayList<>();
    private Groundstation(){
        String url = "jdbc:mysql://localhost:3306";
        String username = "root";
        String password = "";
        System.out.println("Connecting database...");
        try{
            connection = DriverManager.getConnection(url, username, password)
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE `exoplanet`.`robot`");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }
    public static void main(String[] args) {
        new Groundstation();
    }
}
