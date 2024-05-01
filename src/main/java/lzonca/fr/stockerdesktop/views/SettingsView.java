package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.interfaces.LocaleChangeListener;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import lzonca.fr.stockerdesktop.system.TokenManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsView {

    @FXML
    public MenuItem frenchMenuItem;
    @FXML
    public MenuItem englishMenuItem;
    @FXML
    public Label currentMailLabel;

    @FXML
    private javafx.scene.control.TextField emailField;

    @FXML
    private javafx.scene.control.TextField passwordField;

    @FXML
    private javafx.scene.control.TextField passwordConfirmationField;

    @FXML
    private javafx.scene.control.Button logoutButton;

    @FXML
    private Button changeEmailButton;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Label currentPasswordLabel;
    @FXML
    private Label confirmPasswordLabel;

    private User user;
    private ResourceBundle labels;
    private LocaleChangeListener localeChangeListener;
    private MainView mainView;

    @FXML
    public void initialize() {
        loadResourceBundle();
        String language = LanguageManager.getLanguage();

        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        ResourceBundle labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.views.SettingsView", locale);

        frenchMenuItem.setText(labels.getString("french"));
        englishMenuItem.setText(labels.getString("english"));

        // Set the text of the buttons
        changeEmailButton.setText(labels.getString("changeEmail"));
        changePasswordButton.setText(labels.getString("changePassword"));
        logoutButton.setText(labels.getString("logout"));

        // Set the text of the labels
        currentPasswordLabel.setText(labels.getString("currentPassword"));
        confirmPasswordLabel.setText(labels.getString("confirmPassword"));

        // Set the text of the currentMailLabel
        if (user != null) {
            currentMailLabel.setText(labels.getString("currentEmail") + ": " + user.getEmail());
        }

        frenchMenuItem.setOnAction(event -> setLocale(Locale.of("fr", "FR")));
        englishMenuItem.setOnAction(event -> setLocale(Locale.of("en", "US")));
    }


    public void setLocaleChangeListener(LocaleChangeListener localeChangeListener) {
        this.localeChangeListener = localeChangeListener;
    }

    private void setLocale(Locale locale) {
        LanguageManager.setLanguage(locale.getLanguage());

        // Reload the resource bundle
        loadResourceBundle();

        // Close the current stage
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        currentStage.close();

        // Create a new instance of the app and start it
        App newApp = new App();
        Stage newStage = new Stage();
        try {
            newApp.start(newStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.views.SettingsView", locale);
    }

    public void setUser(User user) {
        this.user = user;
        System.out.println("User: " + user);
        if (user != null) {

            currentMailLabel.setText(labels.getString("currentEmail") + ": " + user.getEmail());
        }
    }
    @FXML
    private void logout(){
        TokenManager.removeToken();
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        currentStage.close();
        FXMLLoader fxmlLoader;
        try {
            fxmlLoader = new FXMLLoader(App.class.getResource("AuthView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Add your other methods here...
}