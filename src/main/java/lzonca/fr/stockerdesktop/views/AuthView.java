package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.responses.UserResponse;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import lzonca.fr.stockerdesktop.system.TokenManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Locale;
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
        frenchMenuItem.setOnAction(event -> setLocale(Locale.of("fr", "FR")));
        englishMenuItem.setOnAction(event -> setLocale(Locale.of("en", "US")));

        loadResourceBundle();
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.auth", locale);
    }

    private void setLocale(Locale locale) {
        LanguageManager.setLanguage(locale.getLanguage());

        // Reload the resource bundle and the view
        loadResourceBundle();
        reloadView();
    }

    private void reloadView() {
        // Get the current stage
        Stage currentStage = (Stage) loginButton.getScene().getWindow();

        // Load the AuthView again
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AuthView.fxml"), labels);
        try {
            Scene scene = new Scene(fxmlLoader.load());
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            System.out.println("Invalid email");
            return;
        }

        if (password == null || password.isEmpty()) {
            // Handle null or empty password
            System.out.println("Password cannot be null or empty");
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
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Login failed");
            // Handle login failure
        }
    }

    private String loginWithApi(String email, String password) {
        HttpManager httpManager = new HttpManager();
        String json = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
        HttpResponse<String> response = null;
        try {
            response = httpManager.post("/login", json);
            System.out.println(response.statusCode());
            if (response.statusCode() == 302) {
                // Handle invalid credentials
                System.out.println("Invalid credentials");
                return null;
            }

            // Convert JSON response to UserResponse object
            ObjectMapper mapper = new ObjectMapper();
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