package app;

import core.Singleton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/AppScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Bluetooth Low Energy Simulator");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toString()));
        stage.setScene(scene);
        stage.show();
        stage.setOnHiding(event -> {
            Thread.currentThread().interrupt();
            Singleton.getInstance().executor.shutdownNow();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
