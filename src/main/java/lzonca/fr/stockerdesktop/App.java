package lzonca.fr.stockerdesktop;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import lzonca.fr.stockerdesktop.system.TokenManager;
import lzonca.fr.stockerdesktop.views.MainView;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class App extends Application {
    double x, y;
    Image logo;
    private ResourceBundle labels;

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.Start", locale);
    }

    @Override
    public void start(Stage stage) {
        System.setProperty("javafx.platform", "desktop");
        FXMLLoader fxmlLoader;
        Scene scene;
        loadResourceBundle();
        try {
            if (TokenManager.hasToken()) {
                fxmlLoader = new FXMLLoader(App.class.getResource("MainView.fxml"));
                HttpManager httpManager = new HttpManager();
                HttpResponse<String> userResponse = null;
                try {
                    userResponse = httpManager.getUser();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                scene = new Scene(fxmlLoader.load());
                MainView mainViewController = fxmlLoader.getController();
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                User user;
                assert userResponse != null;
                if (userResponse.body().startsWith("{")) {
                    user = mapper.readValue(userResponse.body(), User.class);
                    mainViewController.setUser(user);
                } else {
                    System.out.println("Unexpected response format: " + userResponse.body());
                    fxmlLoader = new FXMLLoader(App.class.getResource("AuthView.fxml"));
                    scene = new Scene(fxmlLoader.load());
                }
            } else {
                fxmlLoader = new FXMLLoader(App.class.getResource("AuthView.fxml"));
                scene = new Scene(fxmlLoader.load());
            }
        } catch (IOException e) {
            // Display an error message and exit the application or load it in an offline mode
            Platform.runLater(() -> {
                ErrorDialog errorDialog = new ErrorDialog(
                        labels.getString("error"),
                        labels.getString("internet_error"),
                        labels.getString("check_internet"),
                        FontAwesomeSolid.WIFI
                );
                errorDialog.showingProperty().addListener((_, _, newValue) -> {
                    if (!newValue) {
                        Platform.exit();
                    }
                });
                errorDialog.showAndWait();
            });
            return;
        }

        scene.setOnMousePressed(evt -> {
            x = evt.getSceneX();
            y = evt.getSceneY();
        });

        scene.setOnMouseDragged(evt -> {
            stage.setX(evt.getScreenX() - x);
            stage.setY(evt.getScreenY() - y);
        });

        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/styles.css")).toExternalForm());
        stage.setTitle("Stocker Desktop");

        logo = new Image(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/assets/stocker.png")).toExternalForm());
        stage.getIcons().add(logo);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}