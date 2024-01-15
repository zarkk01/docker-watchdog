package gr.aueb.dmst.dockerWatchdog.Controllers;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.scene.control.Button;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContainersController implements Initializable {
    @FXML
    private TableView instancesTableView;
    @FXML
    private TableColumn<InstanceScene, String> idColumn;
    @FXML
    private TableColumn<InstanceScene, String> nameColumn;
    @FXML
    private TableColumn<InstanceScene, String> imageColumn;
    @FXML
    private TableColumn<InstanceScene, String> statusColumn;
    @FXML
    private TableColumn<InstanceScene, String> cpuUsageColumn;
    @FXML
    private TableColumn<InstanceScene, String> pidsColumn;
    @FXML
    private TableColumn<InstanceScene, String> memoryUsageColumn;
    @FXML
    private TableColumn<InstanceScene, String> blockOColumn;
    @FXML
    private TableColumn<InstanceScene, String> blockIColumn;
    @FXML
    private TableColumn<InstanceScene, Void> actionButtonColumn;
    @FXML
    private TableColumn<InstanceScene, Void> selectColumn;

    @FXML
    private VBox notificationBox;

    @FXML
    private TextField searchField;

    @FXML
    private TextField datetimeTextField;
    @FXML
    private Label metricsLabel;
    @FXML
    private Button removeButton;
    @FXML
    private CheckBox runningInstancesCheckbox;

    @FXML
    private Label totalContainersText;
    @FXML
    private Label runningContainersText;
    @FXML
    private Label stoppedContainersText;

    @FXML
    private Button uploadButton;
    @FXML
    private Button imagesButton;
    @FXML
    private Button graphicsButton;
    @FXML
    private Button kubernetesButton;
    @FXML
    private Button volumesButton;
    @FXML
    private ImageView watchdogImage;

    private Stage stage;
    private Parent root;

    private Boolean selectedDateTime = false;

    private Map<String, Boolean> checkboxStates = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            
            hoveredSideBarImages();

            Tooltip woof = new Tooltip("Woof!");
            woof.setShowDelay(Duration.millis(20));
            Tooltip.install(watchdogImage,woof);
            
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            cpuUsageColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));
            pidsColumn.setCellValueFactory(new PropertyValueFactory<>("pids"));
            memoryUsageColumn.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
            blockOColumn.setCellValueFactory(new PropertyValueFactory<>("blockO"));
            blockIColumn.setCellValueFactory(new PropertyValueFactory<>("blockI"));
            removeButton.visibleProperty().setValue(false);

            Image binImg = new Image(getClass().getResource("/images/binRed.png").toExternalForm());
            ImageView binView = new ImageView(binImg);
            binView.setFitHeight(40);
            binView.setPreserveRatio(true);
            Image binHover = new Image(getClass().getResource("/images/binHover.png").toExternalForm());
            removeButton.setGraphic(binView);

            removeButton.setOnMouseEntered(event -> {
                binView.setImage(binHover);
                binView.setOpacity(0.8);
            });

            removeButton.setOnMouseExited(event -> {
                binView.setImage(binImg);
                binView.setOpacity(1);
            });


            Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> actionCellFactory = new Callback<>() {
                @Override
                public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
                    final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                        private final Button btnStart = new Button();

                        private final Tooltip startTooltip = new Tooltip("Start Container");
                        private final ImageView viewStart = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
                        private final ImageView viewStartHover = new ImageView(new Image(getClass().getResource("/images/playHover.png").toExternalForm()));
                        private final ImageView viewStartClick = new ImageView(new Image(getClass().getResource("/images/playClick.png").toExternalForm()));
                        private final Button btnStop = new Button();
                        private final Tooltip stopTooltip = new Tooltip("Stop Container");
                        Image imgStop = new Image(getClass().getResource("/images/stopRed.png").toExternalForm());
                        ImageView viewStop = new ImageView(imgStop);

                        {
                            startTooltip.setShowDelay(Duration.millis(50));
                            Tooltip.install(btnStart, startTooltip);
                            viewStart.setFitHeight(30);
                            viewStart.setFitHeight(30);
                            viewStart.setPreserveRatio(true);
                            viewStartHover.setFitHeight(30);
                            viewStartHover.setPreserveRatio(true);
                            viewStartClick.setFitHeight(20);
                            viewStartClick.setPreserveRatio(true);
                            viewStart.setPreserveRatio(true);
                            btnStart.setPrefSize(30, 30);
                            viewStart.setOpacity(0.8);
                            btnStart.setGraphic(viewStart);
                            btnStart.setOnAction((ActionEvent event) -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                try {
                                    startContainer(instance);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            stopTooltip.setShowDelay(Duration.millis(50));
                            Tooltip.install(btnStop, stopTooltip);

                            viewStop.setFitHeight(30);
                            viewStop.setPreserveRatio(true);
                            btnStop.setGraphic(viewStop);
                            viewStop.setFitHeight(30);
                            viewStop.setFitWidth(30);
                            viewStop.setPreserveRatio(true);
                            btnStop.setPrefSize(30, 30);
                            viewStop.setOpacity(0.8);
                            btnStop.setGraphic(viewStop);
                            DropShadow dropShadow = new DropShadow();

                            btnStart.setEffect(dropShadow);

                            dropShadow.setRadius(5);

                            btnStop.setEffect(dropShadow);

                            btnStop.setOnAction((ActionEvent event) -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                try {
                                    stopContainer(instance);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            btnStart.setOnMouseEntered(e -> viewStart.setImage(viewStartHover.getImage()));
                            btnStart.setOnMouseExited(e -> viewStart.setImage(new Image(getClass().getResource("/images/play.png").toExternalForm())));
                            btnStart.setOnMousePressed(e -> viewStart.setImage(viewStartClick.getImage()));
                            btnStart.setOnMouseReleased(e -> viewStart.setImage(viewStartHover.getImage()));
                            btnStop.setOnMouseEntered(e -> viewStop.setImage(new Image(getClass().getResource("/images/stopHover.png").toExternalForm())));
                            btnStop.setOnMouseExited(e -> viewStop.setImage(new Image(getClass().getResource("/images/stopRed.png").toExternalForm())));
                            btnStop.setOnMousePressed(e -> viewStop.setImage(new Image(getClass().getResource("/images/stopClick.png").toExternalForm())));
                            btnStop.setOnMouseReleased(e -> viewStop.setImage(new Image(getClass().getResource("/images/stopHover.png").toExternalForm())));

                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                if ("running".equals(instance.getStatus())) {
                                    setGraphic(btnStop);
                                } else {
                                    setGraphic(btnStart);
                                }
                            }
                        }
                    };
                    return cell;
                }
            };

            actionButtonColumn.setCellFactory(actionCellFactory);

            Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> selectCellFactory = new Callback<>() {
                @Override
                public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
                    final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                        private final CheckBox checkBox = new CheckBox();
                        {
                            checkBox.setOpacity(0.8);
                            checkBox.setMaxSize(20,20);

                            checkBox.setOnAction(event -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                instance.setSelect(checkBox.isSelected());
                                checkboxStates.put(instance.getId(), checkBox.isSelected());

                                if (checkboxStates.containsValue(true)) {
                                    removeButton.visibleProperty().setValue(true);
                                } else {
                                    removeButton.visibleProperty().setValue(false);
                                }
                            });
                        }


                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                checkBox.setSelected(instance.isSelect());
                                setGraphic(checkBox);
                            }
                        }
                    };
                    return cell;
                }
            };

            selectColumn.setCellFactory(selectCellFactory);

            instancesTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!instancesTableView.getSelectionModel().isEmpty())) {
                    InstanceScene selectedInstance = (InstanceScene) instancesTableView.getSelectionModel().getSelectedItem();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/individualContainerScene.fxml"));
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    IndividualContainerController individualContainerController = loader.getController();
                    individualContainerController.onInstanceDoubleClick(selectedInstance);
                    stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                    stage.getScene().setRoot(root);
                    stage.show();
                }
            });

            instancesTableView.setPlaceholder(new Label("No containers available."));

            instancesTableView.setRowFactory(tv -> {
                TableRow<InstanceScene> row = new TableRow<>();
                row.setOnMouseEntered(event -> row.setCursor(Cursor.HAND));
                row.setOnMouseExited(event -> row.setCursor(Cursor.DEFAULT));
                return row;
            });

            refreshInstances();

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> refreshInstances()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            runningInstancesCheckbox.setOnAction(event -> {
                refreshInstances();
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeSelectedContainers() {
        try {
            Map<String, Boolean> checkboxStates = getCheckboxStates();

            for (Map.Entry<String, Boolean> entry : checkboxStates.entrySet()) {
                String id = entry.getKey();
                boolean isSelected = entry.getValue();

                if (isSelected) {
                    HttpRequest request1 = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/api/containers/" + id + "/stop"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();

                    client.send(request1, HttpResponse.BodyHandlers.ofString());

                    HttpRequest request2 = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/api/containers/" + id + "/delete"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();

                    client.send(request2, HttpResponse.BodyHandlers.ofString());

                    checkboxStates.put(id, false);
                }
            }
            updateCheckboxStates(checkboxStates);

            if (checkboxStates.containsValue(true)) {
                removeButton.visibleProperty().setValue(true);
            } else {
                removeButton.visibleProperty().setValue(false);
            }

            refreshInstances();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateCheckboxStates(Map<String, Boolean> newStates) {
        checkboxStates.clear();
        checkboxStates.putAll(newStates);
    }

    private Map<String, Boolean> getCheckboxStates() {
        return checkboxStates;
    }

    void startContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
        if(instance.getStatus().equals("paused")){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/unpause"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showNotification("Container Event", "Container " + instance.getName() + " has unpaused.");
            }
            refreshInstances();
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/start"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showNotification("Container Event", "Container " + instance.getName() + " has started.");
            }
            refreshInstances();
        }
    }

    private void stopContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + instance.getName() + " has stopped.");
        }
        refreshInstances();
    }

    public void refreshInstances(){
        try {
            List<InstanceScene> instances = getAllInstances();
            Integer maxMetricId = getMaxMetricId();
            
            instancesTableView.getItems().clear();
            
            int totalContainers = instances.size();
            int runningContainers = 0;
            int stoppedContainers = 0;

            for(InstanceScene instance : instances) {
                int running = instance.getStatus().equals("running") ? runningContainers++ : stoppedContainers++;
                if (instance.getName().contains(searchField.getText())) {
                    if (runningInstancesCheckbox.isSelected()) {
                        if (instance.getStatus().equals("running")) {
                            instancesTableView.getItems().add(instance);
                        }
                    } else {
                        instancesTableView.getItems().add(instance);
                    }
                }
            }
            if(!selectedDateTime){
                totalContainersText.setText(String.valueOf(totalContainers));
                runningContainersText.setText(String.valueOf(runningContainers));
                stoppedContainersText.setText(String.valueOf(stoppedContainers));
                if(maxMetricId > 50){
                    metricsLabel.setText("50+");
                } else {
                    metricsLabel.setText(String.valueOf(maxMetricId));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getMaxMetricId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/lastMetricId"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        if (responseBody.isEmpty()) {
            return 1;
        } else {
            return Integer.parseInt(responseBody);
        }
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
            String memoryUsage = memoryUsageL +"MB";
            Long pidsL = jsonObject.getLong("pids");
            String pids = String.valueOf(pidsL);
            Double cpuUsageD = jsonObject.getDouble("cpuUsage") * 100;
            double roundedCpu = Math.round(cpuUsageD * 100.0) / 100.0;
            String cpuUsage = roundedCpu + "%";
            Double blockID = jsonObject.getDouble("blockI");
            double roundedI = Math.round(blockID * 10.0) / 10.0;
            String blockI = roundedI + "MB";
            Double blockOD = jsonObject.getDouble("blockO");
            double rounded0 = Math.round(blockOD * 10.0) / 10.0;
            String blockO = rounded0 + "MB";
            String volumes = jsonObject.getString("volumes");
            String subnet = jsonObject.getString("subnet");
            String gateway = jsonObject.getString("gateway");
            Integer prefixLen = jsonObject.getInt("prefixLen");
            if (status.equals("running")) {
                instances.add(new InstanceScene(id, name, image
                        ,status, memoryUsage, pids, cpuUsage , blockI
                        , blockO, volumes, subnet, gateway, prefixLen,
                        getCheckboxStateById(id) ));
            } else {
                instances.add(new InstanceScene(id, name, image ,status, "N/A", "N/A", "N/A" , "N/A", "N/A", volumes, subnet, gateway, prefixLen, getCheckboxStateById(id) ));
            }
        }
        return instances;
    }

    private boolean getCheckboxStateById(String id) {
        return checkboxStates.getOrDefault(id, false);
    }

    public void showDataThen(ActionEvent e) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            selectedDateTime = true;
            Date chosenDate = dateFormat.parse(datetimeTextField.getText());
            String chosenDateString = URLEncoder.encode(dateFormat.format(chosenDate), "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/metrics?chosenDate=" + chosenDateString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            String metricsDone = responseBody.split(",")[0].replaceAll("[^0-9]", "");
            String runningInstances = responseBody.split(",")[1].replaceAll("[^0-9]", "");
            String totalInstances = responseBody.split(",")[2].replaceAll("[^0-9]", "");
            String stoppedInstances = responseBody.split(",")[3].replaceAll("[^0-9]", "");
            metricsLabel.setText(metricsDone);
            runningContainersText.setText(runningInstances);
            totalContainersText.setText(totalInstances);
            stoppedContainersText.setText(stoppedInstances);
        } catch (ParseException pe) {
            System.out.println("Error parsing date: " + pe.getMessage());
        } catch (Exception ex) {
            System.out.println("Error occurred: " + ex.getMessage());
        }
    }

    public void clearInfo(){
        selectedDateTime = false;
        refreshInstances();
    }

    public void handleUploadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("YAML files (*.yaml)", "*.yaml");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String dockerComposeFilePath = file.getAbsolutePath();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/composeScene.fxml"));
                Parent root = loader.load();

                ComposeController controller = loader.getController();
                controller.setYamlFilePath(dockerComposeFilePath);

                stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the hover effect for the sidebar images.
     * This method applies a hover effect to the sidebar buttons.
     * The `setHoverEffect` method takes a button and two image paths as parameters:
     * the path to the original image and the path to the image to be displayed when the button is hovered over.
     */
    private void hoveredSideBarImages() {
        setHoverEffect(imagesButton, "/images/imageGrey.png", "/images/image.png");
        setHoverEffect(volumesButton, "/images/volumesGrey.png", "/images/volumes.png");
        setHoverEffect(kubernetesButton, "/images/kubernetesGrey.png", "/images/kubernetes.png");
        setHoverEffect(graphicsButton, "/images/graphicsGrey.png", "/images/graphics.png");
    }

    /**
     * Sets the hover effect for a button.
     * This method applies a hover effect to our 4 buttons in the sidebar.
     * When the mouse pointer hovers over the button,
     * the image of the button changes to a different image to indicate that the button is being hovered over.
     * When the mouse pointer moves away from the button,
     * the image of the button changes back to the original image.
     *
     * @param button The button to which the hover effect is to be applied.
     * @param originalImagePath The path to the original image of the button.
     * @param hoveredImagePath The path to the image to be displayed when the button is hovered over.
     */
    private void setHoverEffect(Button button, String originalImagePath, String hoveredImagePath) {
        // Load the original image and the hovered image.
        Image originalImage = new Image(getClass().getResourceAsStream(originalImagePath));
        Image hoveredImage = new Image(getClass().getResourceAsStream(hoveredImagePath));

        // Set the original image as the button's graphic.
        ((ImageView) button.getGraphic()).setImage(originalImage);

        // Set the hover effect: when the mouse enters the button, change the image and add the hover style class.
        button.setOnMouseEntered(event -> {
            button.getStyleClass().add("button-hovered");
            ((ImageView) button.getGraphic()).setImage(hoveredImage);
        });

        // Remove the hover effect: when the mouse exits the button, change the image back to the original and remove the hover style class.
        button.setOnMouseExited(event -> {
            button.getStyleClass().remove("button-hovered");
            ((ImageView) button.getGraphic()).setImage(originalImage);
        });
    }

    /**
     * Displays a notification with the given title and content.
     * It helps us keep user informed about events that occur in containers.
     * After 3 seconds, the Popup is automatically hidden.
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

            // Create a VBox to hold the title and content Labels and style it.
            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #EC625F; -fx-padding: 10px; -fx-border-color: #525252; -fx-border-width: 1px;");

            // Add the VBox to the Popup.
            notification.getContent().add(box);

            // Calculate the position of the Popup on the screen.
            Point2D point = notificationBox.localToScreen(notificationBox.getWidth() - box.getWidth(), notificationBox.getHeight() - box.getHeight());

            // Show the Popup on the screen at the calculated position.
            notification.show(notificationBox.getScene().getWindow(), point.getX(), point.getY());

            // Create a Timeline that will hide the Popup after 3 seconds.
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timeline.play();
        });
    }

    /**
     * Changes the current scene to a new scene.
     * This method loads the FXML file for the new scene,
     * sets it as the root of the current stage,
     * and displays the new scene. It is used to navigate between different scenes in the application.
     *
     * @param actionEvent The event that triggered the scene change.
     * @param fxmlFile The name of the FXML file for the new scene.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        // Load the FXML file for the new scene.
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));

        // Get the current stage.
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        // Set the new scene as the root of the stage and display it.
        stage.getScene().setRoot(root);
        stage.show();
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
        // Change the scene to the Volumes scene.
        changeScene(actionEvent, "kubernetesScene.fxml");
    }
}

