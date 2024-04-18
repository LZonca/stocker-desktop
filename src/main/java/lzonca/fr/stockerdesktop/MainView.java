package lzonca.fr.stockerdesktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class MainView {


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

    @FXML
    public void initialize() {
        goToHome();
    }

    @FXML
    private void goToHome() {
        try {
            Parent stockView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/views/HomeView.fxml")));
            subScene.setRoot(stockView);
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
            Parent settingsView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/views/SettingsView.fxml")));
            subScene.setRoot(settingsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}