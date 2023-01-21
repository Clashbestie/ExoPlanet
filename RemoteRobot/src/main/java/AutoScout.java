import planet.Exoplanet;
import planet.Ground;
import position.Direction;
import position.Position;

import java.util.ArrayList;

public class AutoScout implements Runnable{

    private Thread thread;
    @Override
    public void run() {
        Position target = findClosestUnexplored(RemoteRobot.INSTANCE().getPosition());
        System.out.println(target.getX() + " " + target.getY());
    }

    /*
        Breadth first search
     */
    private Position findClosestUnexplored(Position start){
        if(Exoplanet.INSTANCE().getData(start).ground.equals(Ground.NOTHING)) return start;
        ArrayList<Position> explored = new ArrayList<>();
        explored.add(start);
        ArrayList<Position> q = new ArrayList<>(getNeighbours(start));
        while (!q.isEmpty()){
            Position v = q.remove(0);
            if(Exoplanet.INSTANCE().getData(v).ground.equals(Ground.NOTHING)) return v;
            for (Position w: getNeighbours(v)) {
                if(!explored.contains(w)){
                    explored.add(w);
                    if(!Exoplanet.INSTANCE().getData(w).ground.equals(Ground.OOB)) q.addAll(getNeighbours(w));
                }
            }
        }
        return new Position(-1,-1, Direction.NORTH);
    }

    private ArrayList<Position> getNeighbours(Position start){
        ArrayList<Position> neighbours = new ArrayList<>();
        start.setDir(Direction.NORTH);
        neighbours.add(start.facing());
        start.setDir(Direction.EAST);
        neighbours.add(start.facing());
        start.setDir(Direction.SOUTH);
        neighbours.add(start.facing());
        start.setDir(Direction.WEST);
        neighbours.add(start.facing());
        return neighbours;
    }

    public void Start(){
        thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        thread.interrupt();
    }
}
