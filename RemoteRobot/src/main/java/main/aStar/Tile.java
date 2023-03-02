package main.aStar;

import planet.Ground;
import planet.Measure;
import position.Coordinate;

public class Tile {

    private final Coordinate coordinate;

    private Measure measure;

    public Tile(Coordinate coordinate, Measure measure) {
        this.coordinate = coordinate;
        this.measure = measure;
    }

    public Ground getGround() {
        return measure.ground;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public Measure getMeasure() {
        return measure;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
