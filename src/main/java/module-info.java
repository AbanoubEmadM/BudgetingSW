/**
 * Defines the {@code com.example.a2} Java Platform Module for the personal budgeting JavaFX application.
 * Exposes the main package, opens UI packages to FXML, and declares JavaFX and third-party dependencies.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.HelloApplication
 */
module com.example.a2 {
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
    requires java.sql;

    opens com.example.a2 to javafx.fxml;
    opens com.example.a2.ui to javafx.fxml;
    exports com.example.a2;
}
