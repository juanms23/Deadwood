package controller;

import org.w3c.dom.Document;

import model.Role;
import model.SceneCard;
import model.XMLParser;
import model.Room;
import model.GameManager;
import model.LocationManager;
import model.Player;

import view.CommandLineUI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

class GameController {
    // initialize in test controller
    public static void main(String[] args) {
        // init game manager
        GameManager gameManager = GameManager.getInstance();

        // init location manager
        LocationManager locationManager = LocationManager.getInstance();

        // parse game data from XMLs
        XMLParser parser = new XMLParser();

        Document cardDoc;
        Document boardDoc;
        try {
            cardDoc = parser.getDocFromFile("./data/cards.xml");
            boardDoc = parser.getDocFromFile("./data/board.xml");
        } catch (ParserConfigurationException e) {
            System.err.println("Error loading XML file: " + e.getMessage());
            return;
        }

        // init game objects
        List<SceneCard> sceneCards = parser.readCards(cardDoc); // parse card data
        List<Room> rooms = parser.readRooms(boardDoc); // parse room data
        locationManager.init(sceneCards, rooms); // init location manager
        locationManager.dealCards(); // deal scene cards

        // game logic

        // get player names, initilize player objects
        int numPlayers = CommandLineUI.promptNum("Enter the number of players (2-8)", 2, 8);
        ArrayList<String> names = CommandLineUI.getNames(numPlayers);

        // create player objects
        ArrayList<Player> players = new ArrayList<>();
        final String START_ROOM = "trailer";
        for (String name : names) {

            Player player = new Player(name, START_ROOM);

            // init player vals based on number of players
            switch (numPlayers) {
                case 5:
                    player.setCredits(2);
                    break;
                case 6:
                    player.setCredits(4);
                    break;
                case 7:
                    player.setRank(2);
                    break;
                case 8:
                    player.setRank(2);
                    break;
            }

            player.updateScore();

            players.add(player);

        }
        gameManager.initPlayers(players);

        // day loop
        int finalDay = (gameManager.getNumPlayers() < 4) ? 3 : 4;
        while (gameManager.getDayCnt() < finalDay) {

            String playerName = gameManager.getActivePlayerName();
            CommandLineUI.print(playerName + "'s turn.");

            String loc = locationManager.getActivePlayerLocation(gameManager.getPlayers(),
                    gameManager.getActivePlayerIndex());
            String roleName = gameManager.getActivePlayerRole();
            String pChips = gameManager.getActivePlayerPracticeChips();
            String score = gameManager.getActivePlayerScore();

            CommandLineUI.displayActivePlayerStats(loc, roleName, pChips, score);

            boolean hasMoved = false;
            boolean hasWorked = false;
            boolean takenRole = false;

            // turn loop
            // event listeners
            boolean turnOver = false;
            while (!turnOver) {

                String res = CommandLineUI.prompt();

                switch (res.toLowerCase()) {
                    case "move":

                        if (!hasMoved && !gameManager.activePlayerHasRole()) {

                            HashSet<String> adjacentRooms = locationManager.getAdjacentRooms(gameManager.getPlayers(),
                                    gameManager.getActivePlayerIndex());
                            String room = CommandLineUI.getRoom(adjacentRooms);
                            locationManager.moveActivePlayer(gameManager.getPlayers(),
                                    gameManager.getActivePlayerIndex(), room);
                            hasMoved = true;

                        } else if (hasMoved) {
                            CommandLineUI.print("You can only move once per turn.");
                        } else {
                            CommandLineUI.print("You have a role. Finish it before moving.");
                        }

                        break;

                    case "take role":

                        // check scene active
                        if (!locationManager.sceneRoomIsActive(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex())) {
                            CommandLineUI.print("This scene has already been shot.");
                            break;
                        }

                        // ensure player doesn't have a role
                        if (gameManager.activePlayerHasRole()) {
                            CommandLineUI.print("You already have a role. Finish it before taking another.");
                            break;
                        }

                        List<String> offTitles = new ArrayList<>();
                        List<String> offLevels = new ArrayList<>();
                        for (Role role : locationManager.getOffCardRoles(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex())) {
                            offTitles.add(role.getTitle());
                            offLevels.add(Integer.toString(role.getLevel()));
                        }

                        List<String> onTitles = new ArrayList<>();
                        List<String> onLevels = new ArrayList<>();
                        for (Role role : locationManager.getOnCardRoles(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex())) {
                            onTitles.add(role.getTitle());
                            onLevels.add(Integer.toString(role.getLevel()));
                        }

                        if (!offTitles.isEmpty() && !onTitles.isEmpty()) {

                            String selectedRoleName = CommandLineUI.getRole(onTitles, onLevels, offTitles, offLevels);

                            // set active player's role
                            boolean isOffCardRole = offTitles.contains(selectedRoleName);
                            List<Role> roles = (isOffCardRole
                                    ? locationManager.getOffCardRoles(gameManager.getPlayers(),
                                            gameManager.getActivePlayerIndex())
                                    : locationManager.getOnCardRoles(gameManager.getPlayers(),
                                            gameManager.getActivePlayerIndex()));
                            for (Role role : roles) {

                                // find selected role
                                if (!role.getTitle().equals(selectedRoleName)) {
                                    continue;
                                }

                                // check player can take role
                                int playerRank = gameManager.getActivePlayerRank();

                                if (playerRank >= role.getLevel()) {
                                    gameManager.setActivePlayerRole(role, isOffCardRole);
                                    CommandLineUI.print("You have taken the role of " + role.getTitle() + ".");
                                } else {
                                    CommandLineUI
                                            .print("You can not take this role. Level required: " + role.getLevel());
                                    CommandLineUI.print("You have a rank of " + playerRank + ".");
                                }

                                takenRole = true;

                                break;

                            }

                        } else {

                            // in trailers or casting office
                            CommandLineUI.print("You can't take a role from the " + loc + ".");

                        }

                        break;

                    case "act":

                        if (takenRole) {
                            CommandLineUI.print("You just took this role. Wait for your next turn to act.");
                            break;
                        }

                        if (hasWorked) {
                            CommandLineUI.print("You have already worked today.");
                            break;
                        }

                        // ensure player has a role
                        if (!gameManager.activePlayerHasRole()) {
                            CommandLineUI.print("You need a role to act.");
                            break;
                        }

                        // roll dice
                        int rolledValue = gameManager.rollDice(1).get(0);
                        CommandLineUI.print("You rolled a " + rolledValue + ".");

                        // try to act
                        int practiceChips = Integer.parseInt(gameManager.getActivePlayerPracticeChips());
                        int actValue = rolledValue + practiceChips;
                        boolean acted = gameManager.activePlayerAct(actValue);

                        if (acted) {
                            CommandLineUI.print("Nice! You made the cut.");
                        } else {
                            CommandLineUI.print("You didn't make the cut.");
                        }

                        hasWorked = true;

                        // check scene over
                        if (locationManager.getActiveSceneShotCnt(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex()) == 0) {

                            CommandLineUI.print("That's a wrap! This scene is all finished.");

                            gameManager.reward();
                            locationManager.closeSceneCard(gameManager.getPlayers(),
                                    gameManager.getActivePlayerIndex());

                        }

                        // check day ended
                        int sceneCardsLeft = locationManager.getActiveSceneCardCnt();
                        if (sceneCardsLeft == 1) {

                            CommandLineUI.print("The day has ended.");
                            gameManager.endDay();
                            turnOver = true;

                        }

                        break;

                    case "rehearse":

                        if (takenRole) {
                            CommandLineUI.print("You just took this role. Wait for your next turn to rehearse.");
                            break;
                        }

                        if (hasWorked) {
                            CommandLineUI.print("You have already worked today.");
                            break;
                        }

                        // ensure player has a role
                        if (!gameManager.activePlayerHasRole()) {
                            CommandLineUI.print("You need a role to rehearse.");
                            break;
                        }

                        // ensure player's practice chips are <= role level
                        boolean rehearsed = gameManager.activePlayerRehearse();

                        if (rehearsed) {
                            CommandLineUI.print("Looking good.");
                        } else {
                            CommandLineUI.print("You didn't make the cut.");
                        }

                        hasWorked = true;

                        break;

                    case "upgrade":

                        loc = locationManager.getActivePlayerLocation(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex());
                        boolean inCastingOffice = loc.equals("office");
                        if (!inCastingOffice) {
                            CommandLineUI.print("Please go to the casting office to upgrade.");
                            break;
                        }

                        // get upgrade params from user
                        final int LOWEST_RANK = 2;
                        final int HIGHEST_RANK = 6;
                        int rank = CommandLineUI.promptNum("Enter a rank [2-6]", LOWEST_RANK, HIGHEST_RANK);
                        String payment = CommandLineUI.chooseBetweenTwo(
                                "Would you like to pay with dollars or credits?", "Payment", "dollars", "credits");

                        // try to upgrade
                        boolean upgraded = gameManager.upgradeActivePlayer(rank, payment.equals("dollars"));
                        if (upgraded) {
                            CommandLineUI.print("Upgraded to rank " + rank + ".");
                            gameManager.updateActivePlayerScore();
                        } else {
                            CommandLineUI.print("Unable to upgrade. Get your bag up.");
                        }

                        break;

                    case "end turn":
                        turnOver = true;
                        break;

                    default:
                        CommandLineUI.print(
                                "Please choose from the following actions: move, take role, act, rehearse, upgrade, or end turn.");
                }
            }

            // next player's turn
            gameManager.nextActivePlayer();

        }

        CommandLineUI.print("End.");

        int highestScore = 0;
        String winner = "";
        for (Player p : players) {
            String name = p.getName();
            int score = p.getScore();

            CommandLineUI.print(name + ": " + score);

            if (score > highestScore) {
                score = highestScore;
                winner = name;
            }
        }

        CommandLineUI.print("The winner is: " + winner);

        // cleanup
        CommandLineUI.closeScanner();

    }
}
