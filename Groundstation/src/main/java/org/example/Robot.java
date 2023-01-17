package org.example;

import com.google.gson.Gson;
import org.example.position.Position;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Robot implements Runnable
{

    private Gson gson = new Gson();
    private Socket socket;
    private int id;
    private BufferedWriter writer;
    private BufferedReader reader;

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
                        break;
                    case "destroy":
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
