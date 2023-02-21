package main.aStar;

import main.RemoteRobot;
import main.planet.Ground;
import main.position.Coordinate;
import main.position.Direction;

import java.util.*;

public class RouteFinder {
    private final Graph graph;

    public RouteFinder() {
        this.graph = new Graph();
    }

    public double computeCost(Tile from, Tile to) {
        Ground ground = to.getGround();
        if(ground.equals(Ground.WATER) || ground.equals(Ground.MORASS)) return 10;
        return 1;
    }

    private Direction getDirection(Coordinate from, Coordinate to){
        if(from.Y() > to.Y()) return Direction.NORTH;
        if(from.Y() < to.Y()) return Direction.SOUTH;
        if(from.X() > to.X()) return Direction.WEST;
        return Direction.EAST;
    }

    public List<Step> findRoute(Tile from, Tile to) {
        //System.out.println("From " + from.getCoordinate().X() + "/" + from.getCoordinate().Y() + " to " + to.getCoordinate().X() + "/" + to.getCoordinate().Y());
        Map<Tile, RouteTile> allNodes = new HashMap<>();
        Queue<RouteTile> openSet = new PriorityQueue<>();

        RouteTile start = new RouteTile(from, null, 0d, computeCost(from, to));
        allNodes.put(from, start);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            RouteTile next = openSet.poll();
            if (next.getCurrent().getCoordinate().equals(to.getCoordinate())) {

                List<Step> route = new ArrayList<>();
                RouteTile current = next;
                do {
                    if(current.getPrevious() != null)
                        route.add(0, new Step(current.getPrevious().getCoordinate(), current.getCurrent().getCoordinate(), getDirection(current.getPrevious().getCoordinate(), current.getCurrent().getCoordinate())));
                    current = allNodes.get(current.getPrevious());
                } while (current != null);

                return route;
            }

            graph.getConnections(next.getCurrent()).forEach(connection -> {
                double newScore = next.getRouteScore() + computeCost(next.getCurrent(), connection);
                RouteTile nextNode = allNodes.getOrDefault(connection, new RouteTile(connection));
                allNodes.put(connection, nextNode);

                if (nextNode.getRouteScore() > newScore) {
                    nextNode.setPrevious(next.getCurrent());
                    nextNode.setRouteScore(newScore);
                    nextNode.setEstimatedScore(newScore + computeCost(connection, to));
                    openSet.add(nextNode);
                }
            });
        }

        throw new IllegalStateException("No route found");
    }

}
