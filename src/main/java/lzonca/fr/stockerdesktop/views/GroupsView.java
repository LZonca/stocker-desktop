package lzonca.fr.stockerdesktop.views;

import javafx.fxml.FXML;
import lzonca.fr.stockerdesktop.models.User;

public class GroupsView {

    private User user;
    @FXML
    public void initialize() {
        // Initialization logic here...
    }
    public void setUser(User user) {
        this.user = user;
    }

    // Add your other methods here...
}