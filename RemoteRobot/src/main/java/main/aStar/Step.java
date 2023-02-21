package main.aStar;

import main.position.Coordinate;
import main.position.Direction;

public class Step {
    private final Coordinate from;
    private final Coordinate to;
    private final Direction facing;

    public Step(Coordinate from, Coordinate to, Direction facing){
        this.from = from;
        this.to = to;
        this.facing = facing;
    }

    public Coordinate From(){
        return from;
    }

    public Coordinate To(){
        return to;
    }

    public Direction Facing(){
        return facing;
    }
}
