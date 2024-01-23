package gr.aueb.dmst.dockerWatchdog.Controllers;

import java.util.Optional;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.FadeTransition;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerActionFailedException;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

import org.json.JSONObject;

/**
 * FX Controller for the IndividualContainer panel.
 * It provides methods for handling user interactions with a specific instance like starting, stopping,
 * pausing, unpausing, renaming, and removing it.
 * It also provides methods for changing scenes, showing notifications,
 * and updating charts, while also copying the ID of the container.
 * The class uses our WATCHDOG REST API so to communicate with
 * the backend and send requests for information and actions.
 */
public class IndividualContainerController {

    // The base URL of the WATCHDOG REST API.
    private static final String BASE_URL = "http://localhost:8080/api/containers/";
    // The specific instance that the user has selected.
    private InstanceScene instanceScene;

    private Stage stage;
    private Parent root;

    @FXML
    private SplitPane infoCard;
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
    public Label logsLabel;
    @FXML
    TextArea textArea;

    @FXML
    private VBox notificationBox;

    @FXML
    private Button backButton;
    @FXML
    private Button copyButton;

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button renameButton;
    @FXML
    private Button pauseContainerButton;
    @FXML
    private Button unpauseButton;
    @FXML
    private Button restartButton;
    @FXML
    public ImageView watchdogImage;

    @FXML
    private Button removeButton;

    @FXML
    private LineChart<String,Number> individualCpuChart;
    private XYChart.Series<String, Number> individualCpuSeries;

    private Timeline timeline;
    private String fromWhere;

