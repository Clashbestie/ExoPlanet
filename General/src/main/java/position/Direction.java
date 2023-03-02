package position;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public Direction inverse() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
        }
        return NORTH;
    }
}

