package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.TokenManager;

import java.io.IOException;

public class SettingsView {

    @FXML
    private javafx.scene.control.Label currentMailField;

    @FXML
    private javafx.scene.control.TextField emailField;

    @FXML
    private javafx.scene.control.TextField passwordField;

    @FXML
    private javafx.scene.control.TextField passwordConfirmationField;

    @FXML
    private javafx.scene.control.Button logoutButton;

    private User user;

    @FXML
    public void initialize() {
        if (user != null) {
            currentMailField.setText("Votre email actuel: " + user.getEmail());
        }
    }

    public void setUser(User user) {
        this.user = user;
        // Now you can use this.user in this class to access the user details
        if (user != null) {
            currentMailField.setText("Votre email actuel: " + user.getEmail());
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