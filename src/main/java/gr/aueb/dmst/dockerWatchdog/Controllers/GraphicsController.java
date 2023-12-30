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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class GraphicsController implements Initializable {

    private Stage stage;
    private Parent root;

    @FXML
    private LineChart<Number, Number> cpuChart;

    private XYChart.Series<Number, Number> series;
    private Number currentTime = 0;

    @Override
    public void initialize(java.net.URL arg0, java.util.ResourceBundle arg1) {
        // Initialize the LineChart object
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(1.0);
        yAxis.setTickUnit(0.1);

        cpuChart = new LineChart<>(new NumberAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        cpuChart.getData().add(series);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                updateChart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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

    private void updateChart() throws Exception {
        List<InstanceScene> instances = getAllInstances();
        double totalCpuUsage = 0;
        for (InstanceScene instance : instances) {
            totalCpuUsage += instance.getCpuUsage();
        }
        series.getData().add(new XYChart.Data<>(currentTime, totalCpuUsage));
        currentTime = currentTime.intValue() + 10;
    }
}