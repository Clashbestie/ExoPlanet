package main.planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import main.Args;
import main.AutoScout;
import main.RemoteRobot;
import main.ResettableCountDownLatch;
import main.aStar.Tile;
import main.groundstation.GroundStation;
import planet.Ground;
import planet.Measure;
import position.Coordinate;
import position.Direction;
import position.Position;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Exoplanet implements Runnable {

    private static final Gson gson;

    private static final HashMap<Coordinate, Tile> data = new HashMap<>();
    private static int height, width;

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    private static final BufferedWriter writer;
    private static final BufferedReader reader;
    private static final ArrayList<ServerCommandsListener> listeners = new ArrayList<>();
    private static final ResettableCountDownLatch latch = new ResettableCountDownLatch(1);

    public static boolean await(int timeout) {
        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            latch.reset();
            return false;
        }
        latch.reset();
        return true;
    }

    public static void await() {
        await(5000);
    }

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();

        try {
            Socket socket = new Socket("localhost", 8150);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new Exoplanet()).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Coordinate getSaveCoordinate(){
        for (Tile tile: data.values()) {
            Measure measure = tile.getMeasure();
            if(!GroundStation.isOccupied(tile.getCoordinate()) && AutoScout.neutralTargets.contains(measure.ground) && (measure.temp > Args.normalMinTemp && measure.temp < Args.normalMaxTemp)) return  tile.getCoordinate();
        }
        return null;
    }

    public static void addData(Coordinate coordinate, Measure measure) {
        data.put(coordinate, new Tile(coordinate, measure));
    }

    public static Measure getData(Coordinate coordinate) {
        if (OOB(coordinate)) return new Measure(Ground.OOB, 0);
        return data.containsKey(coordinate) ? data.get(coordinate).getMeasure() : new Measure(Ground.NOTHING, -999.9);
    }

    private static boolean OOB(Coordinate coordinate) {
        return (coordinate.X() < 0 || coordinate.X() >= width || coordinate.Y() < 0 || coordinate.Y() >= height);
    }

    public static Tile getTile(Coordinate coordinate) {
        return new Tile(coordinate, getData(coordinate));
    }

    public static void c2sOrbit(String name) {
        write("{\"CMD\":\"orbit\",\"NAME\":\"" + name + "\"}");
    }

    public static boolean c2sLand(Position position) {
        if (GroundStation.isOccupied(position.getCoordinate()) || OOB(position.getCoordinate())) return false;
        write("{\"CMD\":\"land\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}}");
        return true;
    }

    public static void c2sScan() {
        write("{\"CMD\":\"scan\"}");
    }

    public static boolean c2sMove() {
        if (GroundStation.isOccupied(RemoteRobot.getPosition().facing().getCoordinate())) return false;
        write("{\"CMD\":\"move\"}");
        return true;
    }

    public static void c2sRotate(boolean right) {
        write("{\"CMD\":\"rotate\",\"ROTATION\":\"" + (right ? "RIGHT" : "LEFT") + "\"}");
    }

    public static void c2sExit() {
        write("{\"CMD\":\"exit\"}");
    }

    public static void c2sGetPos() {
        write("{\"CMD\":\"getpos\"}");
    }

    public static void c2sCharge(int duration) {
        write("{\"CMD\":\"charge\",\"DURATION\":" + duration + "}");
    }

    private static void write(String data) {
        try {
            System.out.println("Write-> " + data);
            writer.write(data);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            String command;
            boolean status;
            while (true) {
                command = reader.readLine();
                if (command != null) System.out.println("Read-> " + command);

                HashMap<?, ?> data = gson.fromJson(command, HashMap.class);
                status = false;
                switch (getType(data)) {
                    case INIT:
                        int width = ((Double) ((LinkedTreeMap<?, ?>) data.get("SIZE")).get("WIDTH")).intValue();
                        int height = ((Double) ((LinkedTreeMap<?, ?>) data.get("SIZE")).get("HEIGHT")).intValue();
                        Exoplanet.width = width;
                        Exoplanet.height = height;
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cInit(width, height);
                        }
                        break;
                    case LANDED:
                        LinkedTreeMap<?, ?> _measure = ((LinkedTreeMap<?, ?>) data.get("MEASURE"));
                        Ground ground = Ground.valueOf(_measure.get("GROUND").toString());
                        double temp = Double.parseDouble(((LinkedTreeMap<?, ?>) data.get("MEASURE")).get("TEMP").toString());
                        Measure measure = new Measure(ground, temp);
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cLanded(measure);
                        }
                        break;
                    case SCANED:
                        _measure = ((LinkedTreeMap<?, ?>) data.get("MEASURE"));
                        ground = Ground.valueOf(_measure.get("GROUND").toString());
                        temp = Double.parseDouble(((LinkedTreeMap<?, ?>) data.get("MEASURE")).get("TEMP").toString());
                        measure = new Measure(ground, temp);
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cScanned(measure);
                        }
                        break;
                    case MOVED:
                        LinkedTreeMap<?, ?> _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        int x = ((Double) _position.get("X")).intValue();
                        int y = ((Double) _position.get("Y")).intValue();
                        Direction direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        Position position = new Position(x, y, direction);
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cMoved(position);
                        }
                        break;
                    case ROTATED:
                        direction = Direction.valueOf(data.get("DIRECTION").toString());
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cRotated(direction);
                        }
                        break;
                    case CRASHED:
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cCrashed();
                        }
                        break;
                    case ERROR:
                        String text = data.get("ERROR").toString();
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cError(text);
                        }
                        break;
                    case POS:
                        _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        x = ((Double) _position.get("X")).intValue();
                        y = ((Double) _position.get("Y")).intValue();
                        direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        position = new Position(x, y, direction);
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cPos(position);
                        }
                        break;
                    case CHARGED:
                        LinkedTreeMap<?, ?> _status = (LinkedTreeMap<?, ?>) data.get("STATUS");
                        temp = Double.parseDouble(_status.get("TEMP").toString());
                        int energy = ((Double) _status.get("ENERGY")).intValue();
                        text = _status.get("MESSAGE").toString();
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cCharged(temp, energy, text);
                        }
                        break;
                    case STATUS:
                        status = true;
                        _status = (LinkedTreeMap<?, ?>) data.get("STATUS");
                        temp = Double.parseDouble(_status.get("TEMP").toString());
                        energy = ((Double) _status.get("ENERGY")).intValue();
                        text = _status.get("MESSAGE").toString();
                        for (ServerCommandsListener listener : listeners) {
                            listener.s2cStatus(temp, energy, text);
                        }
                        break;
                    case UNKNOWN:
                        break;
                }
                if(!status) latch.countDown();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ServerCommandType getType(HashMap<?, ?> data) {
        if (data == null || !data.containsKey("CMD")) return ServerCommandType.UNKNOWN;
        try {
            return ServerCommandType.valueOf(((String) data.get("CMD")).toUpperCase());
        } catch (IllegalArgumentException e) {
            return ServerCommandType.UNKNOWN;
        }
    }

    public static void addListener(ServerCommandsListener listener) {
        listeners.add(listener);
    }
}
