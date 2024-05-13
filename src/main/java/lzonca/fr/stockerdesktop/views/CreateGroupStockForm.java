package lzonca.fr.stockerdesktop.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class CreateGroupStockForm {

    @FXML
    public Label TitleLabel;
    @FXML
    public Label stockNameLabel;
    @FXML
    public TextField nameField;
    @FXML
    public Button createStockButton;

    private ResourceBundle labels;

    private StocksView stocksView;

    private GroupsView groupsView;

    private Groupe groupe;



    @FXML
    private void initialize() {
        createStockButton.setOnAction(_ -> {
            HttpManager httpManager = new HttpManager();
            try {
                    // Call createGroupProduit method if the form is opened from GroupsView
                    httpManager.createGroupStock(nameField.getText(), groupe.getId());
                    Platform.runLater(() -> {
                        groupsView.refreshStocks();
                        groupsView.refreshGroups(); // Add this line to refresh the groups
                    });

                // Refresh the stocks
                new ErrorDialog(labels.getString("success"), labels.getString("successTitleStockCreated"), labels.getString("successDescStockCreated"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();

                // Close the current window
                Stage stage = (Stage) createStockButton.getScene().getWindow();
                stage.close();
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateStock"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait());
            }
        });
        loadResourceBundle();
        updateText(labels);
    }

    public void setGroupsView(GroupsView groupsView, Groupe groupe) {
        this.groupsView = groupsView;
        this.groupe = groupe;
    }

    public void setStocksView(StocksView stocksView) {
        this.stocksView = stocksView;
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.forms.CreateStockForm", locale);
    }

    private void updateText(ResourceBundle labels) {
        TitleLabel.setText(labels.getString("createStock"));
        createStockButton.setText(labels.getString("create"));
        stockNameLabel.setText(labels.getString("stockName"));
    }

    private void createStock() {
        if (nameField.getText().isEmpty()) {
            new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateStock"), labels.getString("errorDescTitleCannotBeEmpty"), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
            return;
        }
        try {
            HttpManager httpManager = new HttpManager();
            httpManager.createUserStock(nameField.getText());
            Platform.runLater(() -> {
                new ErrorDialog(labels.getString("success"), labels.getString("successTitleStockCreated"), labels.getString("successDescStockCreated"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();
                // Close the current window
                Stage stage = (Stage) createStockButton.getScene().getWindow();
                stage.close();
            });

        } catch (IOException | InterruptedException e) {
            Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateStock"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait());
        }
    }
}
