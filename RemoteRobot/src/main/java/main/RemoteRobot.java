package main;

import main.groundstation.GroundStation;
import main.planet.Exoplanet;
import main.planet.Ground;
import main.planet.Measure;
import main.planet.ServerCommandsListener;
import main.position.Coordinate;
import main.position.Direction;
import main.position.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RemoteRobot implements ServerCommandsListener
{

    private static RemoteRobot INSTANCE;
    public static RemoteRobot INSTANCE(){
        return INSTANCE;
    }
    private AutoScout autoScout;
    private Exoplanet exoplanet;
    public Exoplanet Exoplanet(){return exoplanet;}
    private GroundStation groundStation;
    public GroundStation GroundStation(){return groundStation;}
    private String name;
    private Position position;
    public Position getPosition(){
        return position;
    }
    private double temp = -1;
    private int energy = -1;
    private String status = "GOOD";

    private RemoteRobot()
    {
        INSTANCE = this;
        exoplanet = new Exoplanet();
        groundStation = new GroundStation();
        exoplanet.addListener(this); //Register before everything else to ensure updated data
        autoScout = new AutoScout();
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
        groundStation.sendData(new Coordinate(0, -1), new Measure(Ground.NOTHING, width));
        groundStation.sendData(new Coordinate(-1, 0), new Measure(Ground.NOTHING, height));
    }

    @Override
    public void s2cLanded(Measure measure)
    {
        groundStation.sendPos(name, position, energy, temp, status);
        groundStation.sendData(position.getCoordinate(), measure);
        exoplanet.addData(position.getCoordinate(), measure);
    }

    @Override
    public void s2cScanned(Measure measure)
    {
        Coordinate facing = position.facing().getCoordinate();
        if(facing.Y() < 0 || facing.X() < 0) return;
        groundStation.sendData(facing, measure);
        exoplanet.addData(facing, measure);
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
        this.position.setDir(direction);
        groundStation.sendPos(name, position, energy, temp, status);
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
