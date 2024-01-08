package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
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
    private TextField pullImageTextField;

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
    @FXML
    private TableColumn<ImageScene,Void> removeImageColumn;

    @FXML
    private TextField searchField;

    @FXML
    public Button containersButton;
    @FXML
    public Button imagesButton;
    @FXML
    public Button graphicsButton;
    @FXML
    public Button kubernetesButton;
    @FXML
    public Button volumesButton;

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
            hoveredSideBarImages();
            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> startCellFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();

                        private final Tooltip createTooltip = new Tooltip("Create a Container");

                        private final ImageView viewStart = new ImageView(new Image(getClass().getResource("/images/create.png").toExternalForm()));
                        private final ImageView viewStartHover = new ImageView(new Image(getClass().getResource("/images/createHover.png").toExternalForm()));
                        private final ImageView viewStartClick = new ImageView(new Image(getClass().getResource("/images/playClick.png").toExternalForm()));
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            createTooltip.setShowDelay(Duration.millis(50));
                            Tooltip.install(btn, createTooltip);
                            viewStart.setFitHeight(30);
                            viewStart.setFitHeight(30);
                            viewStart.setPreserveRatio(true);
                            viewStartHover.setFitHeight(30);
                            viewStartHover.setPreserveRatio(true);
                            viewStartClick.setFitHeight(20);
                            viewStartClick.setPreserveRatio(true);
                            viewStart.setPreserveRatio(true);
                            viewStart.setOpacity(0.8);
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(viewStart);
                            btn.setOnAction((ActionEvent event) -> {
                                // Handle button click to create a container
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    createContainer(image);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            btn.setOnMouseEntered(e -> viewStart.setImage(viewStartHover.getImage()));
                            btn.setOnMouseExited(e -> viewStart.setImage(new Image(getClass().getResource("/images/create.png").toExternalForm())));
                            btn.setOnMousePressed(e -> viewStart.setImage(viewStartClick.getImage()));
                            btn.setOnMouseReleased(e -> viewStart.setImage(viewStartHover.getImage()));


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
                        private final Button btn = new Button();
                        private final Tooltip startTooltip = new Tooltip("Start All Containers");
                        private final ImageView viewStart = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
                        private final ImageView viewStartHover = new ImageView(new Image(getClass().getResource("/images/playHover.png").toExternalForm()));
                        private final ImageView viewStartClick = new ImageView(new Image(getClass().getResource("/images/playClick.png").toExternalForm()));
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            startTooltip.setShowDelay(Duration.millis(50));
                            Tooltip.install(btn, startTooltip);

                            viewStart.setFitHeight(30);
                            viewStart.setFitHeight(30);
                            viewStart.setPreserveRatio(true);
                            viewStartHover.setFitHeight(30);
                            viewStartHover.setPreserveRatio(true);
                            viewStartClick.setFitHeight(20);
                            viewStartClick.setPreserveRatio(true);
                            viewStart.setPreserveRatio(true);
                            viewStart.setOpacity(0.8);
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(viewStart);
                            btn.setOnAction((ActionEvent event) -> {
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    startAllContainers(image.getName());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            btn.setOnMouseEntered(e -> viewStart.setImage(viewStartHover.getImage()));
                            btn.setOnMouseExited(e -> viewStart.setImage(new Image(getClass().getResource("/images/play.png").toExternalForm())));
                            btn.setOnMousePressed(e -> viewStart.setImage(viewStartClick.getImage()));
                            btn.setOnMouseReleased(e -> viewStart.setImage(viewStartHover.getImage()));

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
                        private final Button btn = new Button();
                        private final Tooltip stopTooltip = new Tooltip("Stop All Containers");
                        Image img = new Image(getClass().getResource("/images/stop.png").toExternalForm());
                        ImageView view = new ImageView(img);
                        {
                            stopTooltip.setShowDelay(Duration.millis(50));
                            Tooltip.install(btn, stopTooltip);
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
                            btn.setOnMouseEntered(e -> view.setImage(new Image(getClass().getResource("/images/stopHover.png").toExternalForm())));
                            btn.setOnMouseExited(e -> view.setImage(new Image(getClass().getResource("/images/stop.png").toExternalForm())));
                            btn.setOnMousePressed(e -> view.setImage(new Image(getClass().getResource("/images/stopClick.png").toExternalForm())));
                            btn.setOnMouseReleased(e -> view.setImage(new Image(getClass().getResource("/images/stopHover.png").toExternalForm())));
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

            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> removeImageFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();
                        {
                            btn.setOnAction((ActionEvent event) -> {
                                ImageScene image = getTableView().getItems().get(getIndex());
                                if(image.getStatus().equals("In use")) {
                                    showNotification("Error", "Image is in use, delete all containers first");
                                    return;
                                }
                                try {
                                    removeImage(image.getName());
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
            removeImageColumn.setCellFactory(removeImageFactory);

            refreshImages();

            imagesTableView.setPlaceholder(new Label("No images available."));

            usedImagesCheckbox.setOnAction(event -> {
                refreshImages();
            });

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), evt -> refreshImages()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void hoveredSideBarImages() {
        Image originalContainers = new Image(getClass().getResourceAsStream("/images/containerGrey.png"));

        // Load your hovered image
        Image hoveredContainers = new Image(getClass().getResourceAsStream("/images/container.png"));

        // Set the original image to the ImageView
        ((ImageView) containersButton.getGraphic()).setImage(originalContainers);

        // Attach event handlers
        containersButton.setOnMouseEntered(event -> {
            containersButton.getStyleClass().add("button-hovered");
            ((ImageView) containersButton.getGraphic()).setImage(hoveredContainers);
        });

        containersButton.setOnMouseExited(event -> {
            containersButton.getStyleClass().remove("button-hovered");
            ((ImageView) containersButton.getGraphic()).setImage(originalContainers);
        });

        Image originalVolume = new Image(getClass().getResourceAsStream("/images/volumesGrey.png"));

        // Load your hovered image
        Image hoveredVolume = new Image(getClass().getResourceAsStream("/images/volumes.png"));

        // Set the original image to the ImageView
        ((ImageView) volumesButton.getGraphic()).setImage(originalVolume);

        // Attach event handlers
        volumesButton.setOnMouseEntered(event -> {
            volumesButton.getStyleClass().add("button-hovered");
            ((ImageView) volumesButton.getGraphic()).setImage(hoveredVolume);
        });

        volumesButton.setOnMouseExited(event -> {
            volumesButton.getStyleClass().remove("button-hovered");
            ((ImageView) volumesButton.getGraphic()).setImage(originalVolume);
        });


        Image originalGraphics = new Image(getClass().getResourceAsStream("/images/graphicsGrey.png"));

        // Load your hovered image
        Image hoveredGraphics = new Image(getClass().getResourceAsStream("/images/graphics.png"));

        // Set the original image to the ImageView
        ((ImageView) graphicsButton.getGraphic()).setImage(originalGraphics);

        // Attach event handlers
        graphicsButton.setOnMouseEntered(event -> {
            graphicsButton.getStyleClass().add("button-hovered");
            ((ImageView) graphicsButton.getGraphic()).setImage(hoveredGraphics);
        });

        graphicsButton.setOnMouseExited(event -> {
            graphicsButton.getStyleClass().remove("button-hovered");
            ((ImageView) graphicsButton.getGraphic()).setImage(originalGraphics);
        });

        Image originalKubernetes = new Image(getClass().getResourceAsStream("/images/kubernetesGrey.png"));

        // Load your hovered image
        Image hoveredKubernetes = new Image(getClass().getResourceAsStream("/images/kubernetes.png"));

        // Set the original image to the ImageView
        ((ImageView) kubernetesButton.getGraphic()).setImage(originalKubernetes);

        // Attach event handlers
        kubernetesButton.setOnMouseEntered(event -> {
            kubernetesButton.getStyleClass().add("button-hovered");
            ((ImageView) kubernetesButton.getGraphic()).setImage(hoveredKubernetes);
        });

        kubernetesButton.setOnMouseExited(event -> {
            kubernetesButton.getStyleClass().remove("button-hovered");
            ((ImageView) kubernetesButton.getGraphic()).setImage(originalKubernetes);
        });
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
                if (image.getName().contains(searchField.getText())) {
                    if (usedImagesCheckbox.isSelected()) {
                        if (image.getStatus().equals("In use")) {
                            imagesTableView.getItems().add(image);
                        }
                    } else {
                        imagesTableView.getItems().add(image);
                    }
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
            box.setStyle("-fx-background-color: #EC625F; -fx-padding: 10px; -fx-border-color: #525252; -fx-border-width: 1px;");

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

    public void pullGivenImage() throws Exception {
        String imageName = pullImageTextField.getText();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images/pull/" + imageName))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        refreshImages();
        if (response.statusCode() == 200) {
            showNotification("Success", "Image pulled successfully");
        } else {
            showNotification("Error", "Image pull failed");
        }
    }

    public void removeImage(String imageName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images/remove/" + imageName))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        refreshImages();
        if (response.statusCode() == 200) {
            showNotification("Success", "Image removed successfully");
        } else {
            showNotification("Error", "Image removal failed");
        }
    }
}
