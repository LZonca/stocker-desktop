package lzonca.fr.stockerdesktop.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lzonca.fr.stockerdesktop.components.ErrorDialog;
import lzonca.fr.stockerdesktop.models.Produit;
import lzonca.fr.stockerdesktop.models.Stock;
import lzonca.fr.stockerdesktop.system.HttpManager;
import lzonca.fr.stockerdesktop.system.LanguageManager;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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

    private StocksView stocksView;



    @FXML
    public void initialize() {
        loadResourceBundle();
        updateTexts();
        if (produit != null){
            loadProduit();
        }

        confirmBtn.setOnAction(_ -> {
            if (productName != null && !productName.getText().isEmpty()) {
                HttpManager httpManager = new HttpManager();
                try {
                    httpManager.updateProduit(stock.getId(), produit.getId(), productName.getText(), productCode.getText(), productDesc.getText());
                    Stage stage = (Stage) confirmBtn.getScene().getWindow();
                    stage.close();

                    // Refresh the stock
                    Platform.runLater(() -> stocksView.refreshStock(stock));
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setStocksView(StocksView stocksView) {
        this.stocksView = stocksView;
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
            productName.setText(produit.getNom());

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
        }
    }
}
