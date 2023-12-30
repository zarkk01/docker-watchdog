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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import javafx.scene.control.Button;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

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
    private TableColumn<InstanceScene, Void> startButtonColumn;
    @FXML
    private TableColumn<InstanceScene, Void> stopButtonColumn;

    @FXML
    private VBox notificationBox;

    @FXML
    public TextField datetimeTextField;
    @FXML
    public Button datetimeButton;
    @FXML
    public Label metricsDoneLabel;
    @FXML
    public Label runningInstancesLabel;

    @FXML
    public CheckBox runningInstancesCheckbox;

    private Stage stage;
    private Parent root;

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

            Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> startCellFactory = new Callback<>() {
                @Override
                public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
                    final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);
                            btn.setOnAction((ActionEvent event) -> {
                                InstanceScene instance = getTableView().getItems().get(getIndex());
                                try {
                                    startContainer(instance);
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
                                setGraphic(btn);
                            }
                        }
                    };
                    return cell;
                }
            };

            startButtonColumn.setCellFactory(startCellFactory);

            Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> stopCellFactory = new Callback<>() {
                @Override
                public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
                    final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();

                        //Creating a graphic (image)
                        Image img = new Image(getClass().getResource("/images/stop.png").toExternalForm());
                        ImageView view = new ImageView(img);



                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);
                            btn.setOnAction((ActionEvent event) -> {
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
                                setGraphic(btn);
                            }
                        }
                    };
                    return cell;
                }
            };

            stopButtonColumn.setCellFactory(stopCellFactory);

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

    private void startContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + instance.getName() + " has started.");
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
    }

    public void refreshInstances(){
        try {
            List<InstanceScene> instances = getAllInstances();
            instancesTableView.getItems().clear();
            for(InstanceScene instance : instances) {
                if (runningInstancesCheckbox.isSelected()) {
                    if (instance.getStatus().equals("running")) {
                        instancesTableView.getItems().add(instance);
                    }
                } else {
                    instancesTableView.getItems().add(instance);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            Long memoryUsage = jsonObject.getLong("memoryUsage");
            Long pids = jsonObject.getLong("pids");
            Double cpuUsage = jsonObject.getDouble("cpuUsage");
            Double blockI = jsonObject.getDouble("blockI");
            Double blockO = jsonObject.getDouble("blockO");
            instances.add(new InstanceScene(id, name, image ,status, memoryUsage, pids, cpuUsage, blockI, blockO));
        }
        return instances;
    }

    public void showDataThen(ActionEvent e) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
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
            metricsDoneLabel.setText("How many metrics were be done then: " + metricsDone);
            runningInstancesLabel.setText("How many containers were running then: " + runningInstances);
        } catch (ParseException pe) {
            System.out.println("Error parsing date: " + pe.getMessage());
        } catch (Exception ex) {
            System.out.println("Error occurred: " + ex.getMessage());
        }
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

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timeline.play();
        });
    }
}
