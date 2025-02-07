package com.skeldoor;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import java.util.Random;

@Slf4j
public class DuckPond {

    private WorldPoint NWTile;
    private WorldPoint SETile;
    private WorldPoint SWTile; // Used for orientation with POH
    private int maxDucks;
    private int plane;
    public boolean museumPond;

    // Going to assume pond areas are square for now so I don't need to do any weird pathfinding for the movement of ducks
    DuckPond(WorldPoint nwtile, WorldPoint setile, int maxDucks){
        this.NWTile = nwtile;
        this.SETile = setile;
        this.maxDucks = maxDucks;
        this.plane = nwtile.getPlane();
        SWTile = new WorldPoint(nwtile.getX(), setile.getY(), plane);
        this.museumPond = false;
    }

    DuckPond(WorldPoint nwtile, WorldPoint setile, int maxDucks, boolean museumPond){
        this.NWTile = nwtile;
        this.SETile = setile;
        this.maxDucks = maxDucks;
        this.plane = nwtile.getPlane();
        SWTile = new WorldPoint(nwtile.getX(), setile.getY(), plane);
        this.museumPond = museumPond;
    }

    public WorldPoint getRandomPointInPond(){
        int maxX = Math.max(NWTile.getX(), SETile.getX()) + 1;
        int minX = Math.min(NWTile.getX(), SETile.getX());
        int maxY = Math.max(NWTile.getY(), SETile.getY()) + 1;
        int minY = Math.min(NWTile.getY(), SETile.getY());

        return new WorldPoint(getRandom(minX, maxX), getRandom(minY, maxY), getPlane());
    }

    public int getPlane(){
        return plane;
    }

    public int getMaxDucks(){
        return maxDucks;
    }

    public int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public boolean compareSWTiles(WorldPoint comparisionPondSWTile){
        return comparisionPondSWTile.distanceTo(SWTile) == 0;
    }
}
