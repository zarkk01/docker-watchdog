package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/containersScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 540);
        stage.setScene(scene);
        stage.setTitle("Docker WatchDog");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}