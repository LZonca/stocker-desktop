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
    requires java.prefs;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires annotations;

    opens lzonca.fr.stockerdesktop to javafx.fxml;
    exports lzonca.fr.stockerdesktop;
    exports lzonca.fr.stockerdesktop.views;
    opens lzonca.fr.stockerdesktop.views to javafx.fxml;
    exports lzonca.fr.stockerdesktop.system;
    opens lzonca.fr.stockerdesktop.system to javafx.fxml;
    exports lzonca.fr.stockerdesktop.responses to com.fasterxml.jackson.databind;
    opens lzonca.fr.stockerdesktop.responses to com.fasterxml.jackson.databind;
    exports lzonca.fr.stockerdesktop.models to com.fasterxml.jackson.databind;
    opens lzonca.fr.stockerdesktop.models to com.fasterxml.jackson.databind, javafx.base;
    exports lzonca.fr.stockerdesktop.interfaces;
    opens lzonca.fr.stockerdesktop.interfaces to javafx.fxml;
}