package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lzonca.fr.stockerdesktop.models.User;

public class HomeView {
    @FXML
    private Label userNameLabel;

    private User user;

    public void initialize() {
        if (user != null) {
            userNameLabel.setText(user.getName().toUpperCase());
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            userNameLabel.setText(user.getName().toUpperCase());
        }
    }
}