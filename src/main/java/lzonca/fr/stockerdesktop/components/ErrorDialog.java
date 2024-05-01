package lzonca.fr.stockerdesktop.components;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class ErrorDialog extends Alert {

    public ErrorDialog(String title, String headerText, String contentText, FontAwesomeSolid icon) {
        super(AlertType.ERROR);
        setTitle(title);
        setHeaderText(headerText);
        setContentText(contentText);
        FontIcon fontIcon = new FontIcon(icon.getDescription());
        fontIcon.setIconSize(32);
        setGraphic(fontIcon);
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/lzonca/fr/stockerdesktop/assets/stocker.png")));

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(iconImage);
    }
}