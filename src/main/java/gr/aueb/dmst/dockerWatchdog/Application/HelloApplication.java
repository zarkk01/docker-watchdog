package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.http.HttpClient;

public class HelloApplication extends Application {
    public static HttpClient client;
    public HelloApplication() {
        client = HttpClient.newHttpClient();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/containersScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 540);
        stage.setScene(scene);
        stage.setTitle("Docker Watchdog");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}