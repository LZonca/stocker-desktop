package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
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
import java.util.concurrent.CountDownLatch;

public class GroupsView {
    private final HttpManager httpManager;

    @FXML
    public TitledPane membersPane;

    @FXML
    public TitledPane groupsPane;

    @FXML
    public Label groupsLabel;

    @FXML
    public Button createGroupBtn;
    @FXML
    public StackPane buttonStackPane;
    @FXML
    public VBox groupsContainer;
    @FXML
    public TitledPane stocksPane;
    @FXML
    private ProgressIndicator refreshIndicator;

    @FXML
    private Label groupNameLabel;

    @FXML
    private Accordion groupsAccordion; // This is the Accordion for the groups in your FXML file

    private User user;

    @FXML
    private Button refreshButton;

    @FXML
    private ScrollPane groupsScrollPane;

    // Declare 'stockPanes' at the class level in GroupsView.java
    private final Map<Stock, TitledPane> stockPanes = new HashMap<>();
    @FXML
    private Label buttonLabel;
    private ResourceBundle labels;

    @FXML
    public void initialize() {
        refreshButton.setOnAction(_ -> refreshGroups());
        createGroupBtn.setOnAction(_ -> openNewGroupForm());
        loadResourceBundle();
        updateText(labels);
        refreshGroups();

    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.GroupsView", locale);
    }

    private void updateText(ResourceBundle labels) {
        buttonLabel.setText(labels.getString("refresh"));
        groupNameLabel.setText(labels.getString("groupName"));
        membersPane.setText(labels.getString("groupMembers"));
        stocksPane.setText(labels.getString("groupStocks"));
        groupsPane.setText(labels.getString("groups"));
        groupsLabel.setText(labels.getString("yourGroups"));
        createGroupBtn.setText(labels.getString("createGroup"));
    }

    private void openNewGroupForm() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateGroupForm.fxml"));
            Parent root = loader.load();

            // Get the controller
            CreateGroupForm controller = loader.getController();

            // Set the groupsView in the controller
            controller.setGroupsView(this); // 'this' refers to the current instance of GroupsView

            // Create a new Scene with the loaded FXML file
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(labels.getString("createGroup"));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GroupsView() {
        this.httpManager = new HttpManager();
    }

    public void setUser(User user) {
        this.user = user;
        displayGroups();
    }

    private void displayGroups() {
        groupsAccordion.getPanes().clear(); // Clear the current display
        List<Groupe> groupes = user.getGroupes();
        if (groupes != null) {
            for (Groupe groupe : groupes) {
                System.out.println("Affichage du groupe: " + groupe.getNom());
                // Create a new TitledPane for each group
                TitledPane groupPane = createGroupAccordion(groupe);
                groupsAccordion.getPanes().add(groupPane);
            }
        }else{
            System.out.println("Aucun groupe à afficher");
        }
    }



