package main.aStar;

import main.AutoScout;
import main.RemoteRobot;
import main.planet.Exoplanet;
import main.planet.Ground;
import main.position.Coordinate;
import main.position.Position;

import java.util.HashSet;
import java.util.Set;

public class Graph{


    public Tile getNode(Coordinate coordinate) {
        return RemoteRobot.INSTANCE().Exoplanet().getTile(coordinate);
    }

    public Set<Tile> getConnections(Tile node) {
        Set<Tile> neighbours = new HashSet<>();
        Tile tile;
        Ground ground;
        for (Coordinate coordinate:  AutoScout.getNeighbours(node.getCoordinate())) {
            tile = RemoteRobot.INSTANCE().Exoplanet().getTile(coordinate);
            ground = tile.getGround();

            if(RemoteRobot.INSTANCE().GroundStation().isOccupied(coordinate)) continue;
            if(ground.equals(Ground.OOB) || ground.equals(Ground.LAVA))  continue;
            neighbours.add(tile);
        }
        return neighbours;
    }
}
