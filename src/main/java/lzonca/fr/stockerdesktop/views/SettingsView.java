package lzonca.fr.stockerdesktop.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import lzonca.fr.stockerdesktop.system.TokenManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsView {

    @FXML
    public MenuItem frenchMenuItem;
    @FXML
    public MenuItem englishMenuItem;
    @FXML
    public Label currentMailLabel;
    @FXML
    public Label localeLabel;
    @FXML
    public Button changeNameButton;
    @FXML
    public TextField nameField;
    public Label currentNameLabel;

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

    @FXML
    public void initialize() {
        changePasswordButton.setOnAction(_ -> {
            try {
                editPassword();
                ErrorDialog errorDialog = new ErrorDialog(labels.getString("success"), labels.getString("passwordChangedTitle"), labels.getString("passwordChangedMessage"), FontAwesomeSolid.CHECK);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        changeEmailButton.setOnAction(_ -> {
            try {
                editEmail();

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        changeNameButton.setOnAction(_ -> {
            try {
                editName();

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        loadResourceBundle();
        String language = LanguageManager.getLanguage();

        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        ResourceBundle labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.SettingsView", locale);

        updateText(labels);
    }

    private void editName() throws IOException, InterruptedException {
        String name = nameField.getText();
        if (name.isEmpty()) {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("invalidUsernameTitle"), labels.getString("invalidUsernameMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.show();
            return;
        }
        HttpManager httpManager = new HttpManager();
        httpManager.updateName(name);
        user.setName(name);
        Platform.runLater(() -> {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("success"), labels.getString("nameChangedTitle"), labels.getString("nameChangedMessage"), FontAwesomeSolid.CHECK);
            errorDialog.show();
            currentNameLabel.setText(labels.getString("currentName") + ": " + user.getName());
        });
    }

    private void editEmail() throws IOException, InterruptedException {
        String email = emailField.getText();
        if (email.isEmpty()) {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("invalidEmailTitle"), labels.getString("invalidEmailMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.show();
            return;
        }
        HttpManager httpManager = new HttpManager();
        httpManager.updateEmail(email);
        user.setEmail(email);
        Platform.runLater(() -> {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("success"), labels.getString("emailChangedTitle"), labels.getString("emailChangedMessage"), FontAwesomeSolid.CHECK);
            errorDialog.show();
            currentMailLabel.setText(labels.getString("currentEmail") + ": " + user.getEmail());
        });
    }

    public void editPassword() throws IOException, InterruptedException {
        String password = passwordField.getText();
        String passwordConfirmation = passwordConfirmationField.getText();
        if (password.isEmpty() || passwordConfirmation.isEmpty()) {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("passwordsDoNotMatchTitle"), labels.getString("passwordsDoNotMatchMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.show();
            return;
        }
        if (!password.equals(passwordConfirmation)) {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("passwordsDoNotMatchTitle"), labels.getString("passwordsDoNotMatchMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.show();
            return;
        }
        if (!isPasswordValid(password)) {
            ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("invalidPasswordTitle"), labels.getString("invalidPasswordMessage"), FontAwesomeSolid.EXCLAMATION_TRIANGLE);
            errorDialog.show();
            return;
        }
        HttpManager httpManager = new HttpManager();
        httpManager.updatePassword(password);
    }

    public void updateText(ResourceBundle labels) {
        frenchMenuItem.setText(labels.getString("french"));
        englishMenuItem.setText(labels.getString("english"));

        // Set the text of the buttons
        changeEmailButton.setText(labels.getString("changeEmail"));
        changePasswordButton.setText(labels.getString("changePassword"));
        logoutButton.setText(labels.getString("logout"));

        // Set the text of the labels
        currentPasswordLabel.setText(labels.getString("newPassword"));
        confirmPasswordLabel.setText(labels.getString("confirmPassword"));

        localeLabel.setText(labels.getString("locale"));
        // Set the text of the currentMailLabel
        if (user != null) {
            currentNameLabel.setText(labels.getString("currentName") + ": " + user.getName());
            currentMailLabel.setText(labels.getString("currentEmail") + ": " + user.getEmail());
        }

        frenchMenuItem.setOnAction(_ -> setLocale(Locale.of("fr", "FR")));
        englishMenuItem.setOnAction(_ -> setLocale(Locale.of("en", "US")));
    }


    public boolean isPasswordValid(String password) {
        // At least 12 characters, including uppercase, lowercase, numbers, and special characters
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{12,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    private void setLocale(Locale locale) {
        LanguageManager.setLanguage(locale.getLanguage());

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
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.SettingsView", locale);
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {

            currentMailLabel.setText(labels.getString("currentEmail") + ": " + user.getEmail());
        }
    }

    @FXML
    private void logout() {
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
}