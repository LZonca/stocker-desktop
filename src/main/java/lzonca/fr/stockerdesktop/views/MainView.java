package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.responses.UserResponse;
import lzonca.fr.stockerdesktop.system.CurrentUser;
import lzonca.fr.stockerdesktop.system.TokenManager;

import java.io.IOException;
import java.util.Objects;

public class MainView {

    private UserResponse userResponse;
    private User user; // Add this field

    @FXML
    private Button goToHome;

    @FXML
    private Button goToStocks;

    @FXML
    private Button goToGroups;

    @FXML
    private Button goToSettings;

    @FXML
    private SubScene subScene;

    private HomeView homeViewController;

    @FXML
    public void initialize() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/lzonca/fr/stockerdesktop/views/HomeView.fxml"));
            Parent homeView = fxmlLoader.load();
            homeViewController = fxmlLoader.getController();
            homeViewController.setUser(this.user);
            subScene.setRoot(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (homeViewController != null) {
            homeViewController.setUser(user);
        }
    }

    @FXML
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/views/HomeView.fxml")));
            Parent homeView = loader.load();
            HomeView homeViewController = loader.getController();
            homeViewController.setUser(this.user); // Pass the user details to the HomeView
            subScene.setRoot(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToStocks() {
        try {
            Parent stockView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/views/StocksView.fxml")));
            subScene.setRoot(stockView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToGroups() {
        try {
            Parent groupsView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/views/GroupsView.fxml")));
            subScene.setRoot(groupsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/lzonca/fr/stockerdesktop/views/SettingsView.fxml"));
            Parent settingsView = loader.load();
            SettingsView settingsViewController = loader.getController();
            settingsViewController.setUser(this.user); // Pass the user details to the HomeView
            subScene.setRoot(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}