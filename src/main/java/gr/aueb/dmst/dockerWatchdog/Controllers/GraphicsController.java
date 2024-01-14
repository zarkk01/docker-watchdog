package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.origin.SystemEnvironmentOrigin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class GraphicsController implements Initializable {

    private Stage stage;
    private Parent root;

    @FXML
    private LineChart<String,Number> cpuChart;

    @FXML
    private BarChart<String,Number> pidsChart;

    @FXML
    private LineChart<String,Number> memoryChart;

    @FXML
    private PieChart pieChartImages;
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
    @FXML
    public ImageView watchdogImage;

    private XYChart.Series<String, Number> cpuSeries;
    private XYChart.Series<String, Number> pidsSeries;
    private XYChart.Series<String, Number> memorySeries;

    private LocalDateTime currentTime;
    @Override
    public void initialize(java.net.URL arg0, java.util.ResourceBundle arg1) {
        try {
            startCharts();
            hoveredSideBarImages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            updateCharts();
            updatePidsChart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> {
            try {
                updateCharts();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            try {
                updatePidsChart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        timeline2.setCycleCount(Timeline.INDEFINITE);
        timeline2.play();

        // Install funny tooltip on watchdog imageView
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage,woof);
    }

    public void startCharts() throws Exception {
        cpuSeries = new XYChart.Series<>();
        cpuChart.getData().add(cpuSeries);

        pidsSeries = new XYChart.Series<>();
        pidsChart.getData().add(pidsSeries);

        memorySeries = new XYChart.Series<>();
        memoryChart.getData().add(memorySeries);

        startPieChart();
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

        Image originalVolume = new Image(getClass().getResourceAsStream("/images/volumesGrey.png"));

        // Load your hovered image
        Image hoveredVolume = new Image(getClass().getResourceAsStream("/images/volumes.png"));

        // Set the original image to the ImageView
        ((ImageView) volumesButton.getGraphic()).setImage(originalVolume);

        // Attach event handlers
        volumesButton.setOnMouseEntered(event -> {
            volumesButton.getStyleClass().add("button-hovered");
            ((ImageView) volumesButton.getGraphic()).setImage(hoveredVolume);
        });

        volumesButton.setOnMouseExited(event -> {
            volumesButton.getStyleClass().remove("button-hovered");
            ((ImageView) volumesButton.getGraphic()).setImage(originalVolume);
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

    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/volumesScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();
        changeScene(actionEvent, "volumesScene.fxml");
    }

    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
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
            Long memoryUsageL = jsonObject.getLong("memoryUsage");
            String memoryUsage = String.valueOf(memoryUsageL);
            Long pidsL = jsonObject.getLong("pids");
            String pids = String.valueOf(pidsL);
            Double cpuUsageD = jsonObject.getDouble("cpuUsage");
            String cpuUsage = String.valueOf(cpuUsageD);
            Double blockID = jsonObject.getDouble("blockI");
            double roundedI = Math.round(blockID * 10.0) / 10.0;
            String blockI = roundedI + "B";
            Double blockOD = jsonObject.getDouble("blockO");
            double rounded0 = Math.round(blockOD * 10.0) / 10.0;
            String blockO = rounded0 + "B";
            instances.add(new InstanceScene(id, name, image ,status, memoryUsage, pids, cpuUsage, blockI, blockO, null, null, null, null,false));
        }
        return instances;
    }

    public void updateCharts() throws Exception {

        List<InstanceScene> instances = getAllInstances();

        double totalCpuUsage = 0;
        currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        for (InstanceScene instance : instances) {
            String cpuUsage = instance.getCpuUsage();
            Double num = Double.parseDouble(cpuUsage);
            totalCpuUsage += num;
        }
        if(totalCpuUsage*100>50){
            updateCharts();
        }
        cpuSeries.getData().add(new XYChart.Data<>(formattedTime, totalCpuUsage*100));

        double totalMemoryUsage = 0;
        currentTime = LocalDateTime.now();
        String formatTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        for (InstanceScene instance : instances) {
            Double num = Double.parseDouble(instance.getMemoryUsage());
            totalMemoryUsage += num;
        }
        memorySeries.getData().add(new XYChart.Data<>(formatTime, totalMemoryUsage));

    }
    public void updatePidsChart() throws Exception {
        List<InstanceScene> instances = getAllInstances();
        pidsChart.getData().clear();
        for (InstanceScene instance : instances) {
            String simplerName;
            if(instance.getName().length() > 10){
                simplerName = instance.getName().substring(0, 8) + "...";
            } else {
                simplerName = instance.getName();
            }
            pidsSeries.getData().add(new XYChart.Data<>(simplerName, Long.parseLong(instance.getPids())));
        }
        pidsChart.getData().add(pidsSeries);
    }

    public void startPieChart() throws Exception {
        List<InstanceScene> instances = getAllInstances();
        HashMap<String, Integer> images = new HashMap<>();
        for (InstanceScene instance : instances) {
            String image = instance.getImage();
            if(images.containsKey(image)){
                images.put(image, images.get(image) + 1);
            } else {
                images.put(image, 1);
            }
        }
        for (String image : images.keySet()) {
            pieChartImages.getData().add(new PieChart.Data(image, images.get(image)));
        }

        pieChartImages.getData().forEach(data -> {
            data.nameProperty().bind(
                    Bindings.concat(
                            data.getName(), " : ", (int) data.pieValueProperty().get()
                    )
            );
        });
    }
}