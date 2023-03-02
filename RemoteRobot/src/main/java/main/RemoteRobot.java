package main;

import main.groundstation.GroundStation;
import main.planet.Exoplanet;
import main.planet.ServerCommandsListener;
import planet.Ground;
import planet.Measure;
import position.Coordinate;
import position.Direction;
import position.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

public class RemoteRobot implements ServerCommandsListener {

    private static AutoScout autoScout;
    private static Position position;

    public static Position getPosition() {
        return position;
    }

    private static double temp = 0;

    public static double getTemp() {
        return temp;
    }

    private static int energy = 100;

    public static int getEnergy() {
        return energy;
    }

    private static String status = "GOOD";

    public static void main(String[] args) {
        //Register before everything else to ensure updated data
        Exoplanet.addListener(new RemoteRobot());

        autoScout = new AutoScout();
        Args.readArgs(args);
        if(!Args.getNoAuto() || Args.getLandOnly()) land();
        if(!Args.getNoAuto()) autoScout.Start();
       readConsole();
    }

    private static void land(){
        Exoplanet.c2sOrbit(Args.getName());
        Exoplanet.await();
        Coordinate coordinate = Exoplanet.getSaveCoordinate();
        if(coordinate == null){
            int x;
            int y;
            do {
                x = ThreadLocalRandom.current().nextInt(0, Exoplanet.getWidth());
                y = ThreadLocalRandom.current().nextInt(0, Exoplanet.getHeight());
                position = new Position(x,y,Direction.NORTH);
            }while(!Exoplanet.c2sLand(new Position(x,y, Direction.NORTH)));
        }else {
            Exoplanet.c2sLand(new Position(coordinate.X(), coordinate.Y(), Direction.NORTH));
            position = new Position(coordinate.X(), coordinate.Y(), Direction.NORTH);
        }
        Exoplanet.await();
    }


    private static void readConsole(){
        String command;
        while (true) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                command = reader.readLine();
                if (command == null) {}
                else if (command.startsWith("orbit:") && command.length() > 6) {
                    Exoplanet.c2sOrbit(command.split(":")[1]);
                    Args.setName(command.split(":")[1]);
                } else if (command.startsWith("land:POSITION|") && command.length() > 19) {
                    String[] data = command.split("\\|");
                    if (data.length != 4) System.out.println("Invalid syntax.");
                    else {
                        int x = Integer.parseInt(data[1]);
                        int y = Integer.parseInt(data[2]);
                        Direction dir = Direction.valueOf(data[3]);
                        Position position = new Position(x, y, dir);
                        if (!Exoplanet.c2sLand(position)) {
                            System.out.println("Land cancelled");
                        }
                        RemoteRobot.position = position;
                    }
                } else if (command.equals("scan")) {
                    Exoplanet.c2sScan();
                } else if (command.equals("move")) {
                    Exoplanet.c2sMove();
                } else if (command.startsWith("rotate:") && command.length() > 7) {
                    String data = command.split(":")[1];
                    if (data.equals("RIGHT")) Exoplanet.c2sRotate(true);
                    else if (data.equals("LEFT")) Exoplanet.c2sRotate(false);
                    else System.out.println("Invalid syntax.");
                } else if (command.equals("exit")) {
                    Exoplanet.c2sExit();
                } else if (command.equals("getpos")) {
                    Exoplanet.c2sGetPos();
                } else if (command.startsWith("charge:") && command.length() > 7) {
                    String[] data = command.split(":");
                    if (data.length != 2) System.out.println("Invalid syntax.");
                    else {
                        int duration = Integer.parseInt(data[1]);
                        Exoplanet.c2sCharge(duration);
                    }
                } else if (command.equals("autoscout")) {
                    autoScout.Start();
                } else {
                    System.out.println("Unknown command or Invalid syntax.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void s2cInit(int width, int height) {
        GroundStation.sendData(new Coordinate(0, -1), new Measure(Ground.NOTHING, width));
        GroundStation.sendData(new Coordinate(-1, 0), new Measure(Ground.NOTHING, height));
    }

    @Override
    public void s2cLanded(Measure measure) {
        GroundStation.sendPos(Args.getName(), position, energy, temp, status);
        GroundStation.sendData(position.getCoordinate(), measure);
        Exoplanet.addData(position.getCoordinate(), measure);
    }

    @Override
    public void s2cScanned(Measure measure) {
        Coordinate facing = position.facing().getCoordinate();
        if (facing.Y() < 0 || facing.X() < 0) return;
        GroundStation.sendData(facing, measure);
        Exoplanet.addData(facing, measure);
    }

    @Override
    public void s2cMoved(Position position) {
        RemoteRobot.position = position;
        GroundStation.sendPos(Args.getName(), position, energy, temp, status);
    }

    @Override
    public void s2cRotated(Direction direction) {
        position.setDir(direction);
        GroundStation.sendPos(Args.getName(), position, energy, temp, status);
    }

    @Override
    public void s2cCrashed() {
        GroundStation.sendDestroy();
        System.exit(0);
    }

    @Override
    public void s2cError(String error) {}

    @Override
    public void s2cPos(Position position) {
        RemoteRobot.position = position;
    }

    @Override
    public void s2cCharged(double temp, int energy, String text) {
        RemoteRobot.energy = energy;
        RemoteRobot.temp = temp;
        RemoteRobot.status = text;
    }

    @Override
    public void s2cStatus(double temp, int energy, String text) {
        RemoteRobot.energy = energy;
        RemoteRobot.temp = temp;
        RemoteRobot.status = text;
    }
}
