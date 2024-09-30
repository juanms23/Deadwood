package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;

public class LocationManager {

    // Singleton pattern
    private static final LocationManager instance = new LocationManager(); // eager instantiation

    private LocationManager() {
    } // prevent instantiation

    public static LocationManager getInstance() {
        return instance;
    }

    // access game manager
    private GameManager gameManager = GameManager.getInstance();

    private int activeSceneCardCnt = 10; // number of active sets
    private List<Room> rooms = new ArrayList<>(); // stores all sets
    private HashMap<String, Integer> roomIds = new HashMap<>(); // stores room names (key) w/ indices (value)
    private List<SceneCard> sceneCardDeck = new Stack<>(); // stores all scene cards
    private int deckIndex = 0;

    // init rooms, room indices, scene cards, and deal cards
    public void init(List<SceneCard> sceneCardDeck, List<Room> rooms) {

        // set rooms and room indices
        this.rooms = rooms;
        for (int i = 0; i < rooms.size(); i++) {
            roomIds.put(rooms.get(i).getName(), i); // stores room name and index
        }

        // set scene cards and shuffle
        this.sceneCardDeck = sceneCardDeck;
        Collections.shuffle(this.sceneCardDeck);
    }

    // deal scene cards to scene rooms on board
    public void dealCards() {

        // for every scene room, set scene card
        for (Room room : rooms) {
            if (room.isSceneRoom) { // maybe change to room instanceof SceneRoom
                SceneCard card = sceneCardDeck.get(deckIndex);
                ((SceneRoom) room).setSceneCard(card);
                deckIndex++; // increment deck index, move to next card and call next time board is dealt
            }
        }
    }

    public boolean sceneRoomIsActive(List<Player> players, int activePlayerIndex) {

        boolean roomIsActive = false;

        Room room = getActiveRoom(players, activePlayerIndex);
        if (room.isSceneRoom) {
            roomIsActive = ((SceneRoom) room).isActive();
        } else {
            return true;
        } // trailers and casting office are always active

        return roomIsActive;

    }

    // returns current active room, based on active player's room
    public Room getActiveRoom(List<Player> players, int activePlayerIndex) {

        // get active player's room name
        String playerRoomName = players.get(activePlayerIndex).getRoomName();
        // get room index from roomIds
        int activeRoomID = roomIds.get(playerRoomName);
        // get room object from rooms

        return rooms.get(activeRoomID);
    }

    // returns active player's location
    public String getActivePlayerLocation(List<Player> players, int activePlayerIndex) {
        return players.get(activePlayerIndex).getRoomName();
    }

    // returns adjacent rooms to active player's room
    public HashSet<String> getAdjacentRooms(List<Player> players, int activePlayerIndex) {

        String playerRoomName = players.get(activePlayerIndex).getRoomName();
        int currentRoom = roomIds.get(playerRoomName);
        return rooms.get(currentRoom).getAdjacentRooms();
    }

    // returns moves active player to "toRoom"
    public void moveActivePlayer(List<Player> players, int activePlayerIndex, String toRoom) {
        players.get(activePlayerIndex).move(toRoom);
    }

    // NOTE: assumes that active scene is a scene room
    public int getActiveSceneShotCnt(List<Player> players, int activePlayerIndex) {
        Room activeRoom = getActiveRoom(players, activePlayerIndex);
        return ((SceneRoom) activeRoom).getShotCounter();
    }

    // returns off card roles for active scene room
    public List<Role> getOffCardRoles(List<Player> players, int activePlayerIndex) {

        List<Role> roles = new ArrayList<>();

        Room activeRoom = getActiveRoom(players, activePlayerIndex);
        if (activeRoom.isSceneRoom) {

            // get all off card roles
            List<Role> tempRoles = new ArrayList<>(((SceneRoom) activeRoom).getOffCardRoles());

            // remove taken roles
            Iterator<Role> itter = tempRoles.iterator();
            while (itter.hasNext()) {
                Role role = itter.next();
                if (role.isTaken()) {
                    itter.remove();
                }
            }
            roles = tempRoles;
        }
        return roles;
    }

    // returns on card roles for active scene room
    public List<Role> getOnCardRoles(List<Player> players, int activePlayerIndex) {

        List<Role> roles = new ArrayList<>();

        Room activeRoom = getActiveRoom(players, activePlayerIndex);
        if (activeRoom.isSceneRoom) {

            // get all on card roles
            SceneCard activeCard = ((SceneRoom) activeRoom).getScene();
            List<Role> tempRoles = new ArrayList<>(activeCard.getOnCardRoles());

            // remove taken roles
            Iterator<Role> itter = tempRoles.iterator();
            while (itter.hasNext()) {
                Role role = itter.next();
                if (role.isTaken()) {
                    itter.remove();
                }
            }
            roles = tempRoles;
        }
        return roles;
    }

