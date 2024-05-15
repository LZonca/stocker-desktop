package lzonca.fr.stockerdesktop.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Groupe;
import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

public class CreateProduitForm {

    @FXML
    public Button validateButton;
    @FXML
    public Label productNameLabel;
    @FXML
    public Label titleLabel;
    @FXML
    public Label productDescLabel;
    @FXML
    public Label productCodeLabel;
    @FXML
    public TextField productNameInput;
    @FXML
    public TextField productCodeInput;
    @FXML
    public TextArea productDesc;
    @FXML
    public TextField uploadTextField;
    @FXML
    public Button uploadButton;
    @FXML
    public ImageView imagePreview;

    private ResourceBundle labels;

    private Stock stock;

    private Groupe groupe;

    private File uploadedImage;

    private StocksView stocksView;

    private GroupsView groupsView;
    private boolean isFromGroupsView;


    @FXML
    private void initialize() {
        uploadButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                // Handle the file. For example, you can print its path
                imagePreview.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));
                uploadTextField.setText(selectedFile.getName());
                setImage(selectedFile);
            } else {
                System.out.println("File selection cancelled.");
            }
        });

        validateButton.setOnAction(_ -> {
            HttpManager httpManager = new HttpManager();
            try {
                if (isFromGroupsView) {
                    // Call createGroupProduit method if the form is opened from GroupsView
                    httpManager.createGroupProduit(stock.getId(), groupe.getId(), productNameInput.getText(), productCodeInput.getText(), productDesc.getText());
                    Platform.runLater(() -> {
                        groupsView.refreshStocks();
                        groupsView.refreshGroups(); // Add this line to refresh the groups
                    });
                } else {
                    // Call createStock method if the form is opened from StocksView
                    httpManager.createProduit(stock.getId(), productNameInput.getText(), productCodeInput.getText(), productDesc.getText());
                    // Refresh the stock
                    Platform.runLater(() -> stocksView.refreshStock(stock));
                }


                new ErrorDialog(labels.getString("success"), labels.getString("successTitleProductCreated"), labels.getString("successDescProductCreated"), FontAwesomeSolid.CHECK_CIRCLE).showAndWait();

                // Close the current window
                Stage stage = (Stage) validateButton.getScene().getWindow();
                stage.close();
            } catch (IOException | InterruptedException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        loadResourceBundle();
        updateText(labels);
    }

    public void setFromGroupsView(boolean isFromGroupsView) {
        this.isFromGroupsView = isFromGroupsView;
    }

    public void setGroupsView(GroupsView groupsView, Groupe groupe) {
        this.groupsView = groupsView;
        this.groupe = groupe;
    }

    public void setStocksView(StocksView stocksView) {
        this.stocksView = stocksView;
    }

    private void updateText(ResourceBundle labels) {
        titleLabel.setText(labels.getString("createProduct"));
        productNameLabel.setText(labels.getString("productName"));
        productDescLabel.setText(labels.getString("productDescription"));
        productCodeLabel.setText(labels.getString("productCode"));
        uploadButton.setText(labels.getString("uploadImage"));
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.forms.CreateProduitForm", locale);
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setImage(File file) {
        this.uploadedImage = file;
        this.imagePreview.setImage(new javafx.scene.image.Image(file.toURI().toString()));
    }
}
