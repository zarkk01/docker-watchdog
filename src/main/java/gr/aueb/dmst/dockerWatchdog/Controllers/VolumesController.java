package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import gr.aueb.dmst.dockerWatchdog.Models.VolumeScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
    @FXML
    public Button containersButton;
    @FXML
    public Button imagesButton;
    @FXML
    public Button graphicsButton;
    @FXML
    public Button kubernetesButton;
    @FXML
    public Button volumesButton;

    private Stage stage;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
            mountpointColumn.setCellValueFactory(new PropertyValueFactory<>("mountpoint"));
            containerNamesUsingColumn.setCellValueFactory(new PropertyValueFactory<>("containerNamesUsing"));
            hoveredSideBarImages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        volumesTableView.setPlaceholder(new Label("No volumes available."));

        refreshVolumes();
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
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    public void refreshVolumes() {
        try {
            List<VolumeScene> volumes = getAllVolumes();

            volumesTableView.getItems().clear();

            volumesTableView.getItems().addAll(volumes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void hoveredSideBarImages() {
        Image originalContainers = new Image(getClass().getResourceAsStream("/images/containerGrey.png"));

        // Load your hovered image
        Image hoveredContainers = new Image(getClass().getResourceAsStream("/images/container.png"));

        // Set the original image to the ImageView
        ((ImageView) containersButton.getGraphic()).setImage(originalContainers);

        // Attach event handlers
        containersButton.setOnMouseEntered(event -> {
            containersButton.getStyleClass().add("button-hovered");
            ((ImageView) containersButton.getGraphic()).setImage(hoveredContainers);
        });

        containersButton.setOnMouseExited(event -> {
            containersButton.getStyleClass().remove("button-hovered");
            ((ImageView) containersButton.getGraphic()).setImage(originalContainers);
        });
    // Load your original image
        Image originalImage = new Image(getClass().getResourceAsStream("/images/imageGrey.png"));

        // Load your hovered image
        Image hoveredImage = new Image(getClass().getResourceAsStream("/images/image.png"));

        // Set the original image to the ImageView
        ((ImageView) imagesButton.getGraphic()).setImage(originalImage);

        // Attach event handlers
        imagesButton.setOnMouseEntered(event -> {
            imagesButton.getStyleClass().add("button-hovered");
            ((ImageView) imagesButton.getGraphic()).setImage(hoveredImage);
        });

        imagesButton.setOnMouseExited(event -> {
            imagesButton.getStyleClass().remove("button-hovered");
            ((ImageView) imagesButton.getGraphic()).setImage(originalImage);
        });
        Image originalGraphics = new Image(getClass().getResourceAsStream("/images/graphicsGrey.png"));

        // Load your hovered image
        Image hoveredGraphics = new Image(getClass().getResourceAsStream("/images/graphics.png"));

        // Set the original image to the ImageView
        ((ImageView) graphicsButton.getGraphic()).setImage(originalGraphics);

        // Attach event handlers
        graphicsButton.setOnMouseEntered(event -> {
            graphicsButton.getStyleClass().add("button-hovered");
            ((ImageView) graphicsButton.getGraphic()).setImage(hoveredGraphics);
        });

        graphicsButton.setOnMouseExited(event -> {
            graphicsButton.getStyleClass().remove("button-hovered");
            ((ImageView) graphicsButton.getGraphic()).setImage(originalGraphics);
        });

        Image originalKubernetes = new Image(getClass().getResourceAsStream("/images/kubernetesGrey.png"));

        // Load your hovered image
        Image hoveredKubernetes = new Image(getClass().getResourceAsStream("/images/kubernetes.png"));

        // Set the original image to the ImageView
        ((ImageView) kubernetesButton.getGraphic()).setImage(originalKubernetes);

        // Attach event handlers
        kubernetesButton.setOnMouseEntered(event -> {
            kubernetesButton.getStyleClass().add("button-hovered");
            ((ImageView) kubernetesButton.getGraphic()).setImage(hoveredKubernetes);
        });

        kubernetesButton.setOnMouseExited(event -> {
            kubernetesButton.getStyleClass().remove("button-hovered");
            ((ImageView) kubernetesButton.getGraphic()).setImage(originalKubernetes);
        });
    }

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
