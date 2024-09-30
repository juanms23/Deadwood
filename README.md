# Deadwood.

## setup
Please note that our program requires JavaFX 17.0.11 to run. Download the SDK from here: https://gluonhq.com/products/javafx/.

After downloading, make note of the location where you saved it.

<br />

To compile, clone the repo locally and then run the following in the terminal from *within* the Deadwood directory:
```
javac --module-path "sdk path" --add-modules javafx.controls,javafx.fxml controller/GUIController.java Deadwood.java
```
Be sure to set "sdk path" to the path of the lib file within the SDK you downloaded. Example: "C:/Program Files/JavaFX/javafx-sdk-17.0.11/lib"

<br />

To run, execute the following in the terminal:
```
java --module-path "C:/Program Files/JavaFX/javafx-sdk-17.0.11/lib" --add-modules javafx.controls,javafx.fxml Deadwood
```

## gameplay
To begin, enter the number of players and each corresponding player name, as prompted in the pop up windows.
To move players between rooms, simply drag the corresponding player's die. The room/role will become highlighted if the user can make the respective move.

*Example*
```
Enter the number of players (2-8): 2
Enter Player 1s name: lars
Enter Player 2s name: carlos
```

Simply press one of the following actions to play the game, found as buttons in the upper right-hand corner of the screen: ```act```, ```rehearse```, ```upgrade```, ```end turn```.

Have fun!
