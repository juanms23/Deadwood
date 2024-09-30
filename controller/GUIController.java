package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;

import model.GameManager;
import model.LocationManager;
import model.Player;
import model.Role;
import model.Room;
import model.SceneCard;
import model.SceneRoom;
import model.XMLParser;

import org.w3c.dom.Document;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

public class GUIController {

    // NOTE: if you don't need to include local references use this:
    // @FXML

    // injects fxml objects, creates local references
    @FXML
    private AnchorPane bg;
    @FXML
    private TableView<Player> playerTable;
    @FXML
    private TableColumn<Player, String> nameCol;
    @FXML
    private TableColumn<Player, String> dollarsCol;
    @FXML
    private TableColumn<Player, String> creditsCol;
    @FXML
    private TableColumn<Player, String> chipsCol;

    // access to Singleton elements, managers
    GameManager gameManager = GameManager.getInstance();
    LocationManager locationManager = LocationManager.getInstance();

    // game objects
    List<SceneCard> sceneCards;
    List<Room> allRooms;
    private int numPlayers;
    private ArrayList<Player> players;

    // player dice, scene cards, facedown cards
    private ImageView[] dice;
    private ImageView[] cards;
    private List<ImageView> roleButtons = new ArrayList<>();
    private ImageView[] faceDownCards;

    // colors for player dice
    final String[] colors = { "b", "c", "g", "o", "p", "r", "v", "w", "y" };

    // shot counters for each scene room
    private HashMap<String, ImageView[]> allShotCounters = new HashMap<>();

    // available roles for each scene card
    private HashMap<String, HashSet<String>> availableRoles = new HashMap<>();

    // game state variables
    boolean hasMoved = false;
    boolean hasWorked = false;
    boolean takenRole = false;
    int finalDay;

    // rank constants
    static final int LOWEST_RANK = 2;
    static final int HIGHEST_RANK = 6;

