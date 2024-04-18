module lzonca.fr.stockerdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens lzonca.fr.stockerdesktop to javafx.fxml;
    exports lzonca.fr.stockerdesktop;
    /*exports lzonca.fr.stockerdesktop.components;
    opens lzonca.fr.stockerdesktop.components to javafx.fxml;*/
}