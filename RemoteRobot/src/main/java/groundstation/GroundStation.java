package groundstation;

import planet.Ground;
import planet.Measure;
import position.Position;

import java.io.*;
import java.net.Socket;

public class GroundStation implements Runnable
{

    private final BufferedWriter writer;
    private final BufferedReader reader;

    public GroundStation()
    {
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

    @Override
    public void run()
    {
        //TODO wait for Groundstation commands
    }

    public void sendPos(String name, Position position, int energy, double temp, String status)
    {
        write("{\"CMD\":\"updateposition\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}," +
                "\"NAME\":\"" + name +"\"," +
                "\"ENERGY\":\"" + energy +"\"," +
                "\"TEMP\":\"" + temp +"\"," +
                "\"STATUS\":\"" + status +"\"}");
    }

    public void sendData(Position position, Measure measure){
        write("{\"CMD\":\"updatedata\", \"POSITION\":{\"X\":" + position.getX() + ",\"Y\":" + position.getY() + ",\"DIRECTION\":\"" + position.getDir() + "\"}," +
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
