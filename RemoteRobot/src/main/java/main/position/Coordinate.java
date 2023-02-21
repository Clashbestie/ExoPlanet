package main.position;

public class Coordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int X(){
        return x;
    }

    public int Y(){
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Coordinate other = (Coordinate) obj;

        if (this.x != other.x || this.y != other.y) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.y;
        hash = 53 * hash + this.x;
        return hash;
    }
}
