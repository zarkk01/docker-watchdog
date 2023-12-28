package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class IndividualContainerController {

    @FXML
    private VBox infoCard;
    @FXML
    private Label containerIdLabel;
    @FXML
    private Label containerNameLabel;
    @FXML
    private Label containerStatusLabel;
    @FXML
    private Button removeButton;
    @FXML
    private Button renameButton;

    private InstanceScene instanceScene;
    private Stage stage;
    private Parent root;

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToStatisticsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "statisticsScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    public void onInstanceDoubleClick(InstanceScene instance) {
        this.instanceScene = instance;
        containerIdLabel.setText("ID: " + instance.getId());
        containerNameLabel.setText("Name: " + instance.getName());
        containerStatusLabel.setText("Status: " + instance.getStatus());
        infoCard.setVisible(true);
    }
}