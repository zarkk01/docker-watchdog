package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DesktopApp extends Application {

    private HttpClient client;

    public DesktopApp() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Docker Control");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        Label containerIdLabel = new Label("Container ID: ");
        TextField containerIdField = new TextField();

        ListView<String> instancesListView = new ListView<>();
        ObservableList<String> instancesList = FXCollections.observableArrayList();

        TextArea metricsTextArea = new TextArea();
        metricsTextArea.setEditable(false);

        Button startButton = new Button("Start Container");
        startButton.setOnAction(e -> {
            try {
                String containerId = containerIdField.getText();
                startContainer(containerId);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Button stopButton = new Button("Stop Container");
        stopButton.setOnAction(e -> {
            try {
                String containerId = containerIdField.getText();
                stopContainer(containerId);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        TextField runningInstancesField = new TextField();
        runningInstancesField.setEditable(false);

        Button refreshInstancesButton = new Button("Refresh Instances");
        refreshInstancesButton.setOnAction(e -> {
            try {
                String instances = getAllInstances();
                instancesList.clear();
                instancesList.addAll(instances.split("\n"));
                instancesListView.setItems(instancesList);
                int runningCount = getRunningInstancesCount();
                runningInstancesField.setText("Running instances: " + runningCount);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        // Create a Timeline that calls refreshInstancesButton's action every 0.5 seconds
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> refreshInstancesButton.fire())
        );
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        Label dateLabel = new Label("Date:");
        TextField dateField = new TextField();

        Button showMetricsButton = new Button("Show how many metrics had done " +
                "and how many containers was running in the date you choose");
        showMetricsButton.setOnAction(e -> {
            try {
                String dateString = dateField.getText();
                Timestamp date = Timestamp.valueOf(dateString);
                String metrics = getMetricsAndRunningCon(date);
                metricsTextArea.setText(metrics);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Platform.runLater(() -> {
            try {
                String instances = getAllInstances();
                ObservableList<String> instancesL = FXCollections.observableArrayList(instances.split("\n"));
                instancesListView.setItems(instancesL);
                int runningCount = getRunningInstancesCount();
                runningInstancesField.setText("Running instances: " + runningCount);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        vbox.getChildren().addAll(containerIdLabel,containerIdField, startButton, stopButton, new ScrollPane(instancesListView), dateLabel, dateField, showMetricsButton, new ScrollPane(metricsTextArea), runningInstancesField);

        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void startContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void stopContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String getAllInstances() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        StringBuilder formattedResponse = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            formattedResponse.append(jsonObject.toString(4)); // 4 is the number of spaces for indentation
            formattedResponse.append("\n");
        }
        return formattedResponse.toString();
    }

    public String getMetricsAndRunningCon(Timestamp chosenDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
        String chosenDateString = dateFormat.format(chosenDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/metrics?chosenDate=" + chosenDateString))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response body into a List<Long>
        JSONArray jsonArray = new JSONArray(response.body());
        List<Long> metricsList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            metricsList.add(jsonArray.getLong(i));
        }

        // Convert the List<Long> into a JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("How many metrics were be done then ", metricsList.get(0));
        jsonObject.put("How many containers were running then ", metricsList.get(1));

        return jsonObject.toString(4); // 4 is the number of spaces for indentation
    }

    public int getRunningInstancesCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        int runningCount = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if ("running".equals(jsonObject.getString("status"))) {
                runningCount++;
            }
        }
        return runningCount;
    }
}