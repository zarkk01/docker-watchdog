package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.InstanceScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static gr.aueb.dmst.dockerWatchdog.Application.HelloApplication.client;

public class ContainersController implements Initializable {
    @FXML
    public TableView instancesTableView;
    @FXML
    public TableColumn<InstanceScene, String> idColumn;
    @FXML
    public TableColumn<InstanceScene, String> nameColumn;
    @FXML
    public TableColumn<InstanceScene, String> imageColumn;
    @FXML
    public TableColumn<InstanceScene, String> statusColumn;
    @FXML
    public TableColumn<InstanceScene, String> cpuUsageColumn;
    @FXML
    public TableColumn<InstanceScene, String> pidsColumn;
    @FXML
    public TableColumn<InstanceScene, String> memoryUsageColumn;
    @FXML
    public TableColumn<InstanceScene, String> blockOColumn;
    @FXML
    public TableColumn<InstanceScene, String> blockIColumn;

    private Stage stage;
    private Parent root;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            cpuUsageColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));
            pidsColumn.setCellValueFactory(new PropertyValueFactory<>("pids"));
            memoryUsageColumn.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
            blockOColumn.setCellValueFactory(new PropertyValueFactory<>("blockO"));
            blockIColumn.setCellValueFactory(new PropertyValueFactory<>("blockI"));

            List<InstanceScene> instances = getAllInstances();
            instancesTableView.getItems().addAll(instances);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), event -> refreshInstances()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void refreshInstances(){
        try {
            List<InstanceScene> instances = getAllInstances();
            instancesTableView.getItems().clear();
            instancesTableView.getItems().addAll(instances);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<InstanceScene> getAllInstances() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());
        List<InstanceScene> instances = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String image = jsonObject.getString("image");
            String status = jsonObject.getString("status");
            Long memoryUsage = jsonObject.getLong("memoryUsage");
            Long pids = jsonObject.getLong("pids");
            Double cpuUsage = jsonObject.getDouble("cpuUsage");
            Double blockI = jsonObject.getDouble("blockI");
            Double blockO = jsonObject.getDouble("blockO");
            instances.add(new InstanceScene(id, name, image ,status, memoryUsage, pids, cpuUsage, blockI, blockO));
        }
        return instances;
    }
}