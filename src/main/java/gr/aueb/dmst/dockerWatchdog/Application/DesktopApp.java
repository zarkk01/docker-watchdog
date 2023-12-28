package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpClient;

public class DesktopApp extends Application {
    public static HttpClient client;
    public DesktopApp() {
        client = HttpClient.newHttpClient();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DesktopApp.class.getResource("/containersScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Docker Watchdog");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}