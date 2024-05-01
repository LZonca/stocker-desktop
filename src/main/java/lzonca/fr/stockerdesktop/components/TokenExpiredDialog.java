package lzonca.fr.stockerdesktop.components;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;
public class TokenExpiredDialog extends Alert {
        public TokenExpiredDialog(String title, String headerText, String contentText) {
            super(AlertType.ERROR);
            setTitle(title);
            setHeaderText(headerText);
            setContentText(contentText);
            FontIcon fontIcon = new FontIcon(FontAwesomeSolid.CODE);
            fontIcon.setIconSize(32);

            setGraphic(fontIcon);
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/lzonca/fr/stockerdesktop/assets/stocker.png")));
            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.getIcons().add(iconImage);
            setOnCloseRequest(event -> {
                // Prevent the dialog from closing
                event.consume();
                // Create a new instance of the app and start it
                Platform.runLater(() -> {
                    try {
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }
}
