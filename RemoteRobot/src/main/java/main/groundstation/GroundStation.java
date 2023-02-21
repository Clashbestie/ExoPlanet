package main.groundstation;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import main.RemoteRobot;
import main.planet.Exoplanet;
import main.planet.Ground;
import main.planet.Measure;
import main.position.Coordinate;
import main.position.Direction;
import main.position.Position;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class GroundStation implements Runnable
{

    private final BufferedWriter writer;
    private final BufferedReader reader;
    private HashMap<Integer, Coordinate> robots;
    private static final Gson gson = new Gson();

    public GroundStation()
    {
        robots = new HashMap<>();
        try
        {
            Socket socket = new Socket("localhost", 8152);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this).start();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void updateRobotPosition(int id, Coordinate coordinate){
        if(coordinate.equals(new Coordinate(-1,-1)))
            robots.remove(id);
        robots.put(id, coordinate);
    }

    public boolean isOccupied(Coordinate coordinate){
        return robots.containsValue(coordinate);
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                String message = reader.readLine();
                if (message == null) continue;
                HashMap<?, ?> data = gson.fromJson(message, HashMap.class);
                switch (data.get("CMD").toString())
                {
                    case "updateposition":
                        LinkedTreeMap<?, ?> _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        int x = ((Double) _position.get("X")).intValue();
                        int y = ((Double) _position.get("Y")).intValue();
                        Coordinate coordinate = new Coordinate(x,y);
                        int id = Integer.valueOf(data.get("ID").toString());
                        updateRobotPosition(id, coordinate);
                        break;
                    case "updatedata":
                        _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        x = ((Double) _position.get("X")).intValue();
                        y = ((Double) _position.get("Y")).intValue();
                        coordinate = new Coordinate(x,y);
                        double temp = Double.parseDouble(data.get("TEMP").toString());
                        Ground ground = Ground.valueOf(data.get("GROUND").toString());
                       RemoteRobot.INSTANCE().Exoplanet().addData(coordinate, new Measure(ground,temp));
                        break;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void sendPos(String name, Position position, int energy, double temp, String status)
    {
        write("{\"CMD\":\"updateposition\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}," +
                "\"NAME\":\"" + name +"\"," +
                "\"ENERGY\":\"" + energy +"\"," +
                "\"TEMP\":\"" + temp +"\"," +
                "\"STATUS\":\"" + status +"\"}");
    }

    public void sendData(Coordinate coordinate, Measure measure){
        write("{\"CMD\":\"updatedata\", \"POSITION\":{\"X\":" + coordinate.X() + ",\"Y\":" + coordinate.Y() + ",\"DIRECTION\":\"" + Direction.NORTH + "\"}," +
                "\"GROUND\":\"" + measure.ground +"\"," +
                "\"TEMP\":\"" + measure.temp +"\"}");
    }


    public void sendDestroy()
    {
        write("{\"CMD\":\"destroy\"}");
    }

    private void write(String message)
    {
        try
        {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
