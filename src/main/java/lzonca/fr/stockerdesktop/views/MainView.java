package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import lzonca.fr.stockerdesktop.App;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.responses.UserResponse;
import lzonca.fr.stockerdesktop.system.LanguageManager;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainView {

    private UserResponse userResponse;
    private User user; // Add this field

    private ResourceBundle labels;

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
    private SettingsView settingsViewController; // Add this field

    @FXML
    private BorderPane mainBorderPane;


    @FXML
    public void initialize() {
        subScene.widthProperty().bind(mainBorderPane.widthProperty());
        subScene.heightProperty().bind(mainBorderPane.heightProperty());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/lzonca/fr/stockerdesktop/views/HomeView.fxml"));
            Parent homeView = fxmlLoader.load();
            homeViewController = fxmlLoader.getController();
            homeViewController.setUser(this.user);
            subScene.setRoot(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadResourceBundle();
        updateText(labels);
        System.out.println("MainView initialize method called");
        System.out.println("User: " + this.user);
    }



    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.MainView", locale);
    }

    private void updateText(ResourceBundle labels) {
        goToHome.setText(labels.getString("home"));
        goToStocks.setText(labels.getString("stocks"));
        goToGroups.setText(labels.getString("groups"));
        goToSettings.setText(labels.getString("settings"));
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
            System.out.println("HomeView loaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToStocks() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/lzonca/fr/stockerdesktop/views/StocksView.fxml"));
            Parent stockView = loader.load();
            StocksView stocksViewController = loader.getController();
            stocksViewController.setUser(this.user); // Pass the user details to the HomeView
            subScene.setRoot(stockView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToGroups() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/lzonca/fr/stockerdesktop/views/GroupsView.fxml"));
            Parent groupsView = loader.load();
            GroupsView groupsViewController = loader.getController(); // Corrected line
            groupsViewController.setUser(this.user);
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
            SettingsView settingsViewController = loader.getController(); // Store the SettingsView controller

            settingsViewController.setUser(this.user); // Pass the user details to the SettingsView
            settingsViewController.initialize(); // Call initialize method after setting the user
            subScene.setRoot(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}