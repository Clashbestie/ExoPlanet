import groundstation.GroundStation;
import planet.Exoplanet;
import planet.Ground;
import planet.Measure;
import planet.ServerCommandsListener;
import position.Direction;
import position.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RemoteRobot implements ServerCommandsListener
{

    private static RemoteRobot INSTANCE;
    public static RemoteRobot INSTANCE(){
        return INSTANCE;
    }

    private AutoScout autoScout = new AutoScout();
    private Exoplanet exoplanet = new Exoplanet();
    private GroundStation groundStation = new GroundStation();
    private String name;
    private Position position;

    //TODO check if allows write
    public Position getPosition(){
        return position;
    }

    private double temp = -1;
    private int energy = -1;

    private String status = "GOOD";
    private HashMap<String, Position> robots;

    private RemoteRobot()
    {
        INSTANCE = this;
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
                    this.name = command.split(":")[1];
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
                        Position position = new Position(x,y,dir);
                        exoplanet.c2sLand(position);
                        this.position = position;
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
                else if (command.equals("autoscout"))
                {
                    autoScout.Start();
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
        groundStation.sendData(new Position(0, -1, Direction.EAST), new Measure(Ground.NOTHING, width));
        groundStation.sendData(new Position(-1, 0, Direction.EAST), new Measure(Ground.NOTHING, height));
    }

    @Override
    public void s2cLanded(Measure measure)
    {
        groundStation.sendPos(name, position, energy, temp, status);
        groundStation.sendData(position, measure);
        exoplanet.addData(position, measure);
    }

    @Override
    public void s2cScanned(Measure measure)
    {
        groundStation.sendData(position.facing(), measure);
        exoplanet.addData(position.facing(), measure);
    }

    @Override
    public void s2cMoved(Position position)
    {
        this.position = position;
        groundStation.sendPos(name, position, energy, temp, status);
    }

    @Override
    public void s2cRotated(Direction direction)
    {
        System.out.println(direction);
    }

    @Override
    public void s2cCrashed()
    {
        groundStation.sendDestroy();
    }

    @Override
    public void s2cError(String error)
    {

    }

    @Override
    public void s2cPos(Position position)
    {
        this.position = position;
    }

    @Override
    public void s2cCharged(double temp, int energy, String text)
    {
        this.energy = energy;
        this.temp = temp;
    }

    @Override
    public void s2cStatus(double temp, int energy, String text)
    {
        this.energy = energy;
        this.temp = temp;
    }
}
