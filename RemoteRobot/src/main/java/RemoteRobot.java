import Position.Position;
import planet.Exoplanet;
import planet.ServerCommandsListener;

import javax.sql.CommonDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RemoteRobot implements ServerCommandsListener
{

    private Thread autoScout;
    private Exoplanet exoplanet = new Exoplanet();
    private Position position;
    private double temp;
    private double energy;
    private HashMap<String, Position> robots;

    private RemoteRobot()
    {
        exoplanet.addListener(this);
        String command;
        try
        {
            while (true)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                command = reader.readLine();
                if (command == null) ;
                else if (!command.contains(":"))
                {
                    System.out.println("Invalid syntax.");
                }
                else if (command.startsWith("orbit:") && command.length() > 6)
                {
                    exoplanet.c2sOrbit(command.split(":")[1]);
                }
                else if (command.startsWith("land:POSITION|") && command.length() > 19)
                {
                    String[] data = command.split("\\|");
                    if (data.length != 4) System.out.println("Invalid syntax.");

                }
                else
                {
                    System.out.println("Unknown command or Invalid syntax.");
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)
    {
        new RemoteRobot();
    }

    @Override
    public void s2cInit(HashMap data)
    {

    }

    @Override
    public void s2cLanded(HashMap data)
    {

    }

    @Override
    public void s2cScanned(HashMap data)
    {

    }

    @Override
    public void s2cMoved(HashMap data)
    {

    }

    @Override
    public void s2cRotated(HashMap data)
    {

    }

    @Override
    public void s2cCrashed(HashMap data)
    {

    }

    @Override
    public void s2cError(HashMap data)
    {

    }

    @Override
    public void s2cPos(HashMap data)
    {

    }

    @Override
    public void s2cCharged(HashMap data)
    {

    }

    @Override
    public void s2cStatus(HashMap data)
    {

    }
}
