package model;

import java.util.HashSet;

public class Room {
    protected String roomName;
    protected HashSet<String> adjacentRooms = new HashSet<>();
    private int x;
    private int y;
    private int w;
    private int h;
    public boolean isSceneRoom = false;

    public Room(String roomName, HashSet<String> adjacentRooms, int x, int y, int w, int h) {
        this.roomName = roomName;
        this.adjacentRooms = adjacentRooms;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String getName() {
        return roomName;
    }

    // Checks roomName in adjacentRooms
    public Boolean isRoomAdjacent(String roomName) {
        return adjacentRooms.contains(roomName);
    }

    public HashSet<String> getAdjacentRooms() {
        return adjacentRooms;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
    
}
