// This class acts as the entry point to our Deadwood application

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Deadwood extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // set game title
        primaryStage.setTitle("Deadwood");

        try {

            Parent root = FXMLLoader.load(getClass().getResource("/view/Main.fxml"));
            Scene scene = new Scene(root);

            primaryStage.getIcons().add(new Image("file:view/assets/misc/Deadwood-Icon2.png"));
            primaryStage.setScene(scene);
            // primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
