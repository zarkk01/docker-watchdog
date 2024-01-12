package gr.aueb.dmst.dockerWatchdog.Application;

import java.io.IOException;
import java.net.http.HttpClient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopApp extends Application {
    public static HttpClient client;
    public DesktopApp() {
        client = HttpClient.newHttpClient();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DesktopApp.class.getResource("/containersScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),1100,700);
        stage.setScene(scene);
        stage.setTitle("Docker Watchdog");
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}