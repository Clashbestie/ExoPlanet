package main.groundstation;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import main.planet.Exoplanet;
import planet.Ground;
import planet.Measure;
import position.Coordinate;
import position.Position;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class GroundStation implements Runnable {

    private static final BufferedWriter writer;
    private static final BufferedReader reader;
    private static final HashMap<Integer, Coordinate> robots;
    private static final Gson gson = new Gson();

    static {
        robots = new HashMap<>();
        try {
            Socket socket = new Socket("localhost", 8152);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new GroundStation()).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateRobotPosition(int id, Coordinate coordinate) {
        if (coordinate.equals(new Coordinate(-1, -1)))
            robots.remove(id);
        robots.put(id, coordinate);
    }

    public static boolean isOccupied(Coordinate coordinate) {
        return robots.containsValue(coordinate);
    }

    @Override
    public void run() {
        sendGetData();
        while (true) {
            try {
                String message = reader.readLine();
                if (message == null) continue;
                HashMap<?, ?> data = gson.fromJson(message, HashMap.class);
                switch (data.get("CMD").toString()) {
                    case "updateposition":
                        LinkedTreeMap<?, ?> _map = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        int x = ((Double) _map.get("X")).intValue();
                        int y = ((Double) _map.get("Y")).intValue();
                        Coordinate coordinate = new Coordinate(x, y);
                        int id = Integer.parseInt(data.get("ID").toString());
                        updateRobotPosition(id, coordinate);
                        break;
                    case "updatedata":
                        _map = (LinkedTreeMap<?, ?>) data.get("COORDINATE");
                        x = ((Double) _map.get("X")).intValue();
                        y = ((Double) _map.get("Y")).intValue();
                        coordinate = new Coordinate(x, y);
                        double temp = Double.parseDouble(data.get("TEMP").toString());
                        Ground ground = Ground.valueOf(data.get("GROUND").toString());
                        Exoplanet.addData(coordinate, new Measure(ground, temp));
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendColoring(Coordinate coordinate, int side, Color color) {
        if (side == 4) side++;
        write("{\"CMD\":\"updateColoring\", \"COORDINATE\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + "}," +
                "\"COLOR\":{\"R\":" + color.getRed() + ",\"G\":" + color.getGreen() + ",\"B\":" + color.getBlue() + ",\"A\":" + color.getAlpha() + "}," +
                "\"SIDE\":\"" + side + "\"}");
    }

    public static void sendScore(Coordinate coordinate, String score){
        write("{\"CMD\":\"updateScore\", \"COORDINATE\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + "}," +
                "\"SCORE\":\"" + score + "\"}");
    }

    public static void resetColoring() {
        write("{\"CMD\":\"resetcoloring\"}");
    }

    public static void sendPos(String name, Position position, int energy, double temp, String status) {
        write("{\"CMD\":\"updateposition\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}," +
                "\"NAME\":\"" + name + "\"," +
                "\"ENERGY\":\"" + energy + "\"," +
                "\"TEMP\":\"" + temp + "\"," +
                "\"STATUS\":\"" + status + "\"}");
    }

    public static void sendData(Coordinate coordinate, Measure measure) {
        write("{\"CMD\":\"updatedata\", \"COORDINATE\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + "}," +
                "\"GROUND\":\"" + measure.ground + "\"," +
                "\"TEMP\":\"" + measure.temp + "\"}");
    }

    public void sendGetData() {
        write("{\"CMD\":\"getdata\"}");
    }


    public static void sendDestroy() {
        write("{\"CMD\":\"destroy\"}");
    }

    private static void write(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
