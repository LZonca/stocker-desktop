package lzonca.fr.stockerdesktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class MainView {


    public void initialize() {
        sideBar.toFront();
    }

    @FXML
    private VBox sideBar;

    @FXML
    private AnchorPane contentArea;


    @FXML

    private void toHomeButton() throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/HomeView.fxml")));
        contentArea.getChildren().setAll(node);
    }

    @FXML
    private void toStocksButton() throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/StocksView.fxml")));
        contentArea.getChildren().setAll(node);
    }

    @FXML
    private void toGroupsButton() throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/GroupsView.fxml")));
        contentArea.getChildren().setAll(node);
    }

    @FXML
    private void toSettingsButton() throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/lzonca/fr/stockerdesktop/SettingsView.fxml")));
        contentArea.getChildren().setAll(node);
    }
}