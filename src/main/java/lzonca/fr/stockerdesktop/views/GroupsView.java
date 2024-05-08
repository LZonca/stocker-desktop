package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.controlsfx.glyphfont.FontAwesome;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.*;

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
    private ProgressIndicator refreshIndicator;

    @FXML
    private Label groupNameLabel;

    @FXML
    private Accordion groupsAccordion; // This is the Accordion for the groups in your FXML file

    private User user;

    @FXML
    private Button refreshButton;


    @FXML
    private Label buttonLabel;
    private ResourceBundle labels;

    @FXML
    public void initialize() {
        refreshButton.setOnAction(_ -> refreshGroups());
        createGroupBtn.setOnAction(_-> openNewGroupForm());
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
        groupsPane.setText(labels.getString("groups"));
        groupsLabel.setText(labels.getString("yourGroups"));
        createGroupBtn.setText(labels.getString("createGroup"));
    }

    private void openNewGroupForm() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lzonca/fr/stockerdesktop/views/forms/CreateGroupForm.fxml"));
            Parent root = loader.load();

            // Create a new Scene with the loaded FXML file
            Scene scene = new Scene(root);

            // Create a new Stage to display the form
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
                TitledPane groupPane = createGroupAccordion(groupe);
                groupsAccordion.getPanes().add(groupPane);
            }
        }
    }

    private void refreshGroups() {
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

    private TitledPane createGroupAccordion(Groupe groupe) {
        // Create a TitledPane for each group
        TitledPane groupPane = new TitledPane();

        // Create a VBox to hold the group name and the accordions
        VBox vbox = new VBox();

        // Create a HBox for the title region
        HBox titleBox = new HBox();
        titleBox.setSpacing(10); // Add some spacing between the label and the button

        // Create a Label for the title text
        Label titleLabel = new Label(groupe.getNom());
        titleBox.getChildren().add(titleLabel);

        // Check if the proprietaire_id of the group matches the id of the currently authenticated user
        if (groupe.getProprietaire().getId() == user.getId()) {
            // If it does, create a delete button and add it to the HBox
            Button deleteButton = new Button(labels.getString("delete"));
            /*deleteButton.setGraphic(FontAwesomeSolid.TRASH);*/
            deleteButton.getStyleClass().add("default-button");
            deleteButton.setOnAction(_ -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(labels.getString("pleaseConfirm"));
                alert.setHeaderText(labels.getString("deleteGroup") + groupe.getNom());
                alert.setContentText(labels.getString("deleteGroupConfirmation"));

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK){
                    HttpManager httpmanager = new HttpManager();
                    try {
                        httpmanager.deleteGroup(groupe.getId());
                    } catch (IOException | InterruptedException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    groupsAccordion.getPanes().remove(groupPane);
                }
            });
            titleBox.getChildren().add(deleteButton);
        }else{
                // If it does, create a leave button and add it to the HBox
                Button leaveButton = new Button(labels.getString("leaveGroup"));
                leaveButton.getStyleClass().add("default-button");
                leaveButton.setOnAction(_ -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(labels.getString("pleaseConfirm"));
                    alert.setHeaderText(labels.getString("leaveGroup") + groupe.getNom());
                    alert.setContentText(labels.getString("leaveGroupConfirmation"));

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK){
                        HttpManager httpmanager = new HttpManager();
                        try {
                            httpmanager.leaveGroup(groupe.getId());
                        } catch (IOException | InterruptedException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        groupsAccordion.getPanes().remove(groupPane);
                    }
                });
                titleBox.getChildren().add(leaveButton);
        }

        // Set the HBox as the graphic of the TitledPane and set the text to null
        groupPane.setGraphic(titleBox);
        groupPane.setText(null);

        // Create a Label for the group name and add it to the VBox
        Label groupNameLabel = new Label(groupe.getNom());
        groupNameLabel.setStyle("-fx-font-size: 25px;");
        vbox.getChildren().add(groupNameLabel);

        // Get the owner's name directly from the Groupe object
        String ownerName = groupe.getProprietaire().getName();

        // Create a Label for the owner's name and add it to the VBox
        Label ownerNameLabel = new Label(labels.getString("owner") + ":" + ownerName);
        ownerNameLabel.setStyle("-fx-font-size: 20px;");
        vbox.getChildren().add(ownerNameLabel);

        // Create an Accordion for the members of the group
        Accordion membersAccordion = new Accordion();

        // Create a TitledPane with a TableView for the members
        TitledPane membersPane = new TitledPane();
        membersPane.setText(labels.getString("groupMembers"));

        // Create a TableView to hold the group members
        TableView<User> membersTable = new TableView<>();

        // Create the name column
        TableColumn<User, String> nameColumn = new TableColumn<>();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // use the exact field name
        nameColumn.setText(labels.getString("username")); // set the displayed column name based on the locale // use the exact field name
        nameColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.4)); // Set the width to 50% of the table width
        nameColumn.setCellFactory(column -> {
            return new TableCell<User, String>() {
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
            };
        });
        nameColumn.setStyle("-fx-font-size: 18px;"); // Set the font size to 18px
        nameColumn.setReorderable(false); // Add this line

