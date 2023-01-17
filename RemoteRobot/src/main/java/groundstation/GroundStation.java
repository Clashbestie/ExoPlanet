package groundstation;

import planet.Exoplanet;
import position.Position;

import java.io.*;
import java.net.Socket;

public class GroundStation implements Runnable{

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
    public void run() {
        
    }

    public void sendPos(Position position){
        write(Exoplanet.gson().toJson(position));
    }

    public void sendDestroy(){
        write("{\"CMD\":\"destroy\"}");
    }

    private void write(String message){
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
