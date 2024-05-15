package lzonca.fr.stockerdesktop.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class CreateGroupForm {

    private ResourceBundle labels;
    @FXML
    public Label TitleLabel;
    @FXML
    public TextField nameField;
    @FXML
    public Button createGroupButton;
    @FXML
    public Label groupNameLabel;

    private GroupsView groupsView;


    @FXML
    private void initialize() {
        createGroupButton.setOnAction(_ -> {
            HttpManager httpManager = new HttpManager();


            try {
                httpManager.createGroup(nameField.getText());
                // Refresh the groups
                Platform.runLater(() -> groupsView.refreshGroups());

                new ErrorDialog(labels.getString("success"), labels.getString("successTitleGroupCreated"), labels.getString("successDescGroupCreated"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();

                // Close the current window
                Stage stage = (Stage) createGroupButton.getScene().getWindow();
                stage.close();
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateGroup"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait());
            }
        });
        loadResourceBundle();
        updateText(labels);
    }

    public void setGroupsView(GroupsView groupsView) {
        this.groupsView = groupsView;
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.forms.CreateGroupForm", locale);
    }

    private void updateText(ResourceBundle labels) {
        TitleLabel.setText(labels.getString("createGroup"));
        createGroupButton.setText(labels.getString("create"));
        groupNameLabel.setText(labels.getString("groupName"));
    }

    private void createGroup() {
        if (nameField.getText().isEmpty()) {
            new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateGroup"), labels.getString("errorDescTitleCannotBeEmpty"), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait();
            return;
        }
        try {
            HttpManager httpManager = new HttpManager();
            httpManager.createGroup(nameField.getText());
            Platform.runLater(() -> {
                new ErrorDialog(labels.getString("success"), labels.getString("successTitleGroupCreated"), labels.getString("successDescGroupCreated"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();
                // Close the current window
                Stage stage = (Stage) createGroupButton.getScene().getWindow();
                stage.close();
            });

        } catch (IOException | InterruptedException e) {
            Platform.runLater(() -> new ErrorDialog(labels.getString("error"), labels.getString("errorTitleFailedToCreateGroup"), e.getMessage(), FontAwesomeSolid.EXCLAMATION_TRIANGLE).showAndWait());
        }
    }

}
