package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {

    // Singleton pattern
    private static final GameManager instance = new GameManager(); // eager instantiation

    private GameManager() {
    } // prevent instantiation

    public static GameManager getInstance() {
        return instance;
    }

    // access location manager
    private LocationManager locationManager = LocationManager.getInstance();

    private List<Player> players = new ArrayList<>(); // list of players
    private int activePlayerIndex = 0; // index of active player
    private int dayCnt = 1;

    // init players
    public void initPlayers(List<Player> players) {
        this.players = players;
    }

    // returns player list
    public List<Player> getPlayers() {
        return players;
    }

    // returns active player name
    public String getActivePlayerName() {
        return players.get(activePlayerIndex).getName();
    }

    // returns the number of players
    public int getNumPlayers() {
        return players.size();
    }

    // returns the active player index
    public int getActivePlayerIndex() {
        return activePlayerIndex;
    }

    // returns active player's role
    public String getActivePlayerRole() {
        Role r = players.get(activePlayerIndex).getRole();

        String name = "None";
        if (r != null) {
            name = r.getTitle();
        }

        return name;
    }

    // returns active player's practice chips
    public String getActivePlayerPracticeChips() {
        return Integer.toString(players.get(activePlayerIndex).getPracticeChips());
    }

    // returns active player's score
    public String getActivePlayerScore() {
        return Integer.toString(players.get(activePlayerIndex).getScore());
    }

    // returns true if active player has a role, else false
    public boolean activePlayerHasRole() {
        return players.get(activePlayerIndex).hasRole();
    }

    // set active player's role
    public void setActivePlayerRole(Role role, boolean isOffCardRole) {
        players.get(activePlayerIndex).setRole(role);
        players.get(activePlayerIndex).setWorkingOffCard(isOffCardRole);
    }

    // upgrade active player's rank
    public boolean upgradeActivePlayer(int rank, boolean withDollars) {
        return players.get(activePlayerIndex).upgrade(rank, withDollars);
    }

    // increment active player index. If last in array, reset to beginning
    public void nextActivePlayer() {

        int lastPlayerIndex = players.size() - 1;
        if (activePlayerIndex != lastPlayerIndex) {
            activePlayerIndex++;
        } else {
            activePlayerIndex = 0;
        }

    }

    // returns true if active player rehearses role, else false
    public boolean activePlayerRehearse() {
        SceneRoom activeRoom = (SceneRoom) locationManager.getActiveRoom(players, activePlayerIndex);
        Player activePlayer = players.get(activePlayerIndex);
        if (activePlayer.getPracticeChips() < activeRoom.getScene().getBudget()) {
            return activePlayer.rehearse();
        } else {
            // player has max practice chips
            return false;
        }
    }

    // logic behind active player acting on or off card
    public boolean activePlayerAct(int actValue) {
        //
        Player activePlayer = players.get(activePlayerIndex);
        boolean isOffCard = activePlayer.getWorkingOffCard();
        int movieBudget = ((SceneRoom) locationManager.getActiveRoom(players, activePlayerIndex)).getScene()
                .getBudget();

        if (isOffCard) { // working off card
            if (actValue >= movieBudget) {
                locationManager.removeShotCounter(locationManager.getActivePlayerLocation(players, activePlayerIndex));
                activePlayer.incrementDollars(1);
                activePlayer.incrementCredits(1);
                activePlayer.updateScore();
                return true;
            } else {
                activePlayer.incrementDollars(1);
                activePlayer.updateScore();
                return false;
            }

        } else { // working on card

            if (actValue >= movieBudget) {
                locationManager.removeShotCounter(locationManager.getActivePlayerLocation(players, activePlayerIndex));
                activePlayer.incrementCredits(2);
                activePlayer.updateScore();
                // return true if successful
                return true;
            } else {
                // return false if failed
                return false;
            }

        }
    }

    public int getActivePlayerRank() {
        return players.get(activePlayerIndex).getRank();
    }

    public void updateActivePlayerScore() {
        Player activePlayer = players.get(activePlayerIndex);
        activePlayer.updateScore();
    }

    // ends active player turn, increments active player index , if last in array
    // then set to 0
    public void endTurn() {
        nextActivePlayer();
    }

    // returns all player scores
    public void getPlayerScores() {
        for (Player player : players) {
            player.getScore();
        }
    }

    private void rewardPlayersOnCard(List<Player> playersOnCard, int movieBudget) {
        // roll # of dice = scene budget
        // equal to movie budget
        List<Integer> diceVals = rollDice(movieBudget);
        int j = 0;
        int lastIndex = playersOnCard.size() - 1;
        for (int i = 0; i < diceVals.size(); i++) {
            // reward players on roles with dollars equal to dice values on role
            if (playersOnCard.get(j) != null) {
                // reward players on roles with dollars equal to dice values on role
                playersOnCard.get(j).incrementDollars(diceVals.get(i));
                playersOnCard.get(j).updateScore();
            }
            if (j != lastIndex) {
                j++;
            } else {
                j = 0;
            }
        }
    }

    private void rewardPlayersOffCard(List<Player> playersOffCard) {
        for (Player player : playersOffCard) {
            player.incrementDollars(player.getRole().getLevel());
            player.updateScore();
        }
    }

    // rewards players at the scene
    public void reward() {
        int movieBudget = ((SceneRoom) locationManager.getActiveRoom(players, activePlayerIndex)).getScene()
                .getBudget(); // budget of the movie
        List<Player> playersOnScene = locationManager.getPlayersOnScene(players, activePlayerIndex); // returns list of
                                                                                                     // players on scene

        List<Player> playersOnCard = new ArrayList<>(); // list of players on card
        List<Player> playersOffCard = new ArrayList<>(); // list of players off card

        for (Player player : playersOnScene) {
            if (player.getWorkingOffCard()) {
                playersOffCard.add(player); // add player to off card list
            } else {
                playersOnCard.add(player); // add player to on card list
            }
        }

        // only reward players if there are players on card
        if (!playersOnCard.isEmpty()) {
            // reward players on card
            rewardPlayersOnCard(playersOnCard, movieBudget);

            // reward players off card
            rewardPlayersOffCard(playersOffCard);
        }
    }

    public List<Integer> rollDice(int num) {
        final int NUM_SIDES = 6;
        List<Integer> roll = new ArrayList<>();

        // populate role with rand ints
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            roll.add(rand.nextInt(NUM_SIDES) + 1); // reindex (0->1)
        }

        return roll;
    }

    public void endDay() {
        locationManager.sendPlayersToTrailers(players);
        locationManager.clearSceneCards();
        // set all player roles to null
        for (Player player : players) {
            player.setRole(null);
            player.setPChips(0);
        }
        // reset all role states
        locationManager.resetOffCardRoles();
        locationManager.resetShotCounters();
        locationManager.setActiveSceneCardCnt(10);
        dayCnt++;
    }

    public void endGame() {
    } // TODO: handle end game logic...

    public int getDayCnt() {
        return dayCnt;
    }
}
