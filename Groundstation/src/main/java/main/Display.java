package main;

import planet.Measure;
import position.Coordinate;
import position.Position;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Display {

    private static BufferedWriter writer;

    public static void setSocket(Socket _socket) {
        System.out.println("Display Connected");
        try {
            writer = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendColoring(String data) {
        write(data);
    }

    public static void resetColoring(String data) {
        write(data);
    }

    public static void sendScore(String data) {
        write(data);
    }

    public static void sendRobotPosition(int id, Position position) {
        if (position == null) return;
        write("{\"CMD\":\"updateposition\",\"ID\":\"" + id + "\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}}");
    }

    public static void sendData(Coordinate coordinate, Measure measure) {
        write("{\"CMD\":\"updatedata\", \"COORDINATE\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + "}," +
                "\"GROUND\":\"" + measure.ground + "\"," +
                "\"TEMP\":\"" + measure.temp + "\"}");
    }

    private static void write(String message) {
        if (writer == null) return;
        System.out.println("Display: " + message);
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
