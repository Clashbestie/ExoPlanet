package main.aStar;

import java.util.StringJoiner;

class RouteTile implements Comparable<RouteTile> {

    private final Tile current;
    private Tile previous;
    private double routeScore;
    private double estimatedScore;

    RouteTile(Tile current) {
        this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    RouteTile(Tile current, Tile previous, double routeScore, double estimatedScore) {
        this.current = current;
        this.previous = previous;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    Tile getCurrent() {
        return current;
    }

    Tile getPrevious() {
        return previous;
    }

    void setPrevious(Tile previous) {
        this.previous = previous;
    }

    double getRouteScore() {
        return routeScore;
    }

    void setRouteScore(double routeScore) {
        this.routeScore = routeScore;
    }

    double getEstimatedScore() {
        return estimatedScore;
    }

    void setEstimatedScore(double estimatedScore) {
        this.estimatedScore = estimatedScore;
    }

    @Override
    public int compareTo(RouteTile other) {
        return Double.compare(this.estimatedScore, other.estimatedScore);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RouteTile.class.getSimpleName() + "[", "]").add("current=" + current)
                .add("previous=" + previous).add("routeScore=" + routeScore).add("estimatedScore=" + estimatedScore)
                .toString();
    }
}
