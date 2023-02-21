package main;

import main.aStar.RouteFinder;
import main.aStar.Step;
import main.planet.Exoplanet;
import main.planet.Ground;
import main.planet.Measure;
import main.planet.ServerCommandsListener;
import main.position.Coordinate;
import main.position.Direction;
import main.position.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoScout implements Runnable, ServerCommandsListener {

    private Thread thread;
    private RouteFinder routeFinder;

    private ResettableCountDownLatch latch;

    @Override
    public void run() {
        //land:POSITION|0|0|NORTH
        RemoteRobot.INSTANCE().Exoplanet().addListener(this);
        latch = new ResettableCountDownLatch(1);
        routeFinder = new RouteFinder();
        while (!thread.isInterrupted()) {
            System.out.println("Searching target");
            Coordinate target = findClosestUnexplored(RemoteRobot.INSTANCE().getPosition().getCoordinate());
            System.out.println("Searching route to " + target.X() + "/" + target.Y());
            List<Step> path = routeFinder.findRoute(RemoteRobot.INSTANCE().Exoplanet().getTile(RemoteRobot.INSTANCE().getPosition().getCoordinate()), RemoteRobot.INSTANCE().Exoplanet().getTile(target));
            Iterator<Step> iterator = path.iterator();
            System.out.println("Moving");
            while(iterator.hasNext()){
                Step step = iterator.next();
                if (!RemoteRobot.INSTANCE().getPosition().getCoordinate().equals(step.From())){
                    break;
                }
                rotateTo(step.Facing());
                if(!iterator.hasNext()){
                    RemoteRobot.INSTANCE().Exoplanet().c2sScan();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    latch.reset();
                }else {
                    RemoteRobot.INSTANCE().Exoplanet().c2sMove();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    latch.reset();
                }
            }
            thread.interrupt();
        }
    }

    private void rotateTo(Direction target){
        Direction current = RemoteRobot.INSTANCE().getPosition().getDir();
        while (!current.equals(target)){
            //Direction doesn't matter
            if(Math.abs(current.ordinal() - target.ordinal()) == 2) RemoteRobot.INSTANCE().Exoplanet().c2sRotate(true);
            //One Direction is faster
            else if(Math.abs(current.ordinal() - target.ordinal()) == 1) RemoteRobot.INSTANCE().Exoplanet().c2sRotate(target.ordinal() - current.ordinal() == 1);
            //NORTH <-> WEST
            else RemoteRobot.INSTANCE().Exoplanet().c2sRotate(current.ordinal() - target.ordinal() == 3);
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.reset();
            current = RemoteRobot.INSTANCE().getPosition().getDir();
        }
    }

    /*
        Breadth first search
     */
    private Coordinate findClosestUnexplored(Coordinate start){
        ArrayList<Coordinate> explored = new ArrayList<>();
        explored.add(start);
        ArrayList<Coordinate> q = new ArrayList<>(getNeighbours(start));
        while (!q.isEmpty()){
            Coordinate v = q.remove(0);
            if(RemoteRobot.INSTANCE().Exoplanet().getData(v).ground.equals(Ground.NOTHING)) return v;
            for (Coordinate w: getNeighbours(v)) {
                if(!explored.contains(w)){
                    if(RemoteRobot.INSTANCE().Exoplanet().getData(w).ground.equals(Ground.NOTHING)) return w;
                    explored.add(w);
                    q.add(w);
                    System.out.println(w.X() + "/" + w.Y());
                    //if(!RemoteRobot.INSTANCE().Exoplanet().getData(w).ground.equals(Ground.OOB)) q.addAll(getNeighbours(w));
                }
            }
        }
        return new Coordinate(-1,-1);
    }

    public static ArrayList<Coordinate> getNeighbours(Coordinate start){
        ArrayList<Coordinate> neighbours = new ArrayList<>();
        if(start.X() > 0) neighbours.add(new Coordinate(start.X()-1, start.Y()));
        if(start.X() < RemoteRobot.INSTANCE().Exoplanet().getWidth()-2) neighbours.add(new Coordinate(start.X()+1, start.Y()));
        if(start.Y() > 0) neighbours.add(new Coordinate(start.X(), start.Y()-1));
        if(start.Y() < RemoteRobot.INSTANCE().Exoplanet().getHeight()-2)neighbours.add(new Coordinate(start.X(), start.Y()+1));
        return neighbours;
    }

    public void Start(){
        thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        thread.interrupt();
    }

    @Override
    public void s2cInit(int width, int height) {

    }

    @Override
    public void s2cLanded(Measure measure) {

    }

    @Override
    public void s2cScanned(Measure measure) {
        latch.countDown();
    }

    @Override
    public void s2cMoved(Position position) {
        latch.countDown();
    }

    @Override
    public void s2cRotated(Direction direction) {
        latch.countDown();
    }

    @Override
    public void s2cCrashed() {

    }

    @Override
    public void s2cError(String text) {

    }

    @Override
    public void s2cPos(Position position) {

    }

    @Override
    public void s2cCharged(double temp, int energy, String text) {

    }

    @Override
    public void s2cStatus(double temp, int energy, String text) {

    }
}
