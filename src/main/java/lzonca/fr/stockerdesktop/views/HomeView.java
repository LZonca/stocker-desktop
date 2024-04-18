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
            userNameLabel.setText(user.getName());
        }
    }

    public void setUser(User user) {
        this.user = user;
        // Now you can use this.user in this class to access the user details
        if (user != null) {
            userNameLabel.setText(user.getName());
        }
    }
}