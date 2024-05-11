package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lzonca.fr.stockerdesktop.models.Produit;
import lzonca.fr.stockerdesktop.system.LanguageManager;

import java.util.Locale;
import java.util.ResourceBundle;

public class ShowProduit {

    @FXML
    public Label productNameLabel;

    @FXML
    public Label productCodeLabel;

    @FXML
    public Label productDescLabel;

    @FXML
    public ImageView productImage;

    @FXML
    public Label productQuantityLabel;

    @FXML
    public TextArea productDesc;

    @FXML
    public Label productQuantity;

    @FXML
    public Label productCode;

    private ResourceBundle labels;

    private Produit produit;

    @FXML
    public void initialize() {
        loadResourceBundle();
        updateTexts();
        if (produit != null){
            loadProduit();
        }
    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.ShowProduit", locale);
    }

    private void updateTexts() {
        productCodeLabel.setText(labels.getString("productCode"));
        productDescLabel.setText(labels.getString("productDesc"));
        productQuantityLabel.setText(labels.getString("productQuantity"));
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        loadProduit();
    }

    private void loadProduit() {
        if (produit != null) {
            productNameLabel.setText(produit.getNom());
            if (produit.getImage() != null) {
                try {
                    productImage.setImage(new Image(produit.getImage()));
                } catch (IllegalArgumentException e) {
                    System.out.println("Image not found: " + produit.getImage());
                }
            }

            if (produit.getCode() == null) {
                productCode.setText(labels.getString("noCode"));
            }else {
                productCode.setText(produit.getCode());
            }

            if (produit.getDescription() == null) {
                productDesc.setText(labels.getString("noDesc"));
            }else {
                productDesc.setText(produit.getDescription());
            }
            productQuantity.setText(String.valueOf(produit.getPivot().getQuantite()));
        }
    }
}