    @FXML
    private void initialize() {

        // parse game data from XML files
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
        sceneCards = parser.readCards(cardDoc); // parse card data
        allRooms = parser.readRooms(boardDoc); // parse room data

        locationManager.init(sceneCards, allRooms); // init location manager

        // create shot counters for each room
        for (Room room : allRooms) {
            if (room.isSceneRoom) {
                SceneRoom sceneRoom = (SceneRoom) room;

                // place shot counters onto board
                int numShots = sceneRoom.getShotCounters().size();
                ImageView[] shotCounters = new ImageView[numShots];
                for (int i = 0; i < numShots; i++) {
                    ImageView shotCounter = new ImageView();
                    shotCounter.setImage(new Image("/view/assets/misc/shot.png"));
                    shotCounter.setFitHeight(sceneRoom.getShotCounters().get(i).getH());
                    shotCounter.setFitWidth(sceneRoom.getShotCounters().get(i).getW());
                    shotCounter.setLayoutX(sceneRoom.getShotCounters().get(i).getX());
                    shotCounter.setLayoutY(sceneRoom.getShotCounters().get(i).getY());

                    bg.getChildren().add(shotCounter); // add to board
                    shotCounters[i] = shotCounter; // store in map
                }
                allShotCounters.put(sceneRoom.getName(), shotCounters);
            }
        }

        locationManager.dealCards();

        // create scene cards after location manager shuffles
        List<Room> rooms = locationManager.getRooms();
        cards = new ImageView[rooms.size()];
        faceDownCards = new ImageView[rooms.size()];
        for (int i = 0; i < rooms.size(); i++) {

            if (!rooms.get(i).isSceneRoom) {
                continue;
            } // trailers/office don't have scene cards

            SceneRoom room = (SceneRoom) rooms.get(i);
            SceneCard card = room.getScene();

            cards[i] = new ImageView();
            cards[i].setId(card.getName());
            cards[i].setImage(new Image("/view/assets/cards/" + card.getImgName()));
            cards[i].setFitHeight(room.getH());
            cards[i].setFitWidth(room.getW());
            cards[i].setLayoutX(room.getX());
            cards[i].setLayoutY(room.getY());

            // add card to board
            bg.getChildren().add(cards[i]);
            // add buttons to card, for each role
            int j = 0;
            HashSet<String> roles = new HashSet<>();
            for (Role role : card.getOnCardRoles()) {

                // create ImageView for role
                ImageView roleSquare = new ImageView();
                String level = Integer.toString(role.getLevel());
                roleSquare.setImage(new Image("/view/assets/dice/w" + level + ".png"));
                roleSquare.setOpacity(1);
                roleSquare.setId(role.getTitle());
                SepiaTone hue = new SepiaTone(0);
                roleSquare.setEffect(hue);

                // no image, set button onDragOver
                roleSquare.setOnDragOver(this::draggingOver);
                roleSquare.setOnDragDropped(this::dropped);

                if (card.getOnCardRoles().size() == 3) {

                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 17 + j * 63);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);

                } else if (sceneCards.get(i).getOnCardRoles().size() == 2) {
                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 50 + j * 63);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);

                } else {
                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 80);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);
                }
                // add button to card
                bg.getChildren().add(roleSquare);
                roles.add(role.getTitle());
                roleButtons.add(roleSquare);

                j++;
            }

            availableRoles.put(card.getName(), roles);

            // create face down cards onto board, over scene cards
            faceDownCards[i] = new ImageView();
            faceDownCards[i].setId(room.getName() + "FaceDown");
            faceDownCards[i].setImage(new Image("/view/assets/misc/" + "CardBack-small.jpg"));
            faceDownCards[i].setFitHeight(room.getH());
            faceDownCards[i].setFitWidth(room.getW());
            faceDownCards[i].setLayoutX(room.getX());
            faceDownCards[i].setLayoutY(room.getY());

            // place face down card on top of scene card
            bg.getChildren().add(faceDownCards[i]);
        }

        // get user input for number of players
        // get player names, initilize player objects
        numPlayers = promptIntRange("Number of players (2-8)", 2, 8);
        ArrayList<String> names = getNames(numPlayers);

        players = new ArrayList<>();
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
                default:
                    // do nothing
                    break;
            }

            player.updateScore();

            players.add(player);
        }

        gameManager.initPlayers(players);

        finalDay = (gameManager.getNumPlayers() < 4) ? 3 : 4;

        // set table columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("Name"));
        dollarsCol.setCellValueFactory(new PropertyValueFactory<>("Dollars"));
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("Credits"));
        chipsCol.setCellValueFactory(new PropertyValueFactory<>("PracticeChips"));

        playerTable.setItems(getPlayerBoard());

        // set current player row to bold
        playerTable.setRowFactory(tv -> new TableRow<Player>() {
            @Override
            public void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.getName().equals(gameManager.getActivePlayerName())) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        // create dice objects based on input
        dice = new ImageView[numPlayers];
        for (int i = 0; i < numPlayers; i++) {

            dice[i] = new ImageView();
            dice[i].setId(players.get(i).getName());
            dice[i].setFitHeight(40);
            dice[i].setFitWidth(40);
            dice[i].setOnDragDetected(this::dragPlayer);
            dice[i].setPickOnBounds(true);
            dice[i].setPreserveRatio(true);

            if (i > 3) {
                dice[i].setLayoutX(998 + (i - 4) * 50);
                dice[i].setLayoutY(320);
            } else {
                dice[i].setLayoutX(998 + i * 50);
                dice[i].setLayoutY(270);
            }

            // set image to dice of player color
            String rank = Integer.toString(players.get(i).getRank());
            String color = colors[i];
            dice[i].setImage(new Image("/view/assets/dice/" + color + rank + ".png"));

            bg.getChildren().add(dice[i]);
        }
    }

    private ArrayList<String> getNames(Integer numPlayers) {

        ArrayList<String> res = new ArrayList<>();

        TextInputDialog prompt = new TextInputDialog();
        String name = null;
        for (int i = 1; i <= numPlayers; i++) {

            // get unique player name
            do {
                prompt.getEditor().clear();
                prompt.setTitle("Enter player " + i + "'s name");
                prompt.setHeaderText(null);
                prompt.setGraphic(null);
                prompt.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
                prompt.showAndWait();

                String val = prompt.getResult();
                if (res.contains(val)) {
                    promptErrMsg("This player name already exists.");
                } else {
                    name = val;
                }

            } while (name == null);

            res.add(name);
            name = null;

        }

        return res;
    }

    // prompts user for integer input within a range
    private Integer promptIntRange(String msg, int low, int high) {

        TextInputDialog prompt = new TextInputDialog();

        Integer res = null;
        do {
            prompt.getEditor().clear();
            prompt.setTitle(msg);
            prompt.setHeaderText(null);
            prompt.setGraphic(null);
            prompt.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
            prompt.showAndWait();

            String val = prompt.getResult();
            try {
                res = Integer.parseInt(val);

                if (res < low || res > high) {
                    promptErrMsg("Please enter between " + low + " and " + high + " players.");
                    res = null;
                }

            } catch (NumberFormatException e) {
                promptErrMsg("Please enter a valid number.");
            }

        } while (res == null);

        return res;

    }

    // prompts user to choose between two options
    private String chooseBetweenTwo(String msg, String prompt) {
        TextInputDialog input = new TextInputDialog();
        String res = null;
        do {
            input.getEditor().clear();
            input.setTitle(msg);
            input.setHeaderText(null);
            input.setGraphic(null);
            input.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
            input.getDialogPane().setPrefSize(300, 200);
            input.showAndWait();

            res = input.getResult();
            if (!res.equals("dollars") && !res.equals("credits")) {
                promptErrMsg("Please enter either dollars or credits.");
                res = null;
            }

        } while (res == null);

        return res;
    }

    // prompts error message to user
    private void promptErrMsg(String msg) {
        Dialog<TextField> dialog = new Dialog<>();
        dialog.setTitle("Error");
        dialog.setContentText(msg);
        ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.showAndWait();
    }

    // prompts message to user
    private void promptMsg(String msg) {
        Dialog<TextField> dialog = new Dialog<>();
        dialog.setTitle("Result");
        dialog.setContentText(msg);
        ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.showAndWait();
    }

    // prompts end game message
    private void endGameMsg() {

        int highestScore = 0;
        String winner = "";
        for (Player p : players) {
            String name = p.getName();
            int score = p.getScore();
            if (score > highestScore) {
                highestScore = score;
                winner = name;
            }
        }
        Dialog<TextField> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setContentText("The winner is: " + winner + " with a score of " + highestScore + "!");
        ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.showAndWait();
    }

    // allow active player to move, only if they haven't moved yet
    // will handle moving player to adjacent room in draggingOver method
    private ImageView myCard;
    private HashSet<String> adjacentRooms = new HashSet<>();
    private String activePlayerLoc;
    private List<String> offTitles = new ArrayList<>();
    private List<String> offLevels = new ArrayList<>();
    private List<String> onTitles = new ArrayList<>();
    private List<String> onLevels = new ArrayList<>();

    public void dragPlayer(MouseEvent e) {

        // ensure player can move
        if (!hasMoved && !gameManager.activePlayerHasRole() || !takenRole && !gameManager.activePlayerHasRole()) {

            // set adjacent rooms when drag player starts
            adjacentRooms = locationManager.getAdjacentRooms(gameManager.getPlayers(),
                    gameManager.getActivePlayerIndex());

            ImageView player = (ImageView) e.getTarget();
            String draggedPlayerName = player.getId();
            String activePlayerName = gameManager.getActivePlayerName();

            // only allow active player to move, begin drag
            if (draggedPlayerName.equals(activePlayerName)) {

                myCard = (ImageView) e.getSource();

                Dragboard db = myCard.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content = new ClipboardContent();
                content.putImage(myCard.getImage());
                db.setContent(content);

            }

        } else if (hasMoved) {
            promptErrMsg("You can only move once per turn.");
        } else {
            promptErrMsg("You have a role. Finish it before moving.");
        }
        e.consume();
    }

    // state as player is dragged over rooms
    private ImageView hoveredRoom;

    public void draggingOver(DragEvent e) {

        ImageView hoveredItem = (ImageView) e.getSource();

        // get off card roles for room
        for (Role role : locationManager.getOffCardRoles(gameManager.getPlayers(),
                gameManager.getActivePlayerIndex())) {
            offTitles.add(role.getTitle());
            offLevels.add(Integer.toString(role.getLevel()));
        }

        // get on card roles for room
        for (Role role : locationManager.getOnCardRoles(gameManager.getPlayers(),
                gameManager.getActivePlayerIndex())) {
            onTitles.add(role.getTitle());
            onLevels.add(Integer.toString(role.getLevel()));
        }

        // only allow move if room is neighbor or if role is availble in room or if role
        // is on scene card
        if ((adjacentRooms.contains(hoveredItem.getId()) && !hasMoved) ||
                (offTitles.contains(hoveredItem.getId()) || onTitles.contains(hoveredItem.getId())) && !takenRole) {

            // make room glow
            SepiaTone hue = (SepiaTone) hoveredItem.getEffect();
            hue.setLevel(0.3);

            // hovering over new room
            if (hoveredRoom != null && !hoveredItem.equals(hoveredRoom)) {
                // reset set old room
                SepiaTone oldHue = (SepiaTone) hoveredRoom.getEffect();
                oldHue.setLevel(0);
            }
            hoveredRoom = hoveredItem;

            e.acceptTransferModes(TransferMode.MOVE);

        }

        e.consume();

    }

    public void dropped(DragEvent e) {

        ImageView source = (ImageView) e.getGestureSource();
        ImageView target = (ImageView) e.getGestureTarget();

        if (adjacentRooms.contains(target.getId())) {

            // move player to room, player can only move once per turn
            locationManager.moveActivePlayer(gameManager.getPlayers(), gameManager.getActivePlayerIndex(),
                    target.getId());
            hasMoved = true;

            // remove face down card if present and if scene room
            if (locationManager.getActivePlayerLocation(players, gameManager.getActivePlayerIndex()).equals("trailer")
                    || locationManager.getActivePlayerLocation(players, gameManager.getActivePlayerIndex())
                            .equals("office")) {
                // do nothing
            } else {
                for (ImageView faceDownCard : faceDownCards) {
                    if (faceDownCard.getId().equals(locationManager.getActivePlayerLocation(players,
                            gameManager.getActivePlayerIndex()) + "FaceDown")) {
                        faceDownCard.toBack();
                        break;
                    }
                }
            }

            SepiaTone hue = (SepiaTone) target.getEffect();
            hue.setLevel(0);

            hoveredRoom = null;

            double playerW = source.getFitWidth();
            double playerH = source.getFitHeight();
            myCard.setLayoutX(e.getSceneX() - playerW / 2);
            myCard.setLayoutY(e.getSceneY() - playerH / 2);

        } else {
            hasMoved = true;

            // check scene active
            if (!locationManager.sceneRoomIsActive(gameManager.getPlayers(),
                    gameManager.getActivePlayerIndex())) {
                promptErrMsg("Scene is not active.");
                e.consume();
                // check if player has role
            } else if (gameManager.activePlayerHasRole()) {
                promptErrMsg("Player has role.");
                e.consume();
            } else if (!offTitles.isEmpty() && !onTitles.isEmpty()) {
                // set active player role
                boolean isOffCardRole = offTitles.contains(target.getId());
                List<Role> roles = isOffCardRole
                        ? locationManager.getOffCardRoles(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex())
                        : locationManager.getOnCardRoles(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex());

                for (Role role : roles) {

                    // find role dropped on
                    if (!role.getTitle().equals(target.getId())) {
                        continue;
                    }

                    // check if player can take role
                    int rank = gameManager.getActivePlayerRank();
                    if (rank < role.getLevel()) {
                        promptErrMsg("Player rank too low.");
                        e.consume();
                    } else {
                        // remove face down card if present
                        for (ImageView faceDownCard : faceDownCards) {
                            if (faceDownCard.getId().equals(locationManager.getActivePlayerLocation(players,
                                    gameManager.getActivePlayerIndex()) + "FaceDown")) {
                                faceDownCard.toBack();
                                break;
                            }
                        }

                        // set player role, taken role
                        gameManager.setActivePlayerRole(role, isOffCardRole);
                        takenRole = true;
                        promptMsg("Role, " + role.getTitle() + ", taken.");

                        // disaplay player role
                        double playerW = source.getFitWidth();
                        double playerH = source.getFitHeight();
                        myCard.setLayoutX(e.getSceneX() - playerW / 2);
                        myCard.setLayoutY(e.getSceneY() - playerH / 2);

                    }
                }

            }

        }
        e.consume();
    }

    // add act logic here
    public void actPressed(ActionEvent e) {
        if (takenRole) {
            promptErrMsg("You just took a role. You can't act.");
            e.consume();
        } else if (hasWorked) {
            promptErrMsg("You can only act once per turn.");
            e.consume();
        } else if (!gameManager.activePlayerHasRole()) {
            promptErrMsg("You need a role to act.");
            e.consume();
        } else {

            // roll dice
            int rolledValue = gameManager.rollDice(1).get(0);

            // try to act
            int practiceChips = Integer.parseInt(gameManager.getActivePlayerPracticeChips());
            int actValue = rolledValue + practiceChips;
            boolean acted = gameManager.activePlayerAct(actValue);

            if (acted) {
                promptMsg("Good Acting! You rolled: " + rolledValue + "!");
                // remove a shot counter
                for (ImageView shotCounter : allShotCounters
                        .get(locationManager.getActivePlayerLocation(gameManager.getPlayers(),
                                gameManager.getActivePlayerIndex()))) {
                    if (shotCounter.isVisible()) {
                        shotCounter.setVisible(false);
                        break;
                    }
                }

                gameManager.updateActivePlayerScore();

                // update visible score
                playerTable.setRowFactory(tv -> new TableRow<Player>() {
                    @Override
                    public void updateItem(Player item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                });

            } else {
                promptMsg("You can't act. You rolled " + rolledValue + "! Bad acting.");
            }

            hasWorked = true;

            // check if scene is over
            if (locationManager.getActiveSceneShotCnt(gameManager.getPlayers(),
                    gameManager.getActivePlayerIndex()) == 0) {
                promptMsg("That's a wrap! This scene is over.");

                gameManager.reward();
                locationManager.closeSceneCard(gameManager.getPlayers(), gameManager.getActivePlayerIndex());

                // check if day is over
                int sceneCardsLeft = locationManager.getActiveSceneCardCnt();
                if (sceneCardsLeft == 1) {
                    promptMsg(locationManager.getActivePlayerLocation(players, gameManager.getActivePlayerIndex())
                            + " is the last set. Day is over.");
                    gameManager.endDay(); // sends players to trailer, clears/resets scene cards, resets board, and
                    if (gameManager.getDayCnt() == finalDay) {
                        endGameMsg();
                        e.consume();
                    } else {

                        removeRoles(); // removes all role buttons
                        // increments day
                        setNewSceneCards(); // sets new scene cards

                        // display players dice back to trailers
                        for (int i = 0; i < numPlayers; i++) {
                            dice[i].setLayoutX(998 + i * 50);
                            dice[i].setLayoutY(270);
                        }

                        // reset shot counters
                        setShotCounters();

                        hasMoved = false;
                        hasWorked = false;
                        takenRole = false;

                        gameManager.nextActivePlayer();
                    }
                }

                // indicate current player, set row to bold
                playerTable.setRowFactory(tv -> new TableRow<Player>() {
                    @Override
                    public void updateItem(Player item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setStyle("");
                        } else if (item.getName().equals(gameManager.getActivePlayerName())) {
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                });
            }

            e.consume();
        }
    }

    // add rehearse logic here
    public void rehearsePressed(ActionEvent e) {
        if (takenRole) {
            promptErrMsg("You just took a role. You can't rehearse.");
            e.consume();
        } else if (hasWorked) {
            promptErrMsg("You can only rehearse once per turn.");
            e.consume();
        } else if (!gameManager.activePlayerHasRole()) {
            promptErrMsg("You need a role to rehearse.");
            e.consume();
        } else {

            boolean rehearsed = gameManager.activePlayerRehearse();

            if (rehearsed) {
                promptMsg("You rehearsed. Good job.");

                gameManager.updateActivePlayerScore();

                // update visible score
                playerTable.setRowFactory(tv -> new TableRow<Player>() {
                    @Override
                    public void updateItem(Player item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                });

            } else {
                promptErrMsg("You can't rehearse.");
            }

        }

        hasWorked = true;

        e.consume();
    }

    // add upgrade logic here
    public void upgradePressed(ActionEvent e) {
        activePlayerLoc = locationManager.getActivePlayerLocation(players, gameManager.getActivePlayerIndex());
        boolean inCastingOffice = activePlayerLoc.equals("office");
        if (!inCastingOffice) {
            promptErrMsg("Please go to the casting office to upgrade.");
            e.consume();
        } else {

            // get upgrade params from user
            int rank = promptIntRange("Enter the rank you want to upgrade to (2-6)", LOWEST_RANK, HIGHEST_RANK);
            String payment = chooseBetweenTwo("Would you like to use dollars or credits?", "Payment");

            // try to upgrade
            boolean upgraded = gameManager.upgradeActivePlayer(rank, payment.equals("dollars"));
            if (upgraded) {
                gameManager.updateActivePlayerScore();
                // change player dice image to new rank
                String color = colors[gameManager.getActivePlayerIndex()];
                dice[gameManager.getActivePlayerIndex()]
                        .setImage(new Image("/view/assets/dice/" + color + rank + ".png"));

                // update visible score
                playerTable.setRowFactory(tv -> new TableRow<Player>() {
                    @Override
                    public void updateItem(Player item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                });

            } else {
                promptErrMsg("You can't upgrade. Get your bag up.");
            }

        }
        e.consume();
    }

    // add end turn here
    public void endTurnPressed(ActionEvent e) {
        gameManager.nextActivePlayer();
        // reset hasMoved, hasActed, hasRehearsed
        hasMoved = false;
        hasWorked = false;
        takenRole = false;
        // indicate current player, set row to bold
        playerTable.setRowFactory(tv -> new TableRow<Player>() {
            @Override
            public void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.getName().equals(gameManager.getActivePlayerName())) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        e.consume();
    }

    // set shot counters on board
    public void setShotCounters() {
        for (Room room : allRooms) {
            if (room.isSceneRoom) {
                SceneRoom sceneRoom = (SceneRoom) room;
                int numShots = sceneRoom.getShotCounters().size();
                for (int i = 0; i < numShots; i++) {
                    allShotCounters.get(sceneRoom.getName())[i].setVisible(true);
                }
            }
        }
    }

    // remove old scene cards from board
    public void removeRoles() {
        // remove role buttons, for each card
        for (ImageView roleButton : roleButtons) {
            bg.getChildren().remove(roleButton);
        }
        // clear role buttons
        roleButtons.clear();
    }

    // set new scene cards on board
    public void setNewSceneCards() {
        // get rooms
        List<Room> rooms = locationManager.getRooms();
        // get new scene cards
        for (int i = 0; i < rooms.size(); i++) {
            if (!rooms.get(i).isSceneRoom) {
                continue;
            } // trailers/office don't have scene cards

            SceneRoom room = (SceneRoom) rooms.get(i);
            SceneCard card = room.getScene();

            cards[i].setId(card.getName());
            cards[i].setImage(new Image("/view/assets/cards/" + card.getImgName()));
            cards[i].toFront();

            // add buttons to card, for each role
            int j = 0;

            for (Role role : card.getOnCardRoles()) {

                // create ImageView for role
                ImageView roleSquare = new ImageView();
                String level = Integer.toString(role.getLevel());
                roleSquare.setImage(new Image("/view/assets/dice/w" + level + ".png"));
                roleSquare.setOpacity(1);
                roleSquare.setId(role.getTitle());
                SepiaTone hue = new SepiaTone(0);
                roleSquare.setEffect(hue);

                // no image, set button onDragOver
                roleSquare.setOnDragOver(this::draggingOver);
                roleSquare.setOnDragDropped(this::dropped);

                // set button position
                if (card.getOnCardRoles().size() == 3) {

                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 17 + j * 63);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);

                } else if (sceneCards.get(i).getOnCardRoles().size() == 2) {
                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 50 + j * 63);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);

                } else {
                    roleSquare.setFitHeight(45);
                    roleSquare.setFitWidth(46);

                    // set button position
                    roleSquare.setLayoutX(cards[i].getLayoutX() + 80);
                    roleSquare.setLayoutY(cards[i].getLayoutY() + 45);
                }
                // add button to card
                bg.getChildren().add(roleSquare);
                roleButtons.add(roleSquare);

                j++;
            }

            // moved facedown card to front
            faceDownCards[i].toFront();

            // ensuer player are top in higherarchy
            for (ImageView die : dice) {
                die.toFront();
            }
        }
    }

    public ObservableList<Player> getPlayerBoard() {

        ObservableList<Player> playerBoard = FXCollections.observableArrayList();

        // add players from user input
        for (int i = 0; i < numPlayers; i++) {
            playerBoard.add(players.get(i));
        }
        return playerBoard;

    }
}
