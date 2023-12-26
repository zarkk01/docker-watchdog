package gr.aueb.dmst.dockerWatchdog.Controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ContainersController implements Initializable {

    @FXML
    private ListView<String> myContainers;

    @FXML
    private Label containerName;
    private Stage stage;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        myContainers.getItems().add("Container 1");
        myContainers.getItems().add("Container 2");
        myContainers.getItems().add("Container 3");
        myContainers.getItems().add("Container 4");
        myContainers.getItems().add("Container 5");

        myContainers.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            containerName.setText(t1);
        });
    }

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    public void changeToStatisticsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "statisticsScene.fxml");
    }
}