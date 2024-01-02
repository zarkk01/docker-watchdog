package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class ImagesController implements Initializable {

    @FXML
    private TableView<ImageScene> imagesTableView;

    @FXML
    private CheckBox usedImagesCheckbox;

    @FXML
    private VBox notificationBox;

    @FXML
    private TableColumn<ImageScene,String> idColumn;
    @FXML
    private TableColumn<ImageScene,String> nameColumn;
    @FXML
    private TableColumn<ImageScene,String> statusColumn;
    @FXML
    private TableColumn<ImageScene,Long> sizeColumn;
    @FXML
    private TableColumn<ImageScene,Void> createContainerCollumn;
    @FXML
    private TableColumn<ImageScene,Void> startAllCollumn;
    @FXML
    private TableColumn<ImageScene,Void> stopAllCollumn;

    private Stage stage;
    private Parent root;

    //Initialize the controller
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            //Set up table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> startCellFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);
                            btn.setOnAction((ActionEvent event) -> {
                                // Handle button click to create a container
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    createContainer(image);
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
            createContainerCollumn.setCellFactory(startCellFactory);
            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> startAllCellFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button("Start All");
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);
                            btn.setOnAction((ActionEvent event) -> {
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    startAllContainers(image.getName());
                                } catch (Exception e) {
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
            startAllCollumn.setCellFactory(startAllCellFactory);

            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> stopAllCellFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button("Stop All");
                        Image img = new Image(getClass().getResource("/images/stop.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);

                            btn.setOnAction((ActionEvent event) -> {
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    stopAllContainers(image.getName());
                                } catch (Exception e) {
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
            stopAllCollumn.setCellFactory(stopAllCellFactory);

            refreshImages();

            imagesTableView.setPlaceholder(new Label("No images available."));
            usedImagesCheckbox.setOnAction(event -> {
                refreshImages();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Change the scene to the specified FXML file
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    // Navigate to the Containers scene
    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    // Navigate to the Graphics scene
    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    // Navigate to the Volumes scene
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

    // Navigate to the Kubernetes scene
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    public void refreshImages() {
        try {
            List<ImageScene> images = getAllImages();
            imagesTableView.getItems().clear();
            for(ImageScene image : images) {
                if (usedImagesCheckbox.isSelected()) {
                    if (image.getStatus().equals("In use")) {
                        imagesTableView.getItems().add(image);
                    }
                } else {
                    imagesTableView.getItems().add(image);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ImageScene> getAllImages() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());
        List<ImageScene> images = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String status = jsonObject.getString("status");
            Long size = jsonObject.getLong("size");
            images.add(new ImageScene(id, name ,size, status));
        }
        return images;
    }

    public void createContainer(ImageScene image) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images/create/" + image.getName()))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Success", "Container created successfully");
        } else {
            showNotification("Error", "Container creation failed");
        }

        image.setStatus("In use");
        refreshImages();
    }

    // Display a notification to the user
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

    public void startAllContainers(String imageName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/startAll/" + imageName))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Success", "All containers started successfully");
        } else {
            showNotification("Error", "Container start failed");
        }

        refreshImages();
    }

    public void stopAllContainers(String imageName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/stopAll/" + imageName))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Success", "All containers stopped successfully");
        } else {
            showNotification("Error", "Container stop failed");
        }

        refreshImages();
    }
}