// Create the email column
        TableColumn<User, String> emailColumn = new TableColumn<>();
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // use the exact field name
        emailColumn.setText(labels.getString("email")); // set the displayed column name based on the locale
        emailColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.4)); // Set the width to 50% of the table width
        emailColumn.setCellFactory(column -> {
            return new TableCell<User, String>() {
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
            };
        });
        emailColumn.setStyle("-fx-font-size: 18px;"); // Set the font size to 18px
        emailColumn.setReorderable(false); // Add this line
        TableColumn<User, Void> removeButtonColumn = getUserVoidTableColumn(groupe);

        // Add the columns to the TableView
        membersTable.getColumns().add(nameColumn);
        membersTable.getColumns().add(emailColumn);
        if (user.getId() == groupe.getProprietaire().getId()) {
            if (removeButtonColumn != null){
                membersTable.getColumns().add(removeButtonColumn);
                removeButtonColumn.prefWidthProperty().bind(membersTable.widthProperty().multiply(0.2)); // Set the width to 50% of the table width
                removeButtonColumn.setReorderable(false); // Add this line
            }
        }

        TextField emailField = new TextField();
        emailField.setPromptText(labels.getString("enterEmail"));
        if(groupe.getProprietaire().getId() == user.getId()) {

            Button addUserButton = new Button(labels.getString("addUser"));
            addUserButton.getStyleClass().add("default-button");

            addUserButton.setOnAction(_ -> addUserToGroup(groupe.getId(), emailField.getText())); // Pass the group ID to the event handler
            vbox.getChildren().add(addUserButton);

            // Modify the addUserButton event handler to get the text from the TextField
            addUserButton.setOnAction((ActionEvent _) -> {
                String email = emailField.getText();
                if (!email.isEmpty()) {
                    addUserToGroup(groupe.getId(), email);
                    emailField.clear();
                } else {
                    Platform.runLater(() -> {
                        new ErrorDialog(labels.getString("error"), labels.getString("addUserTitle"), labels.getString("addUserMessage"), FontAwesomeSolid.EXCLAMATION_CIRCLE).showAndWait();
                    });
                }
            });

            // Add the TextField to the VBox
            vbox.getChildren().add(emailField);
        }

        // Add each member of the group to the TableView
        for (User member : groupe.getMembers()) {
            membersTable.getItems().add(member);
        }

        // Set the content of the membersPane to the membersTable TableView
        membersPane.setContent(membersTable);

        // Add the membersPane to the membersAccordion
        membersAccordion.getPanes().add(membersPane);

        // Add the membersAccordion to the VBox
        vbox.getChildren().add(membersAccordion);

        // Set the content of the groupPane to the vbox
        groupPane.setContent(vbox);

        return groupPane;
    }

    private TableColumn<User, Void> getUserVoidTableColumn(Groupe groupe) {
        if (groupe.getProprietaire().getId() == user.getId()) {
            TableColumn<User, Void> removeButtonColumn = new TableColumn<>("Actions");
            removeButtonColumn.setStyle("-fx-font-size: 15px;"); // Set the font size to 18px

            removeButtonColumn.setCellFactory(_ -> new TableCell<>() {
                private final Button removeButton = new Button(labels.getString("removeUser"));

                {
                    removeButton.getStyleClass().add("default-button");
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

    private void removeUserFromGroup(int groupId, User user ) {
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
                    responseMap = mapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>(){});
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
                    responseMap = mapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>(){});
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