package org.example;

import org.example.position.Direction;
import org.example.position.Position;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.*;
import java.util.ArrayList;

public class Groundstation
{

    private static Groundstation groundstation;
    public static Groundstation getGroundstation(){
        return groundstation;
    }
    private ServerSocket socket;
    private Connection connection;
    private ArrayList<Robot> robots = new ArrayList<>();

    private Groundstation()
    {
        groundstation = this;
        int id = 0;
        String url = "jdbc:mysql://localhost:3306/exoplanet";
        String username = "root";
        String password = "";
        try
        {
            connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE `robot`");
            statement = connection.createStatement();
            statement.execute("TRUNCATE `world`");

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        try
        {
            socket = new ServerSocket(8152);
            while(true){
                Robot robot = new Robot(socket.accept(), id);
                robots.add(robot);
                id++;
            }

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sendPositions(Robot robot){
        for (Robot _robot : robots)
        {
            robot.sendRobotPosition(_robot.getID(),_robot.getPosition());
        }
    }

    public void sendRobotPosition(int id, String name, Position position, int energy, double temp, String status, boolean insert)
    {
        try {
            String sql;
            if(insert){
                sql = "INSERT INTO `robot` (`id`, `name`, `x`, `y`, `direction`, `energy`, `temp`, `status`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            }else {
                sql = "UPDATE `robot` SET `id` = ?, `name` = ?, `x` = ?, `y` = ?, `direction` = ?, `energy` = ?, `temp` = ?, `status` = ? WHERE `robot`.`id` = ?";
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(id));
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, String.valueOf(position.getX()));
            preparedStatement.setString(4, String.valueOf(position.getY()));
            preparedStatement.setString(5, String.valueOf(position.getDir()));
            preparedStatement.setString(6, String.valueOf(energy));
            preparedStatement.setString(7, String.valueOf(temp));
            preparedStatement.setString(8, status);
            if(!insert)preparedStatement.setString(9, String.valueOf(id));
            //land:POSITION|0|0|EAST
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Robot robot : robots)
        {
            robot.sendRobotPosition(id, position);
        }
    }

    public void updateData(Position position, Ground ground, double temp){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `world` (`x`, `y`, `ground`, `temp`) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, String.valueOf(position.getX()));
            preparedStatement.setString(2, String.valueOf(position.getY()));
            preparedStatement.setString(3, ground.toString());
            preparedStatement.setString(4, String.valueOf(temp));
            preparedStatement.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Robot robot : robots)
        {
            robot.sendData(position, ground, temp);
        }
    }

    public void destroy(int id){
        robots.removeIf(r -> r.getID() == id);
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `robot` WHERE `robot`.`id` = ?");
            statement.setString(1, String.valueOf(id));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Robot robot : robots)
        {
            robot.sendRobotPosition(id, new Position(-1,-1, Direction.NORTH));
        }
    }

    public static void main(String[] args)
    {
        new Groundstation();
    }
}
