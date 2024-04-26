package lzonca.fr.stockerdesktop.views;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.system.HttpManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class GroupsView {

    private HttpManager httpManager;

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

    @FXML
    public void initialize() {
        refreshButton.setOnAction(event -> refreshGroups());
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
        for (Groupe groupe : user.getGroupes()) {
            // Create a new TitledPane for each group
            TitledPane groupPane = createGroupAccordion(groupe);

            // Add the groupPane to the Accordion
            groupsAccordion.getPanes().add(groupPane);
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
                    // The request failed. You can handle the error here.
                }
            } catch (IOException | InterruptedException | URISyntaxException e) {
                // Handle the exception here
            } finally {
                // Hide the spinner and show the text
                Platform.runLater(() -> {
                    refreshIndicator.setVisible(false);
                    buttonLabel.setText("Actualiser");
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
        Label ownerNameLabel = new Label("Propriétaire: " + ownerName);
        ownerNameLabel.setStyle("-fx-font-size: 20px;");
        vbox.getChildren().add(ownerNameLabel);

        // Create an Accordion for the members of the group
        Accordion membersAccordion = new Accordion();

        // Create a TitledPane with a TableView for the members
        TitledPane membersPane = new TitledPane();
        membersPane.setText("Membres du groupe");

        // Create a TableView to hold the group members
        TableView<User> membersTable = new TableView<>();

        // Create the name column
        TableColumn<User, String> nameColumn = new TableColumn<>("Nom de l'utilisateur");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Create the email column
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Add the columns to the TableView
        membersTable.getColumns().add(nameColumn);
        membersTable.getColumns().add(emailColumn);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter user email");

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(event -> addUserToGroup(groupe.getId(), emailField.getText())); // Pass the group ID to the event handler
        vbox.getChildren().add(addUserButton);

        // Create a TextField for the user to enter an email


        // Modify the addUserButton event handler to get the text from the TextField
        addUserButton.setOnAction(event -> {
            String email = emailField.getText();
            if (!email.isEmpty()) {
                addUserToGroup(groupe.getId(), email);
                emailField.clear();
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
            if (response.statusCode() == 200) {
                displayGroups();
            } else {
                // The request failed. You can handle the error here.
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            // Handle the exception here
        }
    }
}