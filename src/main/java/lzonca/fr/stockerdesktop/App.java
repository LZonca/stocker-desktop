package lzonca.fr.stockerdesktop;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.TokenManager;
import lzonca.fr.stockerdesktop.views.HomeView;
import lzonca.fr.stockerdesktop.views.MainView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Objects;

public class App extends Application {
    double x, y;
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader;
        Scene scene;
        if (TokenManager.hasToken()) {
            fxmlLoader = new FXMLLoader(App.class.getResource("MainView.fxml"));
            HttpManager httpManager = new HttpManager();
            HttpResponse<String> userResponse = httpManager.getUser();
            scene = new Scene(fxmlLoader.load());
            MainView mainViewController = fxmlLoader.getController();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            User user = mapper.readValue(userResponse.body(), User.class);
            mainViewController.setUser(user);
        } else {
            fxmlLoader = new FXMLLoader(App.class.getResource("AuthView.fxml"));
            scene = new Scene(fxmlLoader.load());
        }

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