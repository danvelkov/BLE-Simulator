module app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens gui to javafx.fxml;
    opens topology.modification to javafx.fxml;
    opens packet.controller to javafx.fxml;
    opens packet.model to javafx.base;
    exports app;
    exports gui;
    exports topology.modification;
}