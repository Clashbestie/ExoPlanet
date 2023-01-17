package org.example;

import org.example.position.Position;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Groundstation
{

    private ServerSocket socket;
    private Connection connection;
    private ArrayList<Robot> robots = new ArrayList<>();

    private Groundstation()
    {
        String url = "jdbc:mysql://localhost:3306/exoplanet";
        String username = "root";
        String password = "";
        try
        {
            connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE `robot`");

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try
        {
            socket = new ServerSocket(8152);

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sendRobotPosition(int id, Position position)
    {
        for (Robot robot : robots)
        {
            robot.sendRobotPosition(id, position);
        }
    }

    public static void main(String[] args)
    {
        new Groundstation();
    }
}
