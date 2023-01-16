package planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import position.Position;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Exoplanet implements Runnable
{

    private final Gson gson;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final ArrayList<ServerCommandsListener> listeners = new ArrayList<>();

    public Exoplanet()
    {
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

    public void c2sOrbit(String name)
    {
        write("{\"CMD\":\"orbit\",\"NAME\":\"" + name + "\"}");
    }

    public void c2sLand(Position position)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("CMD", "land");
        data.put("POSITION", position);
        write(gson.toJson(data)); //{"CMD":"land", "POSITION":{"X":0,"Y":0,"DIRECTION":"EAST"}}
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
                        int width = ((Double) ((LinkedTreeMap<?,?>)data.get("SIZE")).get("WIDTH")).intValue();
                        int height = ((Double) ((LinkedTreeMap<?,?>)data.get("SIZE")).get("HEIGHT")).intValue();
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cInit(width, height);
                        }
                        break;
                    case LANDED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            Ground ground = Ground.valueOf(((LinkedTreeMap<?,?>)data.get("MEASURE")).get("GROUND").toString());
                            double temp = Double.parseDouble(((LinkedTreeMap<?,?>)data.get("MEASURE")).get("TEMP").toString());
                            listener.s2cLanded(ground, temp);
                        }
                        break;
                    case SCANED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cScanned(data);
                        }
                        break;
                    case MOVED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cMoved(data);
                        }
                        break;
                    case ROTATED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cRotated(data);
                        }
                        break;
                    case CRASHED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cCrashed(data);
                        }
                        break;
                    case ERROR:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cError(data);
                        }
                        break;
                    case POS:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cPos(data);
                        }
                        break;
                    case CHARGED:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cCharged(data);
                        }
                        break;
                    case STATUS:
                        for (ServerCommandsListener listener : listeners)
                        {
                            listener.s2cStatus(data);
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
