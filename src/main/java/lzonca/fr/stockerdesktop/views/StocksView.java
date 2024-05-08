package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.models.Produit;
import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class StocksView {


    @FXML
    public TitledPane userStocksPane;
    @FXML
    public ProgressIndicator refreshIndicator;
    @FXML
    public Button refreshButton;
    @FXML
    public StackPane buttonStackPane;
    @FXML
    public Label buttonLabel;
    @FXML
    public Button createStockBtn;
    @FXML
    private Accordion stocksAccordion;
    private User user;
    private ResourceBundle labels;

    @FXML
    public void initialize() {
        refreshButton.setOnAction(_ -> refreshStocks());
        createStockBtn.setOnAction(_-> openNewStockForm());
        loadResourceBundle();
        updateText(labels);
        refreshStocks();
    }

    private void openNewStockForm() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateStockForm.fxml"));
            Parent root = loader.load();

            // Create a new Scene with the loaded FXML file
            Scene scene = new Scene(root);

            // Create a new Stage to display the form
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(labels.getString("createStock"));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.StocksView", locale);
    }

    private void updateText(ResourceBundle labels) {
        buttonLabel.setText(labels.getString("refresh"));
        /*groupNameLabel.setText(labels.getString("groupName"));
        groupsPane.setText(labels.getString("groups"));
        groupsLabel.setText(labels.getString("yourGroups"));
        createGroupBtn.setText(labels.getString("createGroup"));*/
    }

    public void setUser(User user) {
        this.user = user;
        displayUserStocks();
    }

    private void displayUserStocks() {
        stocksAccordion.getPanes().clear();
        if (user.getStocks() != null) {
            for (Stock stock : user.getStocks()) {
                stocksAccordion.getPanes().add(createStockAccordion(stock));
            }
        }
    }

    private TitledPane createStockAccordion(Stock stock) {
        TitledPane stockPane = new TitledPane();
        stockPane.setText(stock.getNom());

        // Create a table view
        TableView<Produit> table = new TableView<>();

        // Define columns
        TableColumn<Produit, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue();
            if (produit != null && produit.getPivot() != null) {
                return new ReadOnlyObjectWrapper<>(produit.getPivot().getQuantite());
            } else {
                return new ReadOnlyObjectWrapper<>(0);
            }
        });
        table.getColumns().add(quantityColumn);
        table.getColumns().add(nameColumn);
        // Add products to the table
        for (Produit product : stock.getProduits()) {
            table.getItems().add(product);
        }

        // Set the content of the TitledPane to the table
        stockPane.setContent(table);

        return stockPane;
    }

    private void refreshStocks() {
        // Show the spinner and remove the text
        Platform.runLater(() -> {
            refreshIndicator.setVisible(true);
            buttonLabel.setText("");
        });
        HttpManager httpManager = new HttpManager();
        // Fetch the data in a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                HttpResponse<String> response = httpManager.getUserStocks();
                if (response.statusCode() == 200) {
                    // Parse the response body to get the groups
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    List<Stock> stocks = Arrays.asList(mapper.readValue(response.body(), Stock[].class));

                    // Update the user's groups
                    user.setStocks(stocks);

                    // Refresh the display
                    Platform.runLater(this::displayUserStocks);
                } else {
                    ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("refreshFailedTitle"), labels.getString("refreshFailedMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE);
                }
            } catch (IOException | InterruptedException | URISyntaxException e) {
                Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("refreshFailedTitle"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait());
            } finally {
                // Hide the spinner and show the text
                Platform.runLater(() -> {
                    refreshIndicator.setVisible(false);
                    buttonLabel.setText(labels.getString("refresh"));
                });
            }
        }).start();
    }
}