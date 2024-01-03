package gr.aueb.dmst.dockerWatchdog.Controllers;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class IndividualContainerController {
    private static final String BASE_URL = "http://localhost:8080/api/";
    @FXML
    private SplitPane infoCard;

    @FXML
    private Text headTextContainer;
    @FXML
    private Label containerIdLabel;
    @FXML
    private Label containerNameLabel;
    @FXML
    private Label containerStatusLabel;
    @FXML
    private Label containerImageLabel;
    @FXML
    private Label containerSubnetLabel;
    @FXML
    private Label containerGatewayLabel;
    @FXML
    private Label containerVolumesLabel;
    @FXML
    private VBox notificationBox;
    @FXML
    private Button backButton;

    @FXML
    public Label logsLabel;

    @FXML
    TextArea textArea;

    @FXML
    private LineChart<String,Number> individualCpuChart;
    private XYChart.Series<String, Number> individualCpuSeries;

    private InstanceScene instanceScene;
    private Stage stage;
    private Parent root;

    /**
     * Changes the scene to the specified FXML file.
     *
     * @param actionEvent The ActionEvent triggering the scene change.
     * @param fxmlFile    The name of the FXML file to load.
     * @throws IOException If an I/O error occurs during loading.
     */
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

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    /**
     * Handles a double click on a container instance.
     *
     * @param instance The selected container instance.
     */
    public void onInstanceDoubleClick(InstanceScene instance) {
        this.instanceScene = instance;
        headTextContainer.setText("Container: " + instance.getName());
        containerIdLabel.setText("ID : " + instance.getId());
        containerNameLabel.setText("Name: " + instance.getName());
        containerStatusLabel.setText("Status: " + instance.getStatus());
        containerImageLabel.setText("Image: " + instance.getImage());
        containerVolumesLabel.setText("Volumes: " + instance.getVolumes());
        containerSubnetLabel.setText("Subnet:" + instance.getSubnet() + "/" + instance.getPrefixLen());
        containerGatewayLabel.setText("Gateway:" + instance.getGateway());
        containerLogInfoAppender(instance);
        infoCard.setVisible(true);

        individualCpuSeries = new XYChart.Series<>();
        individualCpuChart.getData().add(individualCpuSeries);
        individualCpuChart.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        individualCpuChart.setTitle("CPU Usage of " + this.instanceScene.getName() + " in %");

        logsLabel.setText("Logs of " + instance.getName());

        Image img = new Image(getClass().getResource("/images/back.png").toExternalForm());
        ImageView view = new ImageView(img);
        view.setFitHeight(20);
        view.setPreserveRatio(true);

        backButton.setGraphic(view);

        try {
            updateIndividualCpuChart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> {
            try {
                updateIndividualCpuChart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Appends log information to the text area for the specified container instance.
     *
     * @param instance The container instance.
     */
    public void containerLogInfoAppender(InstanceScene instance) {
        // Specify container ID or name
        String containerId = instance.getId();

        // Execute the command and update the TextArea with each log frame
        Main.dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        // Process each log frame
                        String logLine = item.toString();

                        // Update the TextArea on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            textArea.appendText(logLine + "\n");
                        });
                    }
                });
    }

    /**
     * Removes the selected container.
     *
     * @param actionEvent The ActionEvent triggering the removal.
     * @throws IOException            If an I/O error occurs.
     * @throws InterruptedException   If the operation is interrupted.
     * @throws URISyntaxException      If the URI is invalid.
     */

    public void removeContainer(ActionEvent actionEvent) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/delete"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        changeScene(actionEvent, "containersScene.fxml");
    }

    /**
     * Pauses the selected container.
     *
     * @throws IOException           If an I/O error occurs.
     * @throws InterruptedException  If the operation is interrupted.
     * @throws URISyntaxException     If the URI is invalid.
     */
    public void pauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/pause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has pause.");
        }

        this.instanceScene.setStatus("Paused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void unpauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/unpause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has unpause.");
        }

        this.instanceScene.setStatus("Unpaused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void renameContainer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Container");
        dialog.setHeaderText("Enter the new name for the container:");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName == null || newName.trim().isEmpty()) {
                return;
            }

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/rename?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8)))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    showNotification("Container Event", "Container " + this.instanceScene.getName() + " has renamed to " + newName + ".");
                }

                containerNameLabel.setText("Name: " + newName);
                this.instanceScene.setName(newName);
                headTextContainer.setText("Container: " + newName);
            } catch (IOException | InterruptedException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public void startContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has started.");
        }

        this.instanceScene.setStatus("running");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void stopContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has stopped.");
        }

        this.instanceScene.setStatus("exited");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void restartContainer() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Restarting the container with ID " + this.instanceScene.getId() + "...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/restart"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has restarted.");
        }

    }



    /**
     * Shows a notification with the specified title and content.
     *
     * @param title   The title of the notification.
     * @param content The content of the notification.
     */
    public void showNotification(String title, String content) {
        Platform.runLater(() -> {
            Popup notification = new Popup();

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
            Label contentLabel = new Label(content);
            contentLabel.setTextFill(Color.WHITE);

            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #4272F1; -fx-padding: 10px; -fx-border-color: black; -fx-border-width: 1px;");

            notification.getContent().add(box);

            Point2D point = notificationBox.localToScreen(notificationBox.getWidth() - box.getWidth(), notificationBox.getHeight() - box.getHeight());

            notification.show(notificationBox.getScene().getWindow(), point.getX(), point.getY());

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timeline.play();
        });
    }

    public List<InstanceScene> getAllInstances() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "containers/instances"))
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
            String volumes = jsonObject.getString("volumes");
            String subnet = jsonObject.getString("subnet");
            String gateway = jsonObject.getString("gateway");
            Integer prefixLen = jsonObject.getInt("prefixLen");
            instances.add(new InstanceScene(id, name, image ,status, memoryUsage, pids, cpuUsage, blockI, blockO, volumes, subnet, gateway, prefixLen , false));
        }
        return instances;
    }

    /**
     * Updates the individual CPU chart for the selected container instance.
     *
     * @throws Exception If an error occurs during the HTTP request.
     */
    public void updateIndividualCpuChart() throws Exception {
        List<InstanceScene> instances = getAllInstances();
        Double individualCpuUsage = 0.0;
        for (InstanceScene instance : instances) {
            if (instance.getId().equals(this.instanceScene.getId())) {
                individualCpuUsage = instance.getCpuUsage();
            }
        }
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        if(individualCpuUsage*100>20){
            updateIndividualCpuChart();
        } else {
            individualCpuSeries.getData().add(new XYChart.Data<>(formattedTime, individualCpuUsage*100));
        }
    }
}