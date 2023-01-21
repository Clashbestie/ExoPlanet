package org.example;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.example.position.Direction;
import org.example.position.Position;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Robot implements Runnable
{

    private Gson gson = new Gson();
    private Socket socket;
    private int id;
    public int getID(){
        return id;
    }
    private BufferedWriter writer;
    private BufferedReader reader;

    private boolean insert = true;

    public Robot(Socket socket, int id)
    {
        this.socket = socket;
        this.id = id;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this).start();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
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
                        Direction direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        Position position = new Position(x, y, direction);
                        int energy = Integer.parseInt(data.get("ENERGY").toString());
                        double temp = Double.parseDouble(data.get("TEMP").toString());
                        String status = data.get("STATUS").toString();
                        String name = data.get("NAME").toString();
                        Groundstation.getGroundstation().sendRobotPosition(id, name, position, energy, temp, status, insert);
                        insert = false;
                        break;
                    case "updatedata":
                        _position = (LinkedTreeMap<?, ?>) data.get("POSITION");
                        x = ((Double) _position.get("X")).intValue();
                        y = ((Double) _position.get("Y")).intValue();
                        direction = Direction.valueOf(_position.get("DIRECTION").toString());
                        position = new Position(x, y, direction);
                        temp = Double.parseDouble(data.get("TEMP").toString());
                        Ground ground = Ground.valueOf(data.get("GROUND").toString());
                        Groundstation.getGroundstation().updateData(position, ground, temp);
                        break;
                    case "destroy":
                        Groundstation.getGroundstation().destroy(id);
                        break;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void sendRobotPosition(int id, Position position)
    {
        if (id == this.id) return;
        try
        {
            writer.write("{\"CMD\":\"updateposition\",\"ID\":\"" + id + "\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}}");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
