package gr.aueb.dmst.dockerWatchdog.Controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class HelloController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button containersButton;

    @FXML
    private Button graphicsButton;

    @FXML
    private Button imagesButton;

    @FXML
    private Button statisticsButton;

    @FXML
    void containersScene(MouseEvent event) {
        borderPane.setCenter(anchorPane);
    }

    @FXML
    void graphicsScene(MouseEvent event) {
        loadPage("graphicsScen");
    }

    @FXML
    void imagesScene(MouseEvent event) {
        loadPage("imagesScen");
    }

    @FXML
    void statsScene(MouseEvent event) {
        loadPage("statisticsScen");
    }

    private void loadPage(String page) {
         Parent root = null;

        try {
            root = FXMLLoader.load(getClass().getResource(page+".fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        borderPane.setCenter(root);
    }

}
