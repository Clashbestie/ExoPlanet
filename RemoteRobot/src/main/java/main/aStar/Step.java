package main.aStar;


import position.Coordinate;
import position.Direction;

public class Step {

    private final Coordinate from;
    private final Coordinate to;
    private final Direction facing;

    public Step(Coordinate from, Coordinate to, Direction facing) {
        this.from = from;
        this.to = to;
        this.facing = facing;
    }

    public Coordinate From() {
        return from;
    }

    public Coordinate To() {
        return to;
    }

    public Direction Facing() {
        return facing;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Step other = (Step) obj;

        return this.from.equals(other.from) && this.to.equals(other.to);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.from.Y() + this.from.X();
        hash = 53 * hash + this.to.Y() + this.to.X();
        return hash;
    }
}
