package main;

import planet.Measure;
import position.Coordinate;
import position.Direction;
import position.Position;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Groundstation {

    private static Connection connection;
    private static final ArrayList<Robot> robots = new ArrayList<>();
    private static final HashMap<Coordinate, Measure> data = new HashMap<>();

    public static void main(String[] args) {
        int id = 0;
        String url = "jdbc:mysql://localhost:3306/exoplanet";
        String username = "root";
        String password = "";
        try {
            connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            statement.execute("TRUNCATE `robot`");
            statement = connection.createStatement();
            statement.execute("TRUNCATE `world`");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Thread display = new Thread(() -> {
            try (ServerSocket displaySocket = new ServerSocket(8153)) {
                while (true) {
                    Display.setSocket(displaySocket.accept());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        display.start();

        try (ServerSocket socket = new ServerSocket(8152)) {
            while (true) {
                Robot robot = new Robot(socket.accept(), id);
                robots.add(robot);
                id++;
            }

        } catch (IOException e) {
            display.interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void sendPositions(Robot robot) {
        for (Robot _robot : robots) {
            robot.sendRobotPosition(_robot.getID(), _robot.getPosition());
        }
    }

    public static void sendData(Robot robot) {
        for (Map.Entry<Coordinate, Measure> set : data.entrySet()) {
            robot.sendData(set.getKey(), new Measure(set.getValue().ground, set.getValue().temp));
        }
    }

    public static void sendRobotPosition(int id, String name, Position position, int energy, double temp, String status, boolean insert) {
        try {
            String sql;
            if (insert) {
                sql = "INSERT INTO `robot` (`id`, `name`, `x`, `y`, `direction`, `energy`, `temp`, `status`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
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
            if (!insert) preparedStatement.setString(9, String.valueOf(id));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Robot robot : robots) {
            robot.sendRobotPosition(id, position);
        }
        Display.sendRobotPosition(id, position);
    }

    public static void updateData(Coordinate coordinate, Measure measure) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `world` (`x`, `y`, `ground`, `temp`) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, String.valueOf(coordinate.X()));
            preparedStatement.setString(2, String.valueOf(coordinate.Y()));
            preparedStatement.setString(3, measure.ground.toString());
            preparedStatement.setString(4, String.valueOf(measure.temp));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        data.put(coordinate, measure);
        for (Robot robot : robots) {
            robot.sendData(coordinate, measure);
        }
        Display.sendData(coordinate, measure);
    }

    public static void destroy(int id) {
        robots.removeIf(r -> r.getID() == id);
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `robot` WHERE `robot`.`id` = ?");
            statement.setString(1, String.valueOf(id));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Robot robot : robots) {
            robot.sendRobotPosition(id, new Position(-1, -1, Direction.NORTH));
        }
    }
}
