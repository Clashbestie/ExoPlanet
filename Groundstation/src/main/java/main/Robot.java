package main;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import planet.Ground;
import planet.Measure;
import position.Coordinate;
import position.Direction;
import position.Position;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Robot implements Runnable {

    private static final Gson gson = new Gson();
    private final int id;
    public int getID() {
        return id;
    }
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private boolean insert = true;
    private Position position;
    public Position getPosition() {
        return position;
    }

    public Robot(Socket socket, int id) {
        this.id = id;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = reader.readLine();
                if (message == null) continue;
                HashMap<?, ?> data = gson.fromJson(message, HashMap.class);
                switch (data.get("CMD").toString()) {
                    case "getdata":
                        Groundstation.sendPositions(this);
                        Groundstation.sendData(this);
                        break;
                    case "updateposition":
                        LinkedTreeMap<?, ?> _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        int x = ((Double) _position.get("X")).intValue();
                        int y = ((Double) _position.get("Y")).intValue();
                        Direction direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        Position position = new Position(x, y, direction);
                        int energy = Integer.parseInt(data.get("ENERGY").toString());
                        double temp = Double.parseDouble(data.get("TEMP").toString());
                        String status = data.get("STATUS").toString();
                        String name = data.get("NAME").toString();
                        this.position = position;
                        Groundstation.sendRobotPosition(id, name, position, energy, temp, status, insert);
                        insert = false;
                        break;
                    case "updatedata":
                        LinkedTreeMap<?, ?> _coordinate = (LinkedTreeMap<?, ?>) data.get("COORDINATE");
                        x = ((Double) _coordinate.get("X")).intValue();
                        y = ((Double) _coordinate.get("Y")).intValue();
                        Coordinate coordinate = new Coordinate(x, y);
                        temp = Double.parseDouble(data.get("TEMP").toString());
                        Ground ground = Ground.valueOf(data.get("GROUND").toString());
                        Groundstation.updateData(coordinate, new Measure(ground, temp));
                        break;
                    case "destroy":
                        Groundstation.destroy(id);
                        break;
                    case "updateColoring":
                        Display.sendColoring(message);
                        break;
                    case "resetcoloring":
                        Display.resetColoring(message);
                        break;
                    case "updateScore":
                        Display.sendScore(message);
                        break;
                }
            } catch (IOException e) {
                System.out.println("IOException for Robot " + id);
                Groundstation.destroy(id);
                break;
            }
        }
    }

    public void sendRobotPosition(int id, Position position) {
        if (id == this.id || position == null) return;
        write("{\"CMD\":\"updateposition\",\"ID\":\"" + id + "\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}}");
    }

    public void sendData(Coordinate coordinate, Measure measure) {
        write("{\"CMD\":\"updatedata\", \"COORDINATE\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + "}," +
                "\"GROUND\":\"" + measure.ground + "\"," +
                "\"TEMP\":\"" + measure.temp + "\"}");
    }

    private void write(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
