package lzonca.fr.stockerdesktop;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class GroupsView {
    @FXML
    private ImageView imageView; // Assuming you have an ImageView in your FXML file with fx:id="imageView"

    public void initialize() {
        InputStream inputStream = getClass().getResourceAsStream("/assets/myImage.png");
        Image image = new Image(inputStream);
        imageView.setImage(image);
    }
}