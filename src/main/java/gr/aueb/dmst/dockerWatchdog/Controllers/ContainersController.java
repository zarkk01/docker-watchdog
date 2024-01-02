package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import javafx.scene.control.Button;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;
import okhttp3.*;
import org.yaml.snakeyaml.Yaml;


public class ContainersController implements Initializable {
    @FXML
    public TableView instancesTableView;
    @FXML
    public TableColumn<InstanceScene, String> idColumn;
    @FXML
    public TableColumn<InstanceScene, String> nameColumn;
    @FXML
    public TableColumn<InstanceScene, String> imageColumn;
    @FXML
    public TableColumn<InstanceScene, String> statusColumn;
    @FXML
    public TableColumn<InstanceScene, String> cpuUsageColumn;
    @FXML
    public TableColumn<InstanceScene, String> pidsColumn;
    @FXML
    public TableColumn<InstanceScene, String> memoryUsageColumn;
    @FXML
    public TableColumn<InstanceScene, String> blockOColumn;
    @FXML
    public TableColumn<InstanceScene, String> blockIColumn;
    @FXML
    private TableColumn<InstanceScene, Void> actionButtonColumn;
    @FXML
    private TableColumn<InstanceScene, Void> selectColumn;

    @FXML
    private VBox notificationBox;

    @FXML
    private TextField searchField;

    @FXML
    public TextField datetimeTextField;
    @FXML
    public Button datetimeButton;
    @FXML
    public Label metricsLabel;
    @FXML
    public Button removeButton;
    @FXML
    public CheckBox runningInstancesCheckbox;

    @FXML
    public Label totalContainersText;
    @FXML
    public Label runningContainersText;
    @FXML
    public Label stoppedContainersText;

    @FXML
    public Button uploadButton;

    private Stage stage;
    private Parent root;

    private Boolean selectedDateTime = false;

    private Map<String, Boolean> checkboxStates = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
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

            Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> actionCellFactory = new Callback<>() {
                @Override
                public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
                    final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                        private final Button btnStart = new Button();
                        private final Button btnStop = new Button();
                        Image imgStart = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        Image imgStop = new Image(getClass().getResource("/images/stop.png").toExternalForm());
                        ImageView viewStart = new ImageView(imgStart);
                        ImageView viewStop = new ImageView(imgStop);

                        {
                            viewStart.setFitHeight(20);
                            viewStart.setPreserveRatio(true);
                            btnStart.setGraphic(viewStart);
                            btnStart.setOnAction((ActionEvent event) -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                try {
                                    startContainer(instance);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            viewStop.setFitHeight(20);
                            viewStop.setPreserveRatio(true);
                            btnStop.setGraphic(viewStop);
                            btnStop.setOnAction((ActionEvent event) -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                try {
                                    stopContainer(instance);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
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
                                if ("running".equals(instance.getStatus())) {
                                    // If the container is running, show only the stop button
                                    setGraphic(btnStop);
                                } else {
                                    // If the container is not running, show only the start button
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
                            checkBox.setOnAction(event -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                // Handle checkbox action, e.g., update the model
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

// Assuming your "select" column is named selectColumn
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

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), event -> refreshInstances()));
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

                    HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());

                    HttpRequest request2 = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/api/containers/" + id + "/delete"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();

                    client.send(request2, HttpResponse.BodyHandlers.ofString());

                    checkboxStates.put(id, false);  // Reset the checkbox state after deletion
                }
            }

            // Update the HashMap with the new states (in case some containers were deleted)
            updateCheckboxStates(checkboxStates);

            if (checkboxStates.containsValue(true)) {
                removeButton.visibleProperty().setValue(true);
            } else {
                removeButton.visibleProperty().setValue(false);
            }


            // Refresh the instances table
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

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
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

    private void startContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
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
        return Integer.parseInt(response.body());
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
            String volumes = jsonObject.getString("volumes");
            String subnet = jsonObject.getString("subnet");
            String gateway = jsonObject.getString("gateway");
            Integer prefixLen = jsonObject.getInt("prefixLen");
            instances.add(new InstanceScene(id, name, image ,status, memoryUsage, pids, cpuUsage * 100, blockI, blockO, volumes, subnet, gateway, prefixLen, getCheckboxStateById(id) ));
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

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), evt -> notification.hide()));
            timeline.play();
        });
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
}
