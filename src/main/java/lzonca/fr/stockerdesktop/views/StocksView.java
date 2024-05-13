package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Produit;
import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.*;

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
    private final Map<Stock, TitledPane> stockPanes = new HashMap<>();

    @FXML
    public void initialize() {
        refreshButton.setOnAction(_ -> refreshStocks());
        createStockBtn.setOnAction(_ -> openNewStockForm());
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

            CreateStockForm createStockForm = new CreateStockForm();
            createStockForm.setStocksView(this); // assuming 'this' is an instance of StocksView

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
        groupsLabel.setText(labels.getString("yourGroups"));*/
        createStockBtn.setText(labels.getString("createStock"));
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
        stockPane.getStyleClass().add("titled-pane");
        stockPanes.put(stock, stockPane);
        // Create a table view
        TableView<Produit> table = new TableView<>();
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);


        Button refreshStockBtn = new Button(labels.getString("refreshStock"));
        refreshStockBtn.setGraphic(new FontIcon("fas-sync"));
        refreshStockBtn.setOnAction(_ -> refreshStock(stock));
        refreshStockBtn.getStyleClass().add("default-button");

        // Define columns
        TableColumn<Produit, String> codeColumn = new TableColumn<>(labels.getString("productCode"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeColumn.setMinWidth(150); // Set minimum width

        TableColumn<Produit, String> nameColumn = new TableColumn<>(labels.getString("productName"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nameColumn.setMinWidth(150); // Set minimum width


        // Create a delete button
        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("default-button");
        deleteButton.setGraphic(new FontIcon("fas-trash"));
        deleteButton.setOnAction(_ -> {
            // Create a confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(labels.getString("confirmDeleteTitle"));
            alert.setHeaderText(labels.getString("confirmDeleteStock"));
            alert.setContentText(labels.getString("confirmDeleteContent"));

            // Show the dialog and wait for the user's response
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                HttpManager httpManager = new HttpManager();
                try {
                    httpManager.deleteStock(stock.getId());
                    // Remove the TitledPane associated with the Stock
                    stocksAccordion.getPanes().remove(stockPanes.get(stock));
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Create a label for the stock name


        TableColumn<Produit, Produit> quantityChangeColumn = new TableColumn<>(labels.getString("productQuantity"));
        quantityChangeColumn.setMinWidth(200); // Set minimum width
        quantityChangeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        quantityChangeColumn.setCellFactory(_ -> new TableCell<>() {
            private final TextField quantityInput = new TextField();
            private final Button incrementButton = new Button();
            private final Button decrementButton = new Button();
            private final Button validateButton = new Button();

            @Override
            protected void updateItem(Produit produit, boolean empty) {
                super.updateItem(produit, empty);
                FontIcon trashIcon = new FontIcon("fas-check");
                validateButton.setGraphic(trashIcon);
                validateButton.getStyleClass().add("default-button");

                FontIcon incrementIcon = new FontIcon("fas-plus");
                incrementButton.setGraphic(incrementIcon);
                incrementButton.getStyleClass().add("default-button");

                FontIcon decrementIcon = new FontIcon("fas-minus");
                decrementButton.setGraphic(decrementIcon);
                decrementButton.getStyleClass().add("default-button");
                if (produit == null) {
                    setGraphic(null);
                } else {
                    quantityInput.setPromptText(String.valueOf(produit.getPivot().getQuantite()));

                    incrementButton.setOnAction(_ -> {
                        if (!quantityInput.getPromptText().isEmpty()) {
                            int quantity = Integer.parseInt(quantityInput.getPromptText());
                            quantity++;
                            quantityInput.setPromptText(String.valueOf(quantity));
                        }
                    });

                    decrementButton.setOnAction(_ -> {
                        if (!quantityInput.getPromptText().isEmpty()) {
                            int quantity = Integer.parseInt(quantityInput.getPromptText());
                            if (quantity > 0) {
                                quantity--;
                                quantityInput.setPromptText(String.valueOf(quantity));
                            }
                        }
                    });

                    validateButton.setOnAction(_ -> {
                        // Get the entered quantity
                        int quantity;
                        try {
                            String quantityText = quantityInput.getText().isEmpty() ? quantityInput.getPromptText() : quantityInput.getText();
                            quantity = Integer.parseInt(quantityText);
                            HttpManager httpManager = new HttpManager();
                            httpManager.updateProductQuantity(stock.getId(), produit.getId(), quantity);
                            new ErrorDialog(labels.getString("success"), labels.getString("successTitleQuantiteUpdated"), labels.getString("successDescQuantiteUpdated"), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait();
                        } catch (NumberFormatException e) {
                            // Show an error message if the entered quantity is not a valid number
                            new ErrorDialog(labels.getString("error"), labels.getString("invalidQuantityTitle"), labels.getString("invalidQuantityMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait();
                        } catch (IOException | URISyntaxException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    HBox buttons = new HBox(5, decrementButton, quantityInput, incrementButton, validateButton);
                    buttons.setAlignment(Pos.CENTER); // Center the buttons
                    setGraphic(buttons);
                }
            }
        });

        // Define a column for the remove button

        TableColumn<Produit, Produit> removeColumn = new TableColumn<>(labels.getString("actions"));
        removeColumn.setMinWidth(20); // Set minimum width
        removeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        removeColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button removeButton = new Button();
            private final Button showButton = new Button();
            private final Button editButton = new Button();


            @Override
            protected void updateItem(Produit produit, boolean empty) {
                super.updateItem(produit, empty);

                if (produit == null) {
                    setGraphic(null);
                    return;
                }

                FontIcon showIcon = new FontIcon("fas-eye");
                showButton.setGraphic(showIcon);
                showButton.getStyleClass().add("default-button");

                showButton.setOnAction(_ -> {
                    try {
                        // Load the FXML file for the Produit creation form
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/ShowProduit.fxml"));
                        Parent root = loader.load();

                        // Pass the stock to the controller
                        ShowProduit controller = loader.getController();
                        controller.setProduit(produit);

                        // Create a new Scene with the loaded FXML file
                        Scene scene = new Scene(root);

                        // Create a new Stage to display the form
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle(produit.getNom());
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                FontIcon editIcon = new FontIcon("fas-edit");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("default-button");

                editButton.setOnAction(_ -> {
                    try {
                        // Load the FXML file for the Produit creation form
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/EditProduit.fxml"));
                        Parent root = loader.load();

                        // Pass the stock to the controller
                        EditProduit controller = loader.getController();
                        controller.setProduit(produit);
                        controller.setStock(stock);
                        controller.setStocksView(StocksView.this); // pass the reference of StocksView

                        // Create a new Scene with the loaded FXML file
                        Scene scene = new Scene(root);

                        // Create a new Stage to display the form
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle(produit.getNom() + " " + labels.getString("edit"));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Set the FontAwesome icon
                FontIcon trashIcon = new FontIcon("fas-trash");
                removeButton.setGraphic(trashIcon);
                removeButton.getStyleClass().add("default-button");

                removeButton.setOnAction(_ -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(labels.getString("confirmDeleteTitle"));
                    alert.setHeaderText(labels.getString("confirmDeleteHeader"));
                    alert.setContentText(labels.getString("confirmDeleteContent"));

                    // Show the alert and wait for the user's response
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Call your API to remove the product
                            HttpManager httpManager = new HttpManager();
                            try {
                                httpManager.removeProduct(stock.getId(), produit.getId());
                            } catch (IOException | InterruptedException | URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                            // Refresh the table to show the updated list of products
                            table.getItems().remove(produit);
                        }
                    });
                });


                HBox buttons = new HBox(showButton, editButton, removeButton);
                buttons.setSpacing(5);
                buttons.setAlignment(Pos.CENTER); // Center the button
                setGraphic(buttons);
            }
        });
        codeColumn.prefWidthProperty().bind(table.widthProperty().divide(4));
        nameColumn.prefWidthProperty().bind(table.widthProperty().divide(4));
        quantityChangeColumn.prefWidthProperty().bind(table.widthProperty().divide(4));
        removeColumn.prefWidthProperty().bind(table.widthProperty().divide(4));
        // Add the columns to the table
        table.getColumns().add(codeColumn);
        table.getColumns().add(nameColumn);
        /*table.getColumns().add(quantityColumn);*/
        table.getColumns().add(quantityChangeColumn);
        table.getColumns().add(removeColumn);


        // Add products to the table
        for (Produit product : stock.getProduits()) {
            table.getItems().add(product);
        }

        // Create a "Create Product" button
        Button createProduitBtn = new Button(labels.getString("createProduit"));
        createProduitBtn.setOnAction(_ -> openNewProduitForm(stock));
        createProduitBtn.getStyleClass().add("default-button");
        createProduitBtn.setGraphic(new FontIcon("fas-plus"));

        // Add the table and the "Create Product" button to a VBox
        VBox vbox = new VBox(createProduitBtn,refreshStockBtn, table);
        vbox.setFillWidth(true);

        // Set the content of the TitledPane to the ScrollPane
        // Set the content of the TitledPane to the VBox
        stockPane.setContent(vbox);

        // Style the table
        table.setStyle("-fx-padding: 10;" // Add padding
                + "-fx-font-size: 14;" // Increase font size
                + "-fx-text-fill: #333333;"); // Change font color
        stockPane.setGraphic(deleteButton);
        return stockPane;
    }

    public void refreshStock(Stock stock) {

        Platform.runLater(() -> {
            refreshIndicator.setVisible(true);
            buttonLabel.setText("");
        });

        HttpManager httpManager = new HttpManager();
        // Fetch the data in a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                HttpResponse<String> response = httpManager.getStocksProduits(stock.getId());
                if (response.statusCode() == 200) {
                    // Parse the response body to get the groups
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    List<Produit> produits = Arrays.asList(mapper.readValue(response.body(), Produit[].class));

                    stock.setProduits(produits);

                    // Refresh the display
                    Platform.runLater(() -> displaySpecificStock(stock));
                } else {
                    ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("refreshFailedTitle"), labels.getString("refreshFailedMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE);
                }
            } catch (IOException | InterruptedException e) {
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

    private void displaySpecificStock(Stock specificStock) {
        // Find the TitledPane associated with the specific stock
        TitledPane stockPane = stockPanes.get(specificStock);

        if (stockPane != null) {
            // Get the VBox that contains the TableView
            VBox vbox = (VBox) stockPane.getContent();

            // Get the TableView from the VBox
            TableView<Produit> table = (TableView<Produit>) vbox.getChildren().get(2);

            // Clear the existing items in the table
            table.getItems().clear();

            // Add products of the specific stock to the table
            for (Produit product : specificStock.getProduits()) {
                table.getItems().add(product);
            }

            // Refresh the table view to update the UI
            table.refresh();
        }
    }

    public void refreshStocks() {
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

    private void openNewProduitForm(Stock stock) {
        try {
            // Load the FXML file for the Produit creation form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateProduitForm.fxml"));
            Parent root = loader.load();

            // Pass the stock to the controller
            CreateProduitForm controller = loader.getController();
            controller.setStock(stock);
            controller.setStocksView(this); // pass the reference of StocksView

            // Create a new Scene with the loaded FXML file
            Scene scene = new Scene(root);

            // Create a new Stage to display the form
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(labels.getString("createProduit"));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}