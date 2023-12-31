package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
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
    private XYChart.Series<String, Number> cpuSeries;
    private XYChart.Series<String, Number> pidsSeries;
    private XYChart.Series<String, Number> memorySeries;

    private LocalDateTime currentTime;
    @Override
    public void initialize(java.net.URL arg0, java.util.ResourceBundle arg1) {
        startCharts();

        try {
            updateCharts();
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
    }

    public void startCharts(){
        cpuSeries = new XYChart.Series<>();
        cpuChart.getData().add(cpuSeries);

        pidsSeries = new XYChart.Series<>();
        pidsChart.getData().add(pidsSeries);

        memorySeries = new XYChart.Series<>();
        memoryChart.getData().add(memorySeries);}

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

    public void updateCharts() throws Exception {
        List<InstanceScene> instances = getAllInstances();
        double totalCpuUsage = 0;
        currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        for (InstanceScene instance : instances) {
            totalCpuUsage += instance.getCpuUsage();
        }
        if(totalCpuUsage*100>50){
            updateCharts();
        }
        cpuSeries.getData().add(new XYChart.Data<>(formattedTime, totalCpuUsage*100));

        double totalMemoryUsage = 0;
        currentTime = LocalDateTime.now();
        String formatTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        for (InstanceScene instance : instances) {
            totalMemoryUsage += instance.getMemoryUsage();
        }
        memorySeries.getData().add(new XYChart.Data<>(formatTime, totalMemoryUsage));

        List<InstanceScene> instan = getAllInstances();
        for (InstanceScene instance : instan) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(instance.getName(), instance.getPids());
            pidsSeries.getData().add(data);
        }
    }
}