    /**
     * This method is called when a user clicks on a container from the containers list in the Containers Panel.
     * It sets up the IndividualContainer panel with the selected container's details and starts the log fetcher for the container.
     * It also sets up the hover effects for the buttons and starts the CPU usage chart updater.
     *
     * @param instance The InstanceScene object representing the selected container.
     */
    public void onInstanceDoubleClick(InstanceScene instance, String fromWhere) {
        this.fromWhere = fromWhere;
        // Set the selected instance.
        this.instanceScene = instance;

        // Set up the labels with the selected container's details.
        containerIdLabel.setText("ID : " + instance.getId());
        containerNameLabel.setText("Name: " + instance.getName());
        containerStatusLabel.setText("Status: " + instance.getStatus());
        containerImageLabel.setText("Image: " + instance.getImage());
        containerVolumesLabel.setText("Volumes: " + instance.getVolumes());
        containerSubnetLabel.setText("Subnet:" + instance.getSubnet() + "/" + instance.getPrefixLen());
        containerGatewayLabel.setText("Gateway:" + instance.getGateway());

        // Start the log fetcher for the selected container.
        containerLogInfoAppender(instance);

        // Make the info card visible.
        infoCard.setVisible(true);

        // Set up the hover effects for all buttons.
        DropShadow dropShadow = new DropShadow();
        removeButton.setEffect(dropShadow);
        startButton.setEffect(dropShadow);
        stopButton.setEffect(dropShadow);
        restartButton.setEffect(dropShadow);
        pauseContainerButton.setEffect(dropShadow);
        unpauseButton.setEffect(dropShadow);
        renameButton.setEffect(dropShadow);

        setupButton(backButton, new ImageView(), "/images/back.png", "/images/backHover.png", 20);
        setupButton(removeButton, new ImageView(), "/images/binRed.png", "/images/binHover.png", 50);
        setupButton(copyButton, new ImageView(), "/images/copy.png", "/images/copyHover.png", 38);

        // Set up the CPU usage chart for the selected container.
        individualCpuSeries = new XYChart.Series<>();
        individualCpuChart.getData().add(individualCpuSeries);
        individualCpuChart.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        individualCpuChart.setTitle("CPU Usage of " + this.instanceScene.getName() + " in %");

        // Set the logs label.
        logsLabel.setText("Logs of " + instance.getName());

        // Start the CPU usage chart updater.
        try {
            updateIndividualCpuChart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set up the chart updater to run every 4 seconds.
        timeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> {
            try {
                updateIndividualCpuChart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Install funny tooltip on watchdog imageView
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage,woof);
    }

    /**
     * Sets up a button with an image and hover effect.
     * The image changes when the mouse enters and exits the button.
     * The button's graphic is set to an ImageView of the image.
     * It is used so to automate the process of setting up the hover effect for all 3 buttons.
     *
     * @param button The button to set up.
     * @param view The ImageView to set as the button's graphic.
     * @param imagePath The path to the image for the button.
     * @param hoverImagePath The path to the image for the button's hover effect.
     * @param fitHeight The height to fit the ImageView to.
     */
    private void setupButton(Button button, ImageView view, String imagePath, String hoverImagePath, double fitHeight) {
        // Load the image from the given path and set it to the ImageView.
        Image img = new Image(getClass().getResource(imagePath).toExternalForm());
        view.setImage(img);

        // Fit the ImageView to the given height and preserve its ratio.
        view.setFitHeight(fitHeight);
        view.setPreserveRatio(true);
        button.setGraphic(view);

        // Load the hover image from the given path.
        Image imgHover = new Image(getClass().getResource(hoverImagePath).toExternalForm());

        // Set the hover effect: when the mouse enters the button, change the image and reduce its opacity.
        button.setOnMouseEntered(event -> {
            view.setImage(imgHover);
            view.setOpacity(0.8);
        });

        // Remove the hover effect: when the mouse exits the button,
        // change the image back to the original and restore its opacity.
        button.setOnMouseExited(event -> {
            view.setImage(img);
            view.setOpacity(1);
        });
    }

    /**
     * This method uses the Docker Java API to fetch the logs of the selected container.
     * The logs are appended to the TextArea in the GUI.
     *
     * @param instance The InstanceScene object representing the selected container.
     */
    public void containerLogInfoAppender(InstanceScene instance) {
        // Get the ID of the selected container.
        String containerId = instance.getId();

        // Start the log fetcher for the selected container.
        Main.dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        // Convert the log frame to a string.
                        String logLine = item.toString();

                        // Append the log line to the TextArea in the GUI.
                        javafx.application.Platform.runLater(() -> {
                            textArea.appendText(logLine + "\n");
                        });
                    }
                });
    }

    /**
     * Starts the selected container.
     * This method sends a POST request to the WATCHDOG REST API to start the selected container.
     * If the container is successfully started, it updates the container's status and shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to start.
     */
    public void startContainer() throws ContainerActionFailedException {
        try {
            // Create the POST request to start the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/start"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the container is successfully started, show a notification and update the container's status.
            if (response.statusCode() == 200) {
                showNotification("Woof!", "Container " + this.instanceScene.getName() + " has started.");
                this.instanceScene.setStatus("running");
                containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
            }
        } catch (Exception e) {
            throw new ContainerActionFailedException("start", this.instanceScene.getId());
        }
    }

    /**
     * Stops the selected container.
     * This method sends a POST request to the WATCHDOG REST API to stop the selected container.
     * If the container is successfully stopped, it updates the container's status and shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to stop.
     */
    public void stopContainer() throws ContainerActionFailedException {
        try {
            // Create the POST request to stop the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/stop"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the container is successfully stopped, show a notification and update the container's status.
            if (response.statusCode() == 200) {
                showNotification("Woof!", "Container " + this.instanceScene.getName() + " is about to stop. It may take a few seconds.");
                this.instanceScene.setStatus("exited");
                containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
            }
        } catch (Exception e) {
            throw new ContainerActionFailedException("stop", this.instanceScene.getId());
        }
    }

    /**
     * Pauses the selected container.
     * This method checks if the container is running and if it is, then it
     * sends a POST request to the WATCHDOG REST API to pause the selected container.
     * If the container is successfully paused, it updates the container's status and shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to pause.
     */
    public void pauseContainer() throws ContainerActionFailedException {
        // Check if the container is already paused so to not even bother trying to pause.
        if(this.instanceScene.getStatus().equals("paused")) {
            // If it is paused, show a notification and return.
            showNotification("Woof!", "Container " + this.instanceScene.getName() + " is already paused.");
            return;
        }

        try {
            // Create the POST request to pause the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/pause"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the container is successfully paused, show a notification and update the container's status.
            if (response.statusCode() == 200) {
                showNotification("Woof!", "Container " + this.instanceScene.getName() + " has paused.");
                this.instanceScene.setStatus("paused");
                containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
            }
        } catch (Exception e) {
            throw new ContainerActionFailedException("pause", this.instanceScene.getId());
        }
    }

    /**
     * Unpauses the selected container.
     * This method checks if the container is paused and if it is, then it
     * sends a POST request to the WATCHDOG REST API to unpause the selected container.
     * If the container is successfully unpaused, it updates the container's status and shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to unpause.
     */
    public void unpauseContainer() throws ContainerActionFailedException {
        // Check if the container is not paused so to not even bother trying to unpause.
        if(this.instanceScene.getStatus().equals("running")) {
            // If it is running, show a notification and return.
            showNotification("Woof!", "Container " + this.instanceScene.getName() + " is not paused.");
            return;
        }

        try {
            // Create the POST request to unpause the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/unpause"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the container is successfully unpaused, show a notification and update the container's status.
            if (response.statusCode() == 200) {
                showNotification("Woof!", "Container " + this.instanceScene.getName() + " has unpause.");
                this.instanceScene.setStatus("running");
                containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
            }
        } catch (Exception e) {
            throw new ContainerActionFailedException("unpause", this.instanceScene.getId());
        }
    }

    /**
     * Renames the selected container.
     * This method prompts the user for a new name for the container, then sends a POST request to the WATCHDOG REST API to rename the container.
     * If the container is successfully renamed, it updates the container's name and shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to rename.
     */
    public void renameContainer() throws ContainerActionFailedException {
        // Prompt the user for a new name for the container.
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Container");
        dialog.setHeaderText("Enter the new name for the container:");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        try {
            result.ifPresent(newName -> {
                if (newName == null || newName.trim().isEmpty()) {
                    return;
                }

                try {
                    // Create the POST request to rename the selected container and send it.
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(BASE_URL + this.instanceScene.getId() + "/rename?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8)))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // If the container is successfully renamed, show a notification and update the container's name.
                    if (response.statusCode() == 200) {
                        showNotification("Woof!", "Container " + this.instanceScene.getName() + " has renamed to " + newName + ".");
                        containerNameLabel.setText("Name: " + newName);
                        this.instanceScene.setName(newName);
                    }
                } catch (URISyntaxException | IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ContainerActionFailedException("rename", this.instanceScene.getId());
        }
    }

    /**
     * Restarts the selected container.
     * This method sends a POST request to the WATCHDOG REST API to restart the selected container.
     * If the container is successfully restarted, it shows a notification.
     *
     * @throws ContainerActionFailedException If the container fails to restart.
     */
    public void restartContainer() throws ContainerActionFailedException {
        try {
            // Create the POST request to restart the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/restart"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the container is successfully restarted, show a notification.
            if (response.statusCode() == 200) {
                showNotification("Container Event", "Container " + this.instanceScene.getName() + " has restarted.");
            }
        } catch (Exception e) {
            throw new ContainerActionFailedException("restart", this.instanceScene.getId());
        }
    }

    /**
     * Removes the selected container.
     * This method sends a POST request to the WATCHDOG REST API to remove the selected container.
     * If the container is successfully removed, it changes the scene to the Containers scene.
     *
     * @param actionEvent The event that triggered the container removal.
     * @throws ContainerActionFailedException If the container fails to remove.
     */
    public void removeContainer(ActionEvent actionEvent) throws ContainerActionFailedException {
        try {
            // Create the POST request to remove the selected container and send it.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + this.instanceScene.getId() + "/delete"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());

            // Change the scene to the Containers scene as the container has been removed.
            changeScene(actionEvent, "containersScene.fxml");
        } catch (Exception e) {
            throw new ContainerActionFailedException("remove", this.instanceScene.getId());
        }
    }

    /**
     * Copies the ID of the selected container to the clipboard.
     * This method retrieves the ID of the selected container from the containerIdLabel,
     * then copies it to the system clipboard. It also shows a notification to inform the user that the ID has been copied.
     */
    public void copyId() {
        // Get the ID of the selected container from the containerIdLabel.
        String id = containerIdLabel.getText();

        // Get the system clipboard.
        final Clipboard clipboard = Clipboard.getSystemClipboard();

        // Create a new ClipboardContent and put the container ID into it.
        final ClipboardContent content = new ClipboardContent();
        content.putString(id.substring(5));

        // Set the clipboard's content to the ClipboardContent we just created.
        clipboard.setContent(content);

        // Show a notification to inform the user that the container ID has been copied to the clipboard.
        showNotification("Copy", "Container ID copied to clipboard");
    }

    /**
     * Retrieves information about the current instance from the WATCHDOG REST API.
     * This method sends a GET request to the "/containers/{id}/info" endpoint of the API,
     * where {id} is the ID of the current instance.
     * The API responds with a JSON object containing the instance's information.
     * This method then parses the JSON object into an InstanceScene object and returns it.
     *
     * @return An InstanceScene object representing the current instance.
     */
    public InstanceScene getInstanceInfo() throws Exception {
        // Create the GET request to get information about the current instance and send it.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + this.instanceScene.getId() + "/info"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response into a JSONObject.
        JSONObject jsonObject = new JSONObject(response.body());

        // Extract the instance's information from the JSONObject.
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        String image = jsonObject.getString("image");
        String status = jsonObject.getString("status");
        Long memoryUsageL = jsonObject.getLong("memoryUsage");
        String memoryUsage = memoryUsageL + "MB";
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
        String volumes = jsonObject.getString("volumes");
        String subnet = jsonObject.getString("subnet");
        String gateway = jsonObject.getString("gateway");
        Integer prefixLen = jsonObject.getInt("prefixLen");

        // Create and return our containers us InstanceScene object.
        return new InstanceScene(id, name, image , status, memoryUsage, pids, cpuUsage, blockI, blockO, volumes, subnet, gateway, prefixLen, false);
    }

    /**
     * Updates the CPU usage chart for the current instance.
     * This method retrieves the current instance's information from the WATCHDOG REST API,
     * then extracts the CPU usage from the instance's information and adds it to the CPU usage chart.
     * The CPU usage is added as a data point in the chart's series, with the current time as the X value and the CPU usage as the Y value.
     * If the CPU usage exceeds 20%, the method recursively calls itself to update the chart again, because it may be an error.
     */
    public void updateIndividualCpuChart() throws Exception {
        // Retrieve the current instance's information from the WATCHDOG REST API.
        InstanceScene instance = getInstanceInfo();

        // Extract the CPU usage from the instance's information.
        String cpuUsage = instance.getCpuUsage();
        Double individualCpuUsage = Double.parseDouble(cpuUsage);

        // Get the current time and format it as a string.
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // If the CPU usage exceeds 20%, recursively call this method to update the chart again.
        if (individualCpuUsage*100>20) {
            updateIndividualCpuChart();
        } else {
            // Otherwise, add the CPU usage as a data point in the chart's series.
            individualCpuSeries.getData().add(new XYChart.Data<>(formattedTime, individualCpuUsage * 100));
        }
    }

    /**
     * Displays a notification with the given title and content.
     * It helps us keep user informed about events that occur in the instance.
     * After 4 seconds, the Popup is automatically hidden.
     *
     * @param title The title of the notification.
     * @param content The content of the notification.
     */
    public void showNotification(String title, String content) {
        Platform.runLater(() -> {
            // Create a new Popup for the notification.
            Popup notification = new Popup();

            // Create a Label for the title of the notification and style it.
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

            // Create a Label for the content of the notification and style it.
            Label contentLabel = new Label(content);
            contentLabel.setTextFill(Color.WHITE);

            // Create a VBox to hold the title and content Labels and style it with our brand colors.
            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #e14b4e; -fx-padding: 10px; -fx-border-color: #525252; -fx-border-width: 1px;");

            // Add the VBox to the Popup.
            notification.getContent().add(box);

            // Calculate the position of the Popup on the screen.
            Point2D point = notificationBox.localToScreen(notificationBox.getWidth() - box.getWidth(), notificationBox.getHeight() - box.getHeight());

            // Show the Popup on the screen at the calculated position.
            notification.show(notificationBox.getScene().getWindow(), point.getX(), point.getY());

            // Create a FadeTransition for the VBox.
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), box);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);

            // Create a Timeline that will start the FadeTransition after 4 seconds.
            Timeline timelineNotification = new Timeline(new KeyFrame(Duration.seconds(4), evt -> fadeTransition.play()));
            timelineNotification.play();

            // Hide the Popup when the FadeTransition is finished.
            fadeTransition.setOnFinished(event -> notification.hide());
        });
    }

    /**
     * Changes the current scene to the Containers scene.
     * This method calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Containers scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    /**
     * Changes the current scene to the Images scene.
     * This method calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Images scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    /**
     * Changes the current scene to the Volumes scene.
     * This method first loads the Volumes scene and refreshes the volumes.
     * Then, it calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Volumes scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        // Load the Volumes scene and refresh the volumes.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/volumesScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();

        // Change the scene to the Volumes scene.
        changeScene(actionEvent, "volumesScene.fxml");
    }

    /**
     * Changes the current scene to the Graphics scene.
     * This method calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Graphics scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    /**
     * Changes the current scene to the Kubernetes scene.
     * This method calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Kubernetes scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    public void changeToBackScene(ActionEvent actionEvent) throws IOException {
        if(fromWhere == "containersScene.fxml") {
            changeScene(actionEvent, "containersScene.fxml");
        } else {
            changeScene(actionEvent, "imagesScene.fxml");
        }
    }

    /**
     * Changes the current scene to a new scene.
     * This method loads the FXML file for the new scene,
     * sets it as the root of the current stage,
     * and displays the new scene. It is used to navigate between different scenes in the application.
     * It also stops the Timeline which keeps updating the CPU usage chart and prevents
     * the Timeline from running in the background when the user is not on the IndividualContainer scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @param fxmlFile The name of the FXML file for the new scene.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        // Stop the Timeline which keeps updating the CPU usage chart.
        stopTimeline();
        // Load the FXML file for the new scene.
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));

        // Get the current stage.
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        // Set the new scene as the root of the stage and display it.
        stage.getScene().setRoot(root);
        stage.show();
    }

    /**
     * Stops the Timeline if it is not null.
     * This method is used to stop the Timeline when the user leaves the scene.
     * Stopping the Timeline can help to reduce lag in the program.
     */
    public void stopTimeline() {
        // Check if the timeline is not null
        if (timeline != null) {
            // If it's not null, stop the timeline
            timeline.stop();
        }
    }
}