    public void refreshGroupStocks(Groupe groupe) {
        // Initialize the CountDownLatch with the number of stocks
        CountDownLatch latch = new CountDownLatch(groupe.getStocks().size());

        // Fetch the data in a new thread to avoid blocking the UI
        for (Stock stock : groupe.getStocks()) {
            new Thread(() -> {
                try {
                    HttpResponse<String> response = httpManager.getStocksProduits(stock.getId());
                    if (response.statusCode() == 200) {
                        // Parse the response body to get the products
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        List<Produit> produits = Arrays.asList(mapper.readValue(response.body(), Produit[].class));

                        stock.setProduits(produits);
                    } else {
                        ErrorDialog errorDialog = new ErrorDialog(labels.getString("error"), labels.getString("refreshFailedTitle"), labels.getString("refreshFailedMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE);
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("refreshFailedTitle"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait());
                } finally {
                    latch.countDown(); // Decrement the count of the latch
                }
            }).start();
        }

        // Wait for all the threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Refresh the display on the JavaFX Application Thread
        Platform.runLater(this::displayGroups);
    }

    public void refreshGroups() {
        // Show the spinner and remove the text
        Platform.runLater(() -> {
            refreshIndicator.setVisible(true);
            buttonLabel.setText("");
        });

        // Fetch the data in a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                HttpResponse<String> response = httpManager.getUserGroups();
                if (response.statusCode() == 200) {
                    // Parse the response body to get the groups
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    List<Groupe> groupes = Arrays.asList(mapper.readValue(response.body(), Groupe[].class));

                    // Update the user's groups
                    user.setGroupes(groupes);

                    // Refresh the display
                    Platform.runLater(this::displayGroups);
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

    private HBox createTitleBox(Groupe groupe) {
        // Create a HBox for the title region
        HBox titleBox = new HBox();
        titleBox.setSpacing(10); // Add some spacing between the label and the button

        // Create a Label for the title text
        Label titleLabel = new Label(groupe.getNom());
        titleBox.getChildren().add(titleLabel);

        // Check if the proprietaire_id of the group matches the id of the currently authenticated user
        if (groupe.getProprietaire().getId() == user.getId()) {
            // If it does, create a delete button and add it to the HBox
            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("default-button");
            FontIcon trashIcon = new FontIcon("fas-trash");
            deleteButton.setGraphic(trashIcon);
            deleteButton.setOnAction(_ -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(labels.getString("pleaseConfirm"));
                alert.setHeaderText(labels.getString("deleteGroup") + groupe.getNom());
                alert.setContentText(labels.getString("deleteGroupConfirmation"));

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    HttpManager httpmanager = new HttpManager();
                    try {
                        httpmanager.deleteGroup(groupe.getId());
                    } catch (IOException | InterruptedException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    titleBox.getChildren().add(deleteButton);
                }
            });
            titleBox.getChildren().add(deleteButton);
        } else {
            // If it does, create a leave button and add it to the HBox
            Button leaveButton = new Button(labels.getString("leaveGroup"));
            leaveButton.getStyleClass().add("default-button");
            FontIcon trashIcon = new FontIcon("fas-sign-out-alt");
            leaveButton.setGraphic(trashIcon);
            leaveButton.setOnAction(_ -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(labels.getString("pleaseConfirm"));
                alert.setHeaderText(labels.getString("leaveGroup") + groupe.getNom());
                alert.setContentText(labels.getString("leaveGroupConfirmation"));

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    HttpManager httpmanager = new HttpManager();
                    try {
                        httpmanager.leaveGroup(groupe.getId());
                    } catch (IOException | InterruptedException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            titleBox.getChildren().add(leaveButton);
        }

        return titleBox;
    }

    private Accordion createMembersAccordion(Groupe groupe) {
        // Create an Accordion for the members of the group
        Accordion membersAccordion = new Accordion();

        // Create a TitledPane with a TableView for the members
        TitledPane membersPane = new TitledPane();
        membersPane.setText(labels.getString("groupMembers"));
        VBox membersBox = new VBox();

        HBox adduserHBox = new HBox();

        Button adduserButton = new Button(labels.getString("addUser"));
        TextField emailField = new TextField();
        emailField.setPromptText(labels.getString("enterEmail"));
        adduserButton.getStyleClass().add("default-button");
        adduserButton.setOnAction(_ -> {
            String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                    + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
            if (emailField.getText().isEmpty() || !emailField.getText().matches(emailRegex)) {
                new ErrorDialog(labels.getString("error"), labels.getString("errorTitleInvalidEmail"), labels.getString("errorDescInvalidEmail"), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait();
            } else {
                try {
                    httpManager.addUserToGroup(groupe.getId(), emailField.getText());
                    new ErrorDialog(labels.getString("success"), labels.getString("addUserSuccessTitle"), labels.getString("addUserSuccessMessage"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        adduserHBox.getChildren().addAll(emailField, adduserButton);


        // Create a TableView to hold the group members
        TableView<User> membersTable = createMemberTable(groupe);
        membersBox.getChildren().addAll(adduserHBox, membersTable);
        // Set the content of the membersPane to the membersTable TableView
        membersPane.setContent(membersBox);

        // Add the membersPane to the membersAccordion
        membersAccordion.getPanes().add(membersPane);

        return membersAccordion;
    }

    private Accordion createStocksAccordion(Groupe groupe) {



        // Create a TitledPane for each stock
        Accordion stocksAccordion = new Accordion();
        TitledPane stocksPane = new TitledPane();
        stocksPane.setText(labels.getString("groupStocks"));
        stocksAccordion.getPanes().add(stocksPane);

        // Create a VBox to hold the "Add Stock" button and the stocks
        ScrollPane stocksScrollPane = new ScrollPane();
        VBox stocksBox = new VBox();

        // Create "Add Stock" button
        Button addStockButton = new Button(labels.getString("addStock"));
        addStockButton.getStyleClass().add("default-button");
        addStockButton.setOnAction(_ -> openNewStockForm(groupe));

        // Add the button to the stocksBox
        stocksBox.getChildren().add(addStockButton);
        Accordion stockAccordion = new Accordion();
        for (Stock stock : groupe.getStocks()) {
            // Create a TitledPane for the stock

            TitledPane stockPane = new TitledPane();
            stockPane.setText(stock.getNom());
            VBox stockPaneContent = new VBox();

            Button newProductButton = new Button(labels.getString("addProduct"));
            newProductButton.getStyleClass().add("default-button");
            newProductButton.setOnAction(_ -> {
                try {
                    // Load the FXML file for the Produit creation form
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateProduitForm.fxml"));
                    Parent root = loader.load();

                    // Pass the stock to the controller
                    CreateProduitForm controller = loader.getController();
                    controller.setStock(stock);
                    controller.setFromGroupsView(true); // set the flag
                    controller.setGroupsView(this, groupe); // pass the reference of GroupsView

                    // Create a new Scene with the loaded FXML file
                    Scene scene = new Scene(root);

                    // Create a new Stage to display the form
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle(labels.getString("createProduct"));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Create a TableView for the stock
            TableView<Produit> table = createProductTable(stock, groupe);

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
                        httpManager.deleteGroupStock(stock.getId(), groupe.getId());
                        // Remove the TitledPane associated with the Stock
                        stockAccordion.getPanes().remove(stockPanes.get(stock));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            stockPaneContent.getChildren().addAll(newProductButton, table);
            stockPane.setContent(stockPaneContent);
            table.setStyle("-fx-padding: 10;" // Add padding
                    + "-fx-font-size: 14;" // Increase font size
                    + "-fx-text-fill: #333333;"); // Change font color
            stockPane.setGraphic(deleteButton);
            stockAccordion.getPanes().add(stockPane);
        }
        stocksBox.getChildren().add(stockAccordion);
        stocksScrollPane.setContent(stocksBox);
        stocksPane.setContent(stocksBox);
        return stocksAccordion;
    }

    private TitledPane createGroupAccordion(Groupe groupe) {
        // Create a TitledPane for each group
        TitledPane groupPane = new TitledPane();

        // Create a VBox to hold the group name and the accordions
        VBox groupVbox = new VBox();

        // Set the HBox as the graphic of the TitledPane and set the text to null
        groupPane.setGraphic(createTitleBox(groupe));
        groupPane.setText(null);

        // Create a Label for the group name and add it to the VBox
        Label groupNameLabel = new Label(groupe.getNom());
        groupNameLabel.setStyle("-fx-font-size: 25px;");
        groupVbox.getChildren().add(groupNameLabel);

        // Get the owner's name directly from the Groupe object
        String ownerName = groupe.getProprietaire().getName();

        // Create a Label for the owner's name and add it to the VBox
        Label ownerNameLabel = new Label(labels.getString("owner") + ":" + ownerName);
        ownerNameLabel.setStyle("-fx-font-size: 20px;");
        groupVbox.getChildren().add(ownerNameLabel);



        // Add the membersAccordion to the groupVbox
        groupVbox.getChildren().add(createMembersAccordion(groupe));



        // Add the Accordion to the group's TitledPane
        groupVbox.getChildren().add(createStocksAccordion(groupe)); // Add the stocksAccordion to the groupVbox

        groupPane.setContent(groupVbox);

        return groupPane;
    }



    private TableView<User> createMemberTable(Groupe groupe){
        TableView<User> membersTable = new TableView<>();
        membersTable.setPrefHeight(400); // Set the height as per your requirement
        // Create the name column
        TableColumn<User, String> nameColumn = new TableColumn<>();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // use the exact field name
        nameColumn.setText(labels.getString("username")); // set the displayed column name based on the locale // use the exact field name
        nameColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.4)); // Set the width to 50% of the table width
        nameColumn.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setPadding(new Insets(10)); // Add 10px of padding
                }
            }
        });
        nameColumn.setStyle("-fx-font-size: 18px;"); // Set the font size to 18px
        nameColumn.setReorderable(false); // Add this line

// Create the email column
        TableColumn<User, String> emailColumn = new TableColumn<>();
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // use the exact field name
        emailColumn.setText(labels.getString("email")); // set the displayed column name based on the locale
        emailColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.4)); // Set the width to 50% of the table width
        emailColumn.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setPadding(new Insets(10)); // Add 10px of padding
                }
            }
        });
        emailColumn.setStyle("-fx-font-size: 18px;"); // Set the font size to 18px
        emailColumn.setReorderable(false); // Add this line
        TableColumn<User, Void> removeButtonColumn = getUserVoidTableColumn(groupe);

        // Add the columns to the TableView
        membersTable.getColumns().add(nameColumn);
        membersTable.getColumns().add(emailColumn);
        if (user.getId() == groupe.getProprietaire().getId()) {
            if (removeButtonColumn != null) {
                membersTable.getColumns().add(removeButtonColumn);
                removeButtonColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.2)); // Set the width to 50% of the table width
                removeButtonColumn.setReorderable(false); // Add this line
            }
        }

        // Add each member of the group to the TableView
        for (User member : groupe.getMembers()) {
            membersTable.getItems().add(member);
        }

        return membersTable;
    }

    private TitledPane createStockPane(Stock stock, Groupe groupe) {
        TitledPane stockPane = new TitledPane();
        stockPane.setText(stock.getNom());
        VBox stockPaneContent = new VBox();

        Button newProductButton = new Button(labels.getString("addProduct"));
        newProductButton.getStyleClass().add("default-button");
        newProductButton.setOnAction(_ -> {
            try {
                // Load the FXML file for the Produit creation form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateProduitForm.fxml"));
                Parent root = loader.load();

                // Pass the stock to the controller
                CreateProduitForm controller = loader.getController();
                controller.setStock(stock);
                controller.setGroupsView(this, groupe); // pass the reference of GroupsView

                // Create a new Scene with the loaded FXML file
                Scene scene = new Scene(root);

                // Create a new Stage to display the form
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle(labels.getString("createProduct"));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Create a TableView for the stock
        TableView<Produit> table = createProductTable(stock, groupe);

        // Set the content of the stockPane to the TableView
        stockPaneContent.getChildren().addAll(newProductButton, table);
        stockPane.setContent(stockPaneContent);

        return stockPane;
    }

    private TableView<Produit> createProductTable(Stock stock, Groupe groupe) {

        System.out.println("Création table des produits pour le stock: " + stock.getNom());

        TableView<Produit> table = new TableView<>();
        table.setPrefHeight(500);
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
                            httpManager.updateGroupProductQuantity(stock.getId(), groupe.getId(), produit.getId(), quantity);
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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/EditGroupProduit.fxml"));
                        Parent root = loader.load();

                        // Pass the stock to the controller
                        EditGroupProduit controller = loader.getController();
                        controller.setProduit(produit);
                        controller.setStock(stock);
                        controller.setGroupsView(GroupsView.this, groupe);

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
                                httpManager.removeProductFromGroup(stock.getId(), groupe.getId(), produit.getId());
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
        table.getColumns().add(quantityChangeColumn);
        table.getColumns().add(removeColumn);

        // Add products to the table
        List<Produit> produits = stock.getProduits();
        /*System.out.println(produits);*/
        System.out.println("ON AFFICGHE SA MERE");
        if (produits != null) {
            for (Produit product : produits) {
                System.out.println(product.getNom() + " ajouté au stock");
                table.getItems().add(product);
            }
        }

        return table;
    }

    private void openNewStockForm(Groupe groupe) {
        try {
            // Load the FXML file for the Group Stock creation form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateGroupStockForm.fxml"));
            Parent root = loader.load();

            // Get the controller
            CreateGroupStockForm controller = loader.getController(); // Use CreateGroupStockForm here

            // Set the groupsView in the controller
            controller.setGroupsView(this, groupe); // 'this' refers to the current instance of GroupsView

            // Create a new Scene with the loaded FXML file
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(labels.getString("createStock"));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayGroupStocks() {
        // Iterate over each TitledPane in the groupsAccordion

        for (TitledPane groupPane : groupsAccordion.getPanes()) {
            // Find the corresponding group
            Groupe groupe = user.getGroupes().stream()
                    .filter(g -> g.getNom().equals(groupPane.getText()))
                    .findFirst()
                    .orElse(null);

            if (groupe != null) {
                // Get the VBox from the groupPane
                VBox vbox = (VBox) groupPane.getContent();

                // Get the Accordion for the stocks
                Accordion stocksAccordion = (Accordion) vbox.getChildren().get(1);

                // Clear the current stocks
                stocksAccordion.getPanes().clear();

                // Add the stocks of the group
                for (Stock stock : groupe.getStocks()) {
                    // Create a TitledPane for the stock
                    TitledPane stockPane = createStockPane(stock, groupe);

                    // Add the stockPane to the stocksAccordion
                    stocksAccordion.getPanes().add(stockPane);
                }
            }
        }

    }

    public void refreshStocks() {
        // Show the spinner and remove the text
        Platform.runLater(() -> {
            refreshIndicator.setVisible(true);
            buttonLabel.setText("");
        });

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
                    // Add this line to refresh the group stocks
                    Platform.runLater(this::displayGroupStocks);
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

    private TableColumn<User, Void> getUserVoidTableColumn(Groupe groupe) {
        if (groupe.getProprietaire().getId() == user.getId()) {
            TableColumn<User, Void> removeButtonColumn = new TableColumn<>("Actions");
            removeButtonColumn.setStyle("-fx-font-size: 15px;"); // Set the font size to 18px
            removeButtonColumn.setSortable(false); // Add this line
            removeButtonColumn.setCellFactory(_ -> new TableCell<>() {
                private final Button removeButton = new Button(labels.getString("removeUser"));
                {
                    removeButton.getStyleClass().add("default-button");
                    FontIcon trashIcon = new FontIcon("fas-user-slash");
                    removeButton.setGraphic(trashIcon);
                    removeButton.setOnAction(_ -> {
                        User user = getTableView().getItems().get(getIndex());

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(labels.getString("pleaseConfirm"));
                        alert.setHeaderText(labels.getString("deleteUser") + user.getName());
                        alert.setContentText(labels.getString("deleteUserConfirmation"));

                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            removeUserFromGroup(groupe.getId(), user);
                            getTableView().getItems().remove(user);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        User user = getTableView().getItems().get(getIndex());
                        if (groupe.getProprietaire().getId() == user.getId()) {
                            setGraphic(null);
                        } else {
                            setGraphic(removeButton);
                            setAlignment(Pos.CENTER); // Add this line to center the content
                        }
                    }
                }
            });
            return removeButtonColumn;
        } else {
            return null;
        }
    }

    private void removeUserFromGroup(int groupId, User user) {
        try {
            System.out.println(user);
            HttpResponse<String> response = httpManager.removeUserFromGroup(groupId, user);
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                ErrorDialog errorDialog = new ErrorDialog(labels.getString("success"), labels.getString("removeUserSuccessTitle"), labels.getString("removeUserSuccessMessage"), FontAwesomeSolid.CHECK_CIRCLE);
                errorDialog.showAndWait();
                refreshGroups();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<String>> responseMap;
                try {
                    responseMap = mapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>() {
                    });
                } catch (IOException e) {
                    // If the response body cannot be parsed, use a default error message
                    responseMap = Map.of("message", List.of("An error occurred while removing the user from the group."));
                }
                List<String> errorMessages = responseMap.getOrDefault("email", List.of("An error occurred while removing the user from the group."));
                String errorMessage = String.join(", ", errorMessages);

                Platform.runLater(() -> {
                    new ErrorDialog("Error", "Failed to remove user from group", errorMessage, FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
                });
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Platform.runLater(() -> {
                new ErrorDialog("Error", "Failed to remove user from group", e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
            });
        }
    }

    private void addUserToGroup(int groupId, String email) {
        try {
            HttpResponse<String> response = httpManager.addUserToGroup(groupId, email);
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                ErrorDialog errorDialog = new ErrorDialog(labels.getString("success"), labels.getString("addUserSuccessTitle"), labels.getString("addUserSuccessMessage"), FontAwesomeSolid.CHECK_CIRCLE);
                errorDialog.showAndWait();
                refreshGroups();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<String>> responseMap;
                try {
                    responseMap = mapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>() {
                    });
                } catch (IOException e) {
                    // If the response body cannot be parsed, use a default error message
                    responseMap = Map.of("email", List.of("An error occurred while adding the user to the group."));
                }
                List<String> errorMessages = responseMap.getOrDefault("email", List.of("An error occurred while adding the user to the group."));
                String errorMessage = String.join(", ", errorMessages);

                Platform.runLater(() -> {
                    new ErrorDialog("Error", "Failed to add user to group", errorMessage, FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
                });
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Platform.runLater(() -> {
                new ErrorDialog("Error", "Failed to add user to group", e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
            });
        }
    }
}