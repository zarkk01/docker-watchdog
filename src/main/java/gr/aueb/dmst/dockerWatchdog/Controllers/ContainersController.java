package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.InstanceScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.Application.HelloApplication.client;

public class ContainersController implements Initializable {
    @FXML
    public TableView instancesTableView;

    @FXML
    private Label containerName;
    private Stage stage;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<InstanceScene> instances = getAllInstances();

            // Create TableColumn instances
            TableColumn<InstanceScene, String> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<InstanceScene, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<InstanceScene, String> statusColumn = new TableColumn<>("Status");
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Add TableColumn instances to the TableView
            instancesTableView.getColumns().add(idColumn);
            instancesTableView.getColumns().add(nameColumn);
            instancesTableView.getColumns().add(statusColumn);

            // Add the instances to the TableView
            instancesTableView.getItems().addAll(instances);
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

    public List<InstanceScene> parseJsonToContainerInstances(String json) {
        JSONArray jsonArray = new JSONArray(json);
        List<InstanceScene> instances = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String status = jsonObject.getString("status");
            instances.add(new InstanceScene(id, name, status));
        }
        return instances;
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
            String status = jsonObject.getString("status");
            instances.add(new InstanceScene(id, name, status));
        }
        return instances;
    }
}