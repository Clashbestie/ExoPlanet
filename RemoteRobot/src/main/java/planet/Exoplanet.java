package planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import position.Direction;
import position.Position;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Exoplanet implements Runnable
{

    private static Exoplanet INSTANCE;
    public static Exoplanet INSTANCE(){
        return INSTANCE;
    }

    private HashMap<Position, Measure> data = new HashMap<>();

    private int height, width;
    private Gson gson;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ArrayList<ServerCommandsListener> listeners = new ArrayList<>();

    public Exoplanet()
    {
        INSTANCE = this;
        GsonBuilder builder = new GsonBuilder();

        gson = builder.create();

        try
        {
            Socket socket = new Socket("localhost", 8150);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this).start();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addData(Position position, Measure measure){
        data.put(position, measure);
    }

    public Measure getData(Position position){
        if(position.getX() < 0 || position.getX() >= height || position.getY() < 0 || position.getY() >= width) return new Measure(Ground.OOB, 0);
        return data.containsKey(position)? data.get(position): new Measure(Ground.NOTHING, -999.9);
    }

    public void c2sOrbit(String name)
    {
        write("{\"CMD\":\"orbit\",\"NAME\":\"" + name + "\"}");
    }

    public void c2sLand(Position position)
    {
        write("{\"CMD\":\"land\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}}");
    }

    public void c2sScan()
    {
        write("{\"CMD\":\"scan\"}");
    }

    public void c2sMove()
    {
        write("{\"CMD\":\"move\"}");
    }

    public void c2sRotate(boolean right)
    {
        write("{\"CMD\":\"rotate\",\"ROTATION\":\"" + (right ? "RIGHT" : "LEFT") + "\"}");
    }

    public void c2sExit()
    {
        write("{\"CMD\":\"exit\"}");
    }

    public void c2sGetPos()
    {
        write("{\"CMD\":\"getpos\"}");
    }

    public void c2sCharge(int duration)
    {
        write("{\"CMD\":\"charge\",\"DURATION\":\"" + duration + "\"}");
    }

    private void write(String data)
    {
        try
        {
            System.out.println("Write-> " + data);
            writer.write(data);
            writer.newLine();
            writer.flush();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        try
        {
            String command;
            while (true)
            {
                command = reader.readLine();
                if (command != null) System.out.println("Read-> " + command);

                HashMap<?, ?> data = gson.fromJson(command, HashMap.class);

                switch (getType(data))
                {
                    case INIT:
                        int width = ((Double) ((LinkedTreeMap<?, ?>) data.get("SIZE")).get("WIDTH")).intValue();
                        int height = ((Double) ((LinkedTreeMap<?, ?>) data.get("SIZE")).get("HEIGHT")).intValue();
                        this.width = width;
                        this.height = height;
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cInit(width, height);
                        }
                        break;
                    case LANDED:
                        LinkedTreeMap<?, ?> _measure = ((LinkedTreeMap<?, ?>) data.get("MEASURE"));
                        Ground ground = Ground.valueOf(_measure.get("GROUND").toString());
                        double temp = Double.parseDouble(((LinkedTreeMap<?, ?>) data.get("MEASURE")).get("TEMP").toString());
                        Measure measure = new Measure(ground, temp);
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cLanded(measure);
                        }
                        break;
                    case SCANED:
                        _measure = ((LinkedTreeMap<?, ?>) data.get("MEASURE"));
                        ground = Ground.valueOf(_measure.get("GROUND").toString());
                        temp = Double.parseDouble(((LinkedTreeMap<?, ?>) data.get("MEASURE")).get("TEMP").toString());
                        measure = new Measure(ground, temp);
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cScanned(measure);
                        }
                        break;
                    case MOVED:
                        LinkedTreeMap<?, ?> _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        int x = ((Double) _position.get("X")).intValue();
                        int y = ((Double) _position.get("Y")).intValue();
                        Direction direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        Position position = new Position(x, y, direction);
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cMoved(position);
                        }
                        break;
                    case ROTATED:
                        direction = Direction.valueOf(data.get("DIRECTION").toString());
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cRotated(direction);
                        }
                        break;
                    case CRASHED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cCrashed();
                        }
                        break;
                    case ERROR:
                        String text = data.get("ERROR").toString();
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cError(text);
                        }
                        break;
                    case POS:
                        _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        x = ((Double) _position.get("X")).intValue();
                        y = ((Double) _position.get("Y")).intValue();
                        direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        position = new Position(x, y, direction);
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cPos(position);
                        }
                        break;
                    case CHARGED:
                        LinkedTreeMap<?, ?> _status = (LinkedTreeMap<?, ?>) data.get("STATUS");
                        temp = Double.parseDouble((String) _status.get("TEMP"));
                        int energy = ((Double) _status.get("ENERGY")).intValue();
                        text = _status.get("MESSAGE").toString();
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cCharged(temp, energy, text);
                        }
                        break;
                    case STATUS:
                        _status = (LinkedTreeMap<?, ?>) data.get("STATUS");
                        temp = Double.parseDouble((String) _status.get("TEMP"));
                        energy = ((Double) _status.get("ENERGY")).intValue();
                        text = _status.get("MESSAGE").toString();
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cStatus(temp, energy, text);
                        }
                        break;
                    case UNKNOWN:
                        break;
                }
            }

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ServerCommandType getType(HashMap<?, ?> data)
    {
        if (data == null || !data.containsKey("CMD")) return ServerCommandType.UNKNOWN;
        try
        {
            return ServerCommandType.valueOf(((String) data.get("CMD")).toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return ServerCommandType.UNKNOWN;
        }
    }

    public void addListener(ServerCommandsListener listener)
    {
        listeners.add(listener);
    }
}
