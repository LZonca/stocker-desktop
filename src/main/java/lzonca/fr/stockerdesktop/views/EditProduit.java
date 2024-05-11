package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lzonca.fr.stockerdesktop.models.Produit;
import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

public class EditProduit {
    @FXML
    public Label productNameLabel;

    @FXML
    public TextField productName;

    @FXML
    public Label productCodeLabel;

    @FXML
    public Label productDescLabel;


    @FXML
    public TextArea productDesc;

    @FXML
    public TextField productCode;

    @FXML
    public Button confirmBtn;

    private ResourceBundle labels;

    private Stock stock;

    private Produit produit;

    @FXML
    public void initialize() {
        loadResourceBundle();
        updateTexts();
        if (produit != null){
            loadProduit();
        }

        confirmBtn.setOnAction(_ -> {
            HttpManager httpManager = new HttpManager();
            try {
                httpManager.updateProduit(produit.getId(), stock.getId(), productName.getText(), productCode.getText(), productDesc.getText());
                System.out.println("Produit updated");
            } catch (IOException | InterruptedException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.ShowProduit", locale);
    }

    private void updateTexts() {
        productNameLabel.setText(labels.getString("productName"));
        productCodeLabel.setText(labels.getString("productCode"));
        productDescLabel.setText(labels.getString("productDesc"));
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        loadProduit();
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    private void loadProduit() {
        if (produit != null) {
            productName.setPromptText(produit.getNom());

            if (produit.getCode() == null) {

                productCode.setPromptText(labels.getString("noCode"));

            }else {

                productCode.setPromptText(produit.getCode());

            }

            if (produit.getDescription() == null) {

                productDesc.setPromptText(labels.getString("noDesc"));

            }else {

                productDesc.setPromptText(produit.getDescription());

            }
        }
    }
}
