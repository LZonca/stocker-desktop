package lzonca.fr.stockerdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    double x, y;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        scene.setOnMouseDragged(evt -> {
            stage.setX(evt.getScreenX() - x);
            stage.setY(evt.getScreenY() - y);
        });

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/styles.css")).toExternalForm());
        stage.setTitle("Stocker Desktop");

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/assets/stocker.png")).toExternalForm()));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
