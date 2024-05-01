package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lzonca.fr.stockerdesktop.models.User;
import lzonca.fr.stockerdesktop.system.LanguageManager;

import java.util.Locale;
import java.util.ResourceBundle;

public class HomeView {
    @FXML
    public Label welcomeLabel;
    @FXML
    private Label userNameLabel;

    private User user;

    private ResourceBundle labels;

    public void initialize() {
        if (user != null) {
            userNameLabel.setText(user.getName().toUpperCase());
        }

        loadResourceBundle();
        updateText(labels);
    }

    private void updateText(ResourceBundle labels) {
        welcomeLabel.setText(labels.getString("welcome"));

    }

    private void loadResourceBundle() {
        String language = LanguageManager.getLanguage();
        Locale locale = language != null ? Locale.of(language) : Locale.getDefault();
        labels = ResourceBundle.getBundle("lzonca.fr.stockerdesktop.lang.HomeView", locale);
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            userNameLabel.setText(user.getName().toUpperCase());
        }
    }
}