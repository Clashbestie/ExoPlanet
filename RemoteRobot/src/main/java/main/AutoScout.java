package main;

import main.aStar.RouteFinder;
import main.aStar.Step;
import main.groundstation.GroundStation;
import main.planet.Exoplanet;
import planet.Ground;
import position.Coordinate;
import position.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AutoScout implements Runnable {

    public static final Color red = new Color(255, 0, 0, 200);
    public static final Color darkGray = new Color(200, 200, 200, 200);
    public static final Color green = new Color(0, 255, 0, 100);
    public static final Color yellow = new Color(255, 255, 0, 200);

    private Thread thread;
    private final RouteFinder routeFinder = new RouteFinder();
    private int failedMove = 0;
    private static final ArrayList<Coordinate> ignore = new ArrayList<>();
    private static final ArrayList<Coordinate> chargingIgnore = new ArrayList<>();
    private static final List<Ground> chargeTargets = Arrays.asList(Ground.GRAVEL, Ground.MORASS, Ground.SAND);

    public static final List<Ground> neutralTargets = Arrays.asList(Ground.GRAVEL, Ground.SAND, Ground.PLANT, Ground.ROCK);

    public static void addIgnored(Coordinate coordinate) {
        ignore.add(coordinate);
    }

    private void log(String message) {
        System.out.println("Autoscout: " + message);
    }

    @Override
    public void run() {
        //land:POSITION|0|0|NORTH
        while (!thread.isInterrupted()) {
            Charge();
            log("Searching target");
            Coordinate target = findClosest(RemoteRobot.getPosition().getCoordinate(), Ground.NOTHING, ignore);
            if (target.equals(new Coordinate(-1, -1))) {
                log("No target found");
                GroundStation.sendDestroy();
                System.exit(0);
            }else if(target.equals(RemoteRobot.getPosition().getCoordinate())){
                log("Found current Position");
                target = findClosest(RemoteRobot.getPosition().getCoordinate(), neutralTargets, ignore);
                if (target.equals(new Coordinate(-1, -1))) {
                    log("No movable explored Neighbours found");
                    GroundStation.sendDestroy();
                    System.exit(0);
                }
                List<Step> path = routeFinder.findRoute(Exoplanet.getTile(RemoteRobot.getPosition().getCoordinate()), Exoplanet.getTile(target));
                followPath(path, false);
            }else {
                log("Searching route to " + target.X() + "/" + target.Y() + " from " + RemoteRobot.getPosition().getX() + "/" + RemoteRobot.getPosition().getY());
                List<Step> path = routeFinder.findRoute(Exoplanet.getTile(RemoteRobot.getPosition().getCoordinate()), Exoplanet.getTile(target));
                followPath(path, true);
            }
        }
    }

    private boolean followPath(List<Step> path, boolean scan) {
        if (path.size() == 0) return false;
        ignore.clear();
        Iterator<Step> iterator = path.iterator();
        log("Moving");
        while (iterator.hasNext()) {
            Step step = iterator.next();
            rotateTo(step.Facing());
            if (!iterator.hasNext()) {
                if (scan) Exoplanet.c2sScan();
                else Exoplanet.c2sMove();
                Exoplanet.await();
            } else {
                //Tile occupied
                if (!Exoplanet.c2sMove()) return false;
                Exoplanet.await();
                if (!RemoteRobot.getPosition().getCoordinate().equals(step.To())) {
                    Coordinate current = RemoteRobot.getPosition().getCoordinate();
                    log("Unexpected position " + current.X() + "/" + current.Y() + " != " + step.To().X() + "/" + step.To().Y());
                    failedMove++;
                    if (failedMove >= 3 && Exoplanet.getData(step.To()).ground.equals(Ground.WATER)) {
                        RouteFinder.forbid(step);
                        failedMove = 0;
                    }
                    return false;
                } else failedMove = 0;
            }
        }
        return true;
    }

    private void Charge() {
        Exoplanet.c2sCharge(1);//Get current Energy Level
        Exoplanet.await();
        if (RemoteRobot.getEnergy() <= 50) {
            System.out.println("Finding charge location");
            List<Step> path;
            do {
                Coordinate target = findClosest(RemoteRobot.getPosition().getCoordinate(), chargeTargets, chargingIgnore);

                int temp = (int) Exoplanet.getData(target).temp;
                if (temp < Args.normalMinTemp || temp > Args.normalMaxTemp) {
                    chargingIgnore.add(target);
                    path = null;
                    continue;
                }
                path = routeFinder.findRoute(Exoplanet.getTile(RemoteRobot.getPosition().getCoordinate()), Exoplanet.getTile(target));
            } while (path == null || !followPath(path, false));
            Exoplanet.c2sCharge(1);//Get current Energy Level
            Exoplanet.await();
            int multiplier = (Exoplanet.getData(RemoteRobot.getPosition().getCoordinate()).ground.equals(Ground.SAND) ? 1 : 2);
            int time = (100 - RemoteRobot.getEnergy()) * multiplier;
            Exoplanet.c2sCharge(time);
            Exoplanet.await();
        } else {
            System.out.println("Not charging");
        }
    }

    private void rotateTo(Direction target) {
        Direction current = RemoteRobot.getPosition().getDir();
        while (!current.equals(target)) {
            //Direction doesn't matter
            if (Math.abs(current.ordinal() - target.ordinal()) == 2) Exoplanet.c2sRotate(true);
                //One Direction is faster
            else if (Math.abs(current.ordinal() - target.ordinal()) == 1)
                Exoplanet.c2sRotate(target.ordinal() - current.ordinal() == 1);
                //NORTH <-> WEST
            else Exoplanet.c2sRotate(current.ordinal() - target.ordinal() == 3);
            Exoplanet.await();
            current = RemoteRobot.getPosition().getDir();
        }
    }

    /*
        Breadth first search
     */
    private Coordinate findClosest(Coordinate start, Ground target, ArrayList<Coordinate> ignoreList) {
        ArrayList<Coordinate> explored = new ArrayList<>();
        explored.add(start);
        ArrayList<Coordinate> q = new ArrayList<>();
        q.add(start);
        while (!q.isEmpty()) {
            Coordinate v = q.remove(0);
            if ((ignoreList == null || !ignore.contains(v)) && Exoplanet.getData(v).ground.equals(target)) return v;
            for (Coordinate w : getNeighbours(v)) {
                if (!explored.contains(w)) {
                    explored.add(w);
                    q.add(w);
                }
            }
        }
        return new Coordinate(-1, -1);
    }

    private Coordinate findClosest(Coordinate start, List<Ground> targets, ArrayList<Coordinate> ignoreList) {
        ArrayList<Coordinate> explored = new ArrayList<>();
        explored.add(start);
        ArrayList<Coordinate> q = new ArrayList<>();
        q.add(start);
        while (!q.isEmpty()) {
            Coordinate v = q.remove(0);
            if ((ignoreList == null || !ignore.contains(v)) && targets.contains(Exoplanet.getData(v).ground)) return v;
            for (Coordinate w : getNeighbours(v)) {
                if (!explored.contains(w)) {
                    explored.add(w);
                    q.add(w);
                }
            }
        }
        return new Coordinate(-1, -1);
    }

    public static ArrayList<Coordinate> getNeighbours(Coordinate start) {
        ArrayList<Coordinate> neighbours = new ArrayList<>();
        neighbours.add(new Coordinate(start.X() - 1, start.Y()));
        neighbours.add(new Coordinate(start.X(), start.Y() - 1));
        neighbours.add(new Coordinate(start.X(), start.Y() + 1));
        neighbours.add(new Coordinate(start.X() + 1, start.Y()));
        neighbours.removeIf(n -> (
                Exoplanet.getData(n).ground.equals(Ground.OOB) ||
                        Exoplanet.getData(n).ground.equals(Ground.LAVA) ||
                        ignore.contains(n) ||
                        RouteFinder.isForbidden(new Step(start, n, Direction.NORTH))));
        return neighbours;
    }

    public void Start() {
        thread = new Thread(this);
        thread.start();
    }
}
