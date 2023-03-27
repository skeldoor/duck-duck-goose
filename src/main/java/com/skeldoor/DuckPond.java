package com.skeldoor;

import net.runelite.api.coords.WorldPoint;

import java.util.Random;

public class DuckPond {

    private WorldPoint NWTile;
    private WorldPoint SETile;
    private int maxDucks;

    // Going to assume pond areas are square for now so I don't need to do any weird pathfinding for the movement of ducks
    DuckPond(WorldPoint nwtile, WorldPoint setile, int maxDucks){
        this.NWTile = nwtile;
        this.SETile = setile;
        this.maxDucks = maxDucks;
    }

    public WorldPoint getRandomPointInPond(){
        int maxX = Math.max(NWTile.getX(), SETile.getX()) + 1;
        int minX = Math.min(NWTile.getX(), SETile.getX());
        int maxY = Math.max(NWTile.getY(), SETile.getY()) + 1;
        int minY = Math.min(NWTile.getY(), SETile.getY());

        return new WorldPoint(getRandom(minX, maxX), getRandom(minY, maxY), 0);
    }

    public int getMaxDucks(){
        return maxDucks;
    }

    public int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
