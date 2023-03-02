package main.aStar;

import main.Args;
import main.AutoScout;
import main.groundstation.GroundStation;
import main.planet.Exoplanet;
import planet.Ground;
import position.Coordinate;
import position.Direction;

import java.util.*;

public class RouteFinder {

    //Store Steps that seem to be prevented by Water
    private static final ArrayList<Step> forbiddenSteps = new ArrayList<>();

    public static void forbid(Step step) {forbiddenSteps.add(step);}

    public static boolean isForbidden(Step step) {
        return forbiddenSteps.contains(step);
    }

    /*
        Find a way from -> to
     */
    public List<Step> findRoute(Tile from, Tile to) {
        Map<Coordinate, RouteTile> allNodes = new HashMap<>();
        Queue<RouteTile> openSet = new PriorityQueue<>();

        RouteTile start = new RouteTile(from, null, 0, computeCost(from, to));
        allNodes.put(from.getCoordinate(), start);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            RouteTile next = openSet.poll();
            if (next.getCurrent().getCoordinate().equals(to.getCoordinate()) && next.getPrevious() != null) {
                List<Step> route = new ArrayList<>();
                RouteTile current = next;
                do {
                    if (current.getPrevious() != null){
                        route.add(0, new Step(current.getPrevious().getCoordinate(), current.getCurrent().getCoordinate(), getDirection(current.getPrevious().getCoordinate(), current.getCurrent().getCoordinate())));
                        current = allNodes.get(current.getPrevious().getCoordinate());
                    }else{
                        current = null;
                    }
                } while (current != null);

                return route;
            }

            getConnections(next.getCurrent(), to).forEach(connection -> {
                double newScore = next.getRouteScore() + computeCost(next.getCurrent(), connection);
                RouteTile nextNode = allNodes.getOrDefault(connection.getCoordinate(), new RouteTile(connection));//Always returns default
                allNodes.put(connection.getCoordinate(), nextNode);

                if (newScore < nextNode.getRouteScore()) {
                    nextNode.setPrevious(next.getCurrent());
                    nextNode.setRouteScore(newScore);
                    nextNode.setEstimatedScore(newScore + computeCost(connection, to));
                    openSet.add(nextNode);
                }
            });
        }
        AutoScout.addIgnored(to.getCoordinate());
        return new ArrayList<>();
    }

    private boolean areNeighbours(Coordinate from, Coordinate to){
        return Math.abs(from.X() - to.X()) <= 1 && Math.abs(from.Y() - to.Y()) <= 1;
    }

    private int getDistance(Coordinate from, Coordinate to){
        return Math.abs(from.X()-to.X()) + Math.abs(from.Y()-to.Y());
    }

    /*
        Set Cost for movements to avoid risky ones
     */
    public double computeCost(Tile from, Tile to) {

        if(!areNeighbours(from.getCoordinate(), to.getCoordinate())) return getDistance(from.getCoordinate(), to.getCoordinate());

        //Avoid Tiles with temperature related crash potential
        if (to.getMeasure().temp < Args.crashMinTemp || to.getMeasure().temp > Args.crashMaxTemp)
            return 20;

        //Avoid Water and Morass
        Ground ground = to.getGround();
        if (ground.equals(Ground.WATER) || ground.equals(Ground.MORASS)) return 10;

        //Avoid Tiles with potentially heater/cooler activating temps
        if (to.getMeasure().temp < Args.normalMinTemp || to.getMeasure().temp > Args.normalMaxTemp)
            return 3;

        return 1;
    }

    /*
        Return Direction need to get from -> to
     */
    private Direction getDirection(Coordinate from, Coordinate to) {
        if (from.Y() > to.Y()) return Direction.NORTH;
        if (from.Y() < to.Y()) return Direction.SOUTH;
        if (from.X() > to.X()) return Direction.WEST;
        return Direction.EAST;
    }

    /*
        Get valid Neighbours to move to
    */
    public Set<Tile> getConnections(Tile node, Tile target) {
        Set<Tile> neighbours = new HashSet<>();
        Tile tile;
        Ground ground;

        //Filter Neighbours
        for (Coordinate coordinate : AutoScout.getNeighbours(node.getCoordinate())) {
            tile = Exoplanet.getTile(coordinate);
            ground = tile.getGround();

            //Exclude forbidden Steps
            if (RouteFinder.isForbidden(new Step(node.getCoordinate(), coordinate, Direction.NORTH))) continue;

            if(coordinate.equals(target.getCoordinate())) {
                return new HashSet<>(Collections.singletonList(tile));
            }

            //Exclude unexplored
            if (ground.equals(Ground.NOTHING))
                continue;

            //Exclude occupied
            if (GroundStation.isOccupied(coordinate)) continue;

            neighbours.add(tile);
        }
        return neighbours;
    }
}
