import groundstation.GroundStation;
import planet.Exoplanet;
import planet.Ground;
import planet.ServerCommandsListener;
import position.Direction;
import position.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RemoteRobot implements ServerCommandsListener
{

    private Thread autoScout;
    private Exoplanet exoplanet = new Exoplanet();

    private GroundStation groundStation;
    private Position position;
    private double temp;
    private double energy;
    private HashMap<String, Position> robots;

    private RemoteRobot()
    {
        exoplanet.addListener(this);
        String command;
        while (true)
        {


            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                command = reader.readLine();
                if (command == null) continue;
                else if (command.startsWith("orbit:") && command.length() > 6)
                {
                    exoplanet.c2sOrbit(command.split(":")[1]);
                }
                else if (command.startsWith("land:POSITION|") && command.length() > 19)
                {
                    String[] data = command.split("\\|");
                    if (data.length != 4) System.out.println("Invalid syntax.");
                    else
                    {
                        int x = Integer.parseInt(data[1]);
                        int y = Integer.parseInt(data[2]);
                        Direction dir = Direction.valueOf(data[3]);
                        exoplanet.c2sLand(new Position(x, y, dir));
                    }
                }
                else if (command.equals("scan"))
                {
                    exoplanet.c2sScan();
                }
                else if (command.equals("move"))
                {
                    exoplanet.c2sMove();
                }
                else if (command.startsWith("rotate:") && command.length() > 7)
                {
                    String data = command.split(":")[1];
                    if (data.equals("RIGHT")) exoplanet.c2sRotate(true);
                    else if (data.equals("LEFT")) exoplanet.c2sRotate(false);
                    else System.out.println("Invalid syntax.");
                }
                else if (command.equals("exit"))
                {
                    exoplanet.c2sExit();
                }
                else if (command.equals("getpos"))
                {
                    exoplanet.c2sGetPos();
                }
                else if (command.startsWith("charge:") && command.length() > 7)
                {
                    String[] data = command.split(":");
                    if (data.length != 2) System.out.println("Invalid syntax.");
                    else
                    {
                        int duration = Integer.parseInt(data[1]);
                        exoplanet.c2sCharge(duration);
                    }
                }
                else
                {
                    System.out.println("Unknown command or Invalid syntax.");
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        new RemoteRobot();
    }

    @Override
    public void s2cInit(int width, int height)
    {
        System.out.println(width + "|" + height);
    }

    @Override
    public void s2cLanded(Ground ground, double temp)
    {
        System.out.println(ground + "|" + temp);
    }

    @Override
    public void s2cScanned(Ground ground, double temp)
    {
        System.out.println(ground + "|" + temp);
    }

    @Override
    public void s2cMoved(Position position)
    {
        this.position = position;
    }

    @Override
    public void s2cRotated(Direction direction)
    {
        System.out.println(direction);
    }

    @Override
    public void s2cCrashed()
    {

    }

    @Override
    public void s2cError(String error)
    {

    }

    @Override
    public void s2cPos(Position position)
    {

    }

    @Override
    public void s2cCharged(double temp, int energy, String text)
    {

    }

    @Override
    public void s2cStatus(double temp, int energy, String text)
    {

    }
}
