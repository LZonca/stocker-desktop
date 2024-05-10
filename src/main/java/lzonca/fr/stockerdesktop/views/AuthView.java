package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.responses.UserResponse;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import lzonca.fr.stockerdesktop.system.TokenManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthView {

    @FXML
    public MenuItem frenchMenuItem;

    @FXML
    public MenuItem englishMenuItem;
    @FXML
    public Text passwordLabel;
    @FXML
    public Text emailLabel;
    @FXML
    public Hyperlink noAccount;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private final Preferences prefs = Preferences.userNodeForPackage(AuthView.class);

    private UserResponse userResponse;
    private User user;
    private ResourceBundle labels;

    @FXML
    public void initialize() {
        frenchMenuItem.setOnAction(_ -> setLocale(Locale.of("fr", "FR")));
        englishMenuItem.setOnAction(_ -> setLocale(Locale.of("en", "US")));

        loadResourceBundle();
        updateText();
    }

    private void updateText() {
        // Update the text of your controls here
        loginButton.setText(labels.getString("login"));
        frenchMenuItem.setText(labels.getString("french"));
        englishMenuItem.setText(labels.getString("english"));

        noAccount.setText(labels.getString("noAccount"));
        // Assuming "emailLabel", "passwordLabel", and "errorMessage" are the keys for the email label, password label, and error message respectively
        // Replace these keys with the actual keys used in your resource bundle files
        emailField.setPromptText(labels.getString("email"));
        passwordField.setPromptText(labels.getString("password"));
        emailLabel.setText(labels.getString("email"));
        passwordLabel.setText(labels.getString("password"));
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        System.out.println("Loading resource bundle for locale: " + locale);
        try {
            labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.auth", locale);
            System.out.println("Successfully loaded resource bundle");
        } catch (MissingResourceException e) {
            System.out.println("Failed to load resource bundle: " + e.getMessage());
        }
    }


    private void setLocale(Locale locale) {
        LanguageManager.setLanguage(locale.getLanguage());

        // Close the current stage
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        currentStage.close();

        // Create a new instance of the app and start it
        Platform.runLater(() -> {
            try {
                new App().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public void login() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Email regex pattern
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            // Handle invalid email
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("invalidEmailTitle"), labels.getString("invalidEmailMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.showAndWait();
            return;
        }

        if (password == null || password.isEmpty()) {
            // Handle null or empty password
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("invalidPasswordTitle"), labels.getString("invalidPasswordMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.showAndWait();
            return;
        }

        // Replace this with your actual login logic
        String token = loginWithApi(email, password);

        if (token != null) {
            TokenManager.storeToken(token);
            // Close the current stage
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MainView.fxml"));
            try {
                Scene scene = new Scene(fxmlLoader.load());
                MainView mainView = fxmlLoader.getController();
                mainView.setUser(this.userResponse.getUser()); // Set the user object
                Stage stage = new Stage();
                stage.setTitle("Stocker Desktop");
                Image logo = new Image(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/assets/stocker.png")).toExternalForm());
                stage.getIcons().add(logo);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/css/styles.css")).toExternalForm());
                stage.setScene(scene);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("loginFailedTitle"), labels.getString("loginFailedMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE);
            errorDialog.showAndWait();
        }
    }

    private String loginWithApi(String email, String password) {
        HttpManager httpManager = new HttpManager();
        String json = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        HttpResponse<String> response = null;
        try {
            response = httpManager.post("/login", json);
            System.out.println(response.statusCode());
            ObjectMapper mapper = new ObjectMapper();
            if (response.statusCode() == 302) {
                System.out.println(response.body());
        /*        // Parse the response body to get the error message
                ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("loginFailedTitle"), labels.getString("loginFailedMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE);
                return null;*/
            }

            // Convert JSON response to UserResponse object
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserResponse userResponse = mapper.readValue(response.body(), UserResponse.class);
            this.userResponse = userResponse;

            // Get the token from the UserResponse object
            String token = userResponse.getToken();

            // Get the user from the UserResponse object and store it in the user field
            this.user = userResponse.getUser();

            return token;

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}