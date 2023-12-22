package gr.aueb.dmst.dockerWatchdog;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

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

        TextField containerIdField = new TextField();

        TextArea instancesTextArea = new TextArea();
        instancesTextArea.setEditable(false);

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

        Button refreshInstancesButton = new Button("Refresh Instances");
        refreshInstancesButton.setOnAction(e -> {
            try {
                String instances = getAllInstances();
                instancesTextArea.setText(instances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Label startDateLabel = new Label("Start Date:");
        TextField startDateField = new TextField();

        Label endDateLabel = new Label("End Date:");
        TextField endDateField = new TextField();

        Button showMetricsButton = new Button("Show Metrics between dates");
        showMetricsButton.setOnAction(e -> {
            try {
                String startDateString = startDateField.getText();
                String endDateString = endDateField.getText();
                Timestamp startDate = Timestamp.valueOf(startDateString);
                Timestamp endDate = Timestamp.valueOf(endDateString);
                String metrics = getAllMetrics(startDate, endDate);
                metricsTextArea.setText(metrics);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        TextField runningInstancesField = new TextField();
        runningInstancesField.setEditable(false);

        vbox.getChildren().addAll(containerIdField, startButton, stopButton, refreshInstancesButton, new ScrollPane(instancesTextArea), startDateLabel, startDateField, endDateLabel, endDateField, showMetricsButton, new ScrollPane(metricsTextArea), runningInstancesField);

        Platform.runLater(() -> {
            try {
                String instances = getAllInstances();
                int runningCount = getRunningInstancesCount();
                runningInstancesField.setText("Running instances: " + runningCount);
                instancesTextArea.setText(instances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

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

    public String getAllMetrics(Timestamp startDate, Timestamp endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/metrics?startDate=" + startDateString + "&endDate=" + endDateString))
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