    // returns true if room is adjacent to active player's room, else false
    public boolean roomIsAdjacentToActivePlayer(List<Player> players, int activePlayerIndex, String toRoom) {
        return getAdjacentRooms(players, activePlayerIndex).contains(toRoom);
    }

    // removes shot counter from active scene room
    public void removeShotCounter(String roomName) {
        Room activeRoom = rooms.get(roomIds.get(roomName));
        if (activeRoom.isSceneRoom) {
            ((SceneRoom) activeRoom).decrementShotCounter();
        }
    }

    private List<Role> getAllTakenRoles(List<Player> players, int activePlayerIndex) {

        List<Role> roles = new ArrayList<>();

        Room activeRoom = getActiveRoom(players, activePlayerIndex);
        if (activeRoom.isSceneRoom) {

            // get all off card roles
            List<Role> tempRoles = new ArrayList<>(((SceneRoom) activeRoom).getOffCardRoles());

            // get all on card roles
            SceneCard activeCard = ((SceneRoom) activeRoom).getScene();
            tempRoles.addAll(new ArrayList<>(activeCard.getOnCardRoles()));

            // remove untaken roles
            Iterator<Role> itter = tempRoles.iterator();
            while (itter.hasNext()) {
                Role role = itter.next();
                if (!role.isTaken()) {
                    itter.remove();
                }

            }

            roles = tempRoles;

        }

        return roles;

    }

    // sorted highest -> lowest player rank
    public List<Player> getPlayersOnScene(List<Player> players, int activePlayerIndex) {

        // get all on/off card roles that are taken
        List<Role> roles = getAllTakenRoles(players, activePlayerIndex);

        // get players on scene
        List<Player> playersOnScene = new ArrayList<>();
        for (Player p : players) {
            Role pRole = p.getRole();
            if (roles.contains(pRole)) {
                playersOnScene.add(p);
            }
        }

        // sort players based on level
        Collections.sort(playersOnScene, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                int levelDifference = p2.getRole().getLevel() - p1.getRole().getLevel();
                return levelDifference;
            }
        });

        return playersOnScene;

    }

    public void closeSceneCard(List<Player> players, int activePlayerIndex) {
        // set all player roles to null
        List<Player> playersOnScene = new ArrayList<>();
        List<Role> roles = getAllTakenRoles(players, activePlayerIndex);
        for (Player p : players) {
            Role pRole = p.getRole();
            if (roles.contains(pRole)) {
                playersOnScene.add(p);
            }
        }

        for (Player p : playersOnScene) {
            p.setRole(null);
            p.setPChips(0);
        }

        SceneRoom activeRoom = ((SceneRoom) getActiveRoom(players, activePlayerIndex));
        activeRoom.toggleActive();

        activeSceneCardCnt--;

    }

    public void resetOffCardRoles() {
        // for all rooms, reset off card roles
        for (Room room : rooms) {
            if (room.isSceneRoom) {
                List<Role> offCardRoles = ((SceneRoom) room).getOffCardRoles();
                for (Role role : offCardRoles) {
                    role.setTaken(false);
                }
            }
        }
    }

    public void resetShotCounters() {
        // for all rooms, reset shot counters
        for (Room room : rooms) {
            if (room.isSceneRoom) {
                ((SceneRoom) room).resetShotCounter();
            }
        }
    }

    public int getActiveSceneCardCnt() {
        return activeSceneCardCnt;
    }

    public void setActiveSceneCardCnt(int activeSceneCardCnt) {
        this.activeSceneCardCnt = activeSceneCardCnt;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    // resets board, clears scene cards and places new scene cards
    public void clearSceneCards() {
        for (Room room : rooms) {
            if (room.isSceneRoom) {
                ((SceneRoom) room).setActive(); // set room to active
                SceneCard card = sceneCardDeck.get(deckIndex);
                ((SceneRoom) room).setSceneCard(card);
                deckIndex++; // increment deck index, move to next card and call next time board is dealt
            }
        }
    }

    // returns all players to trailers
    public void sendPlayersToTrailers(List<Player> players) {
        for (Player player : players) {
            player.move("trailer");
        }
    }
}