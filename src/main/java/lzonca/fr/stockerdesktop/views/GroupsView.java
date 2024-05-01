package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
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
    private VBox groupsContainer; // This is the container for the group labels in your FXML file

    @FXML
    private ProgressIndicator refreshIndicator;

    @FXML
    private Label groupNameLabel;

    @FXML
    private Accordion groupsAccordion; // This is the Accordion for the groups in your FXML file

    @FXML
    private TableView<?> membersTable; // This is the TableView for the members in your FXML file

    private User user;

    @FXML
    private Button refreshButton;

    @FXML
    private StackPane buttonStackPane;

    @FXML
    private Label buttonLabel;
    private ResourceBundle labels;

    @FXML
    public void initialize() {
        refreshButton.setOnAction(_ -> refreshGroups());
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
        groupPane.setText(groupe.getNom());

        // Create a VBox to hold the group name and the accordions
        VBox vbox = new VBox();

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

        // Create the email column
        TableColumn<User, String> emailColumn = new TableColumn<>();
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // use the exact field name
        emailColumn.setText(labels.getString("email")); // set the displayed column name based on the locale

        // Add the columns to the TableView
        membersTable.getColumns().add(nameColumn);
        membersTable.getColumns().add(emailColumn);

        TextField emailField = new TextField();
        emailField.setPromptText(labels.getString("enterEmail"));

        Button addUserButton = new Button(labels.getString("addUser"));
        addUserButton.getStyleClass().add("default-button");
        addUserButton.setOnAction(_ -> addUserToGroup(groupe.getId(), emailField.getText())); // Pass the group ID to the event handler
        vbox.getChildren().add(addUserButton);

        // Create a TextField for the user to enter an email

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

    private void addUserToGroup(int groupId, String email) {
        try {
            HttpResponse<String> response = httpManager.addUserToGroup(groupId, email);
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                displayGroups();
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