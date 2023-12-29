package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import gr.aueb.dmst.dockerWatchdog.Models.VolumeScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class VolumesController implements Initializable {

    @FXML
    private TableView<VolumeScene> volumesTableView;
    @FXML
    private TableColumn<VolumeScene,String> nameColumn;
    @FXML
    private TableColumn<VolumeScene,String> driverColumn;
    @FXML
    private TableColumn<VolumeScene,String> mountpointColumn;
    @FXML
    private TableColumn<VolumeScene,String> containerNamesUsingColumn;

    private Stage stage;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
            mountpointColumn.setCellValueFactory(new PropertyValueFactory<>("mountpoint"));
            containerNamesUsingColumn.setCellValueFactory(new PropertyValueFactory<>("Container Using"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        refreshVolumes();
    }

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

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

//    public void refreshVolumes() {
//        try {
//            List<VolumeScene> volumes = getAllVolumes();
//
//            volumesTableView.getItems().clear();
//
//            volumesTableView.getItems().addAll(volumes);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public List<VolumeScene> getAllVolumes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/volumes"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());
        List<VolumeScene> volumes = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            String driver = jsonObject.getString("driver");
            String mountpoint = jsonObject.getString("mountpoint");
            String containerNamesUsing = jsonObject.getString("containerNamesUsing");
            volumes.add(new VolumeScene(name, driver, mountpoint, containerNamesUsing));
        }
        return volumes;
    }
}
