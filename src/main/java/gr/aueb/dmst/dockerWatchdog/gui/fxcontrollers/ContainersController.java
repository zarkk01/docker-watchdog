package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.text.ParseException;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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
import javafx.scene.layout.Pane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import gr.aueb.dmst.dockerWatchdog.gui.models.InstanceScene;
import static gr.aueb.dmst.dockerWatchdog.gui.GuiApplication.client;


/**
 * Controller for the Containers scene.
 * It provides methods for handling user interactions with the Docker containers,
 * such as starting, stopping, and removing containers. User can additionally search for
 * a specific container by name and select multiple containers to remove.
 * It also provides methods for changing scenes, showing notifications,
 * and updating the instances table view. It also navigates user to the
 * individual container scene when a row is clicked.
 * The class uses the WATCHDOG REST API to communicate with
 * the backend and send requests for information and actions at database.
 */
public class ContainersController implements Initializable {

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(ContainersController.class);
    private Stage stage;
    private Parent root;

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
    private Text containersHead;
    @FXML
    private VBox sideBar;
    @FXML
    private Pane infoDownPane;
    @FXML
    private Pane runningPane;
    @FXML
    private Pane searchPane;

    @FXML
    private VBox notificationBox;
    private static VBox notificationBoxStatic;

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
    private Button userButton;

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

    @FXML
    private ImageView loadingImageView;
    private static ImageView loadingImageViewStatic;

    private Boolean selectedDateTime = false;

    private Map<String, Boolean> checkboxStates = new HashMap<>();

    private Timeline timeline;

    /**
     * Initializes the ContainersController.
     * This method is called after the ContainersController is constructed.
     * It sets up shadows, sidebar images, table columns, remove button, instances table view, and refreshes the instances.
     * It also creates a Timeline that refreshes the instances every 2 seconds and starts it.
     * If the runningInstancesCheckbox is selected, it refreshes the instances.
     * If an error occurs during the initialization, it throws a RuntimeException.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     * @throws RuntimeException If an error occurs during the initialization.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up the shadows for the components.
            setUpShadows();

            // Set up the sidebar images.
            hoveredSideBarImages();

            // Set up a tooltip for the watchdogImage.
            setUpWoofTooltip();

            // Set up the table columns.
            setUpTableColumns();

            // Set up the remove button.
            setUpRemoveButton();

            // Set up the instances table view.
            setUpInstancesTableView();

            // Refresh the instances.
            refreshInstances();

            // Create a Timeline that refreshes the instances every 2 seconds and start it.
            timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> refreshInstances()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            // If the runningInstancesCheckbox got selected, refresh the instances.
            runningInstancesCheckbox.setOnAction(event -> {
                refreshInstances();
            });

            // Set the notificationBoxStatic to the notificationBox.
            // It is static so to be able to be called by ApiService where the actions are performed.
            notificationBoxStatic = this.notificationBox;

            // Set the loadingImageViewStatic to the loadingImageView with the loading gif.
            // It is static so to be able to be manipulated by ApiService.
            loadingImageView.setImage(new Image(getClass().getResource("/images/loading.gif").toExternalForm()));
            loadingImageViewStatic = this.loadingImageView;

            // Trim the username if it is longer than 7 characters so to fit in the button
            String rightLengthName = UserController.name != null && UserController.name.length() > 7 ? UserController.name.substring(0, 7) : UserController.name;
            // Set the text of the userButton to "Log in" if the user is not logged in, and to username if the user is logged in.
            userButton.setText(UserController.token == null ? "Log in" : rightLengthName);
        } catch (Exception e) {
            // If an error occurs during the initialization, throw a RuntimeException.
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the drop shadow effect for various components in the scene.
     * This method creates a new DropShadow effect and applies it to the infoDownPane, runningPane, searchPane,
     * instancesTableView, topBar, sideBar and containersHead.
     * The radius of the shadow is set to 8 and the color is set to a semi-transparent black.
     */
    private void setUpShadows() {
        // Set up drop shadow effect for the components.
        DropShadow shadow = new DropShadow();
        shadow.setRadius(7.5);
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        infoDownPane.setEffect(shadow);
        runningPane.setEffect(shadow);
        searchPane.setEffect(shadow);
        instancesTableView.setEffect(shadow);
        sideBar.setEffect(shadow);
        containersHead.setEffect(shadow);
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
     * Sets up a tooltip for the watchdogImage.
     * This method creates a new Tooltip with the text "Woof!" and sets its show delay to 20 milliseconds.
     * The tooltip is then installed on the watchdogImage.
     */
    private void setUpWoofTooltip() {
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage,woof);
    }

    /**
     * Sets up the instances table view.
     * This method configures the behavior of the instances table view.
     * It sets the mouse click event for the table view, which loads the IndividualContainer scene when a row is double-clicked.
     * It also sets the placeholder for the table view, which is displayed when the table view is empty.
     * Finally, it sets the row factory for the table view, which changes the cursor to a hand when it hovers over a row.
     */
    private void setUpInstancesTableView() {
        // Set the mouse click event for the table view.
        // When a row is double-clicked, load the IndividualContainer scene for the selected instance.
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
                // Pass the selected instance to the IndividualContainerController and the scene we are coming from.
                individualContainerController.onInstanceDoubleClick(selectedInstance,"containersScene.fxml");
                stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.show();
            }
        });

        // Set the placeholder for the table view.
        // This placeholder is displayed when the table view is empty.
        instancesTableView.setPlaceholder(new Label("No containers available."));

        // Set the row factory for the table view.
        // This changes the cursor to a hand when it hovers over a row.
        instancesTableView.setRowFactory(tv -> {
            TableRow<InstanceScene> row = new TableRow<>();
            row.setOnMouseEntered(event -> row.setCursor(Cursor.HAND));
            row.setOnMouseExited(event -> row.setCursor(Cursor.DEFAULT));
            return row;
        });
    }

    /**
     * Sets up the remove button.
     * This method configures the behavior of the remove button.
     * It sets the image for the button and configures the hover effect.
     * When the mouse pointer hovers over the button, the image of the button changes to a different image.
     * When the mouse pointer moves away from the button, the image of the button changes back to the original image.
     */
    private void setUpRemoveButton() {
        // Load the image for the remove button.
        Image binImg = new Image(getClass().getResource("/images/binRed.png").toExternalForm());
        ImageView binView = new ImageView(binImg);
        binView.setFitHeight(40);
        binView.setPreserveRatio(true);

        // Load the image to be displayed when the button is hovered over.
        Image binHover = new Image(getClass().getResource("/images/binHover.png").toExternalForm());

        // Set the image for the remove button.
        removeButton.setGraphic(binView);

        // Set the hover effect for the remove button.
        removeButton.setOnMouseEntered(event -> {
            binView.setImage(binHover);
            binView.setOpacity(0.8);
        });

        // Remove the hover effect when the mouse pointer moves away from the button.
        removeButton.setOnMouseExited(event -> {
            binView.setImage(binImg);
            binView.setOpacity(1);
        });
    }

    /**
     * Sets up the table columns.
     * This method configures the behavior of the table columns.
     * It sets the cell value factory for each column, which determines what value is displayed in the cells of the column.
     * The cell value factory is a Callback that extracts a property value from the InstanceScene object and displays it in the cell.
     * It also sets the cell factory for the actionButtonColumn and selectColumn, which determines how the cells of the column are created.
     * The cell factory is a Callback that creates a TableCell for each row in the column.
     * The actionButtonColumn's cells contain a Button that allows the user to start or stop the Docker container represented by the row.
     * The selectColumn's cells contain a CheckBox that allows the user to select the Docker container represented by the row.
     * Finally, it hides the remove button.
     */
    private void setUpTableColumns() {
        // Set the cell value factory for each column.
        // The cell value factory is a Callback that extracts a property value from the InstanceScene object and displays it in the cell.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        cpuUsageColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));
        pidsColumn.setCellValueFactory(new PropertyValueFactory<>("pids"));
        memoryUsageColumn.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        blockOColumn.setCellValueFactory(new PropertyValueFactory<>("blockO"));
        blockIColumn.setCellValueFactory(new PropertyValueFactory<>("blockI"));

        // Set the cell factory for the actionButtonColumn and selectColumn.
        // The cell factory is a Callback that creates a TableCell for each row in the column.
        actionButtonColumn.setCellFactory(actionCellFactory);
        selectColumn.setCellFactory(selectCellFactory);

        // Hide the remove button.
        removeButton.visibleProperty().setValue(false);
    }

    /**
     * Creates a cell factory for the action column.
     * The cell factory is a Callback that creates a TableCell for each row in the column.
     * Each TableCell contains a Button that allows the user to start or stop the Docker container represented by the row.
     * The Button's action depends on the status of the Docker container:
     * - If the container is running, the Button stops the container.
     * - If the container is not running, the Button starts the container.
     * The Button's graphic changes when the mouse enters or exits the Button, providing a hover effect.
     */
    Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> actionCellFactory = new Callback<>() {
        @Override
        public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
            final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                // Create start and stop buttons
                private final Button btnStart = new Button();
                private final Button btnStop = new Button();

                // Tooltip and images for the start button
                private final Tooltip startTooltip = new Tooltip("Start container");
                private final ImageView viewStart = new ImageView(new Image(getClass().getResource("/images/play.png").toExternalForm()));
                private final ImageView viewStartHover = new ImageView(new Image(getClass().getResource("/images/playHover.png").toExternalForm()));
                private final ImageView viewStartClick = new ImageView(new Image(getClass().getResource("/images/playClick.png").toExternalForm()));

                // Tooltip and images for the stop button
                private final Tooltip stopTooltip = new Tooltip("Stop container");
                Image imgStop = new Image(getClass().getResource("/images/stopRed.png").toExternalForm());
                ImageView viewStop = new ImageView(imgStop);

                {
                    // Setup for the start button
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
                    // Set the action for the start button
                    btnStart.setOnAction((ActionEvent event) -> {
                        InstanceScene instance = getTableView().getItems().get(getIndex());
                        try {
                            startContainer(instance);
                        } catch (IOException | InterruptedException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    // Setup for the stop button
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
                    // Set the action for the stop button
                    btnStop.setOnAction((ActionEvent event) -> {
                        InstanceScene instance = getTableView().getItems().get(getIndex());
                        try {
                            stopContainer(instance);
                        } catch (IOException | InterruptedException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    // Hover effects for the start and stop buttons
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
                        // Check the status of the instance and set the appropriate button (start or stop)
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

    /**
     * Creates a cell factory for the select column.
     * The cell factory is a Callback that creates a TableCell for each row in the column.
     * Each TableCell contains a CheckBox that allows the user to select the Docker container represented by the row.
     * The CheckBox's state changes when the user clicks on it, and the state is stored in the checkboxStates map.
     * If the CheckBox is selected, the remove button becomes visible.
     * If the CheckBox is not selected and no other CheckBox is selected, the remove button becomes invisible.
     *
     * @return A Callback that creates a TableCell for each row in the select column.
     */
    Callback<TableColumn<InstanceScene, Void>, TableCell<InstanceScene, Void>> selectCellFactory = new Callback<>() {
        @Override
        public TableCell<InstanceScene, Void> call(final TableColumn<InstanceScene, Void> param) {
            final TableCell<InstanceScene, Void> cell = new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOpacity(0.8);
                    checkBox.setMaxSize(20,20);

                    // Set the action for the CheckBox.
                    checkBox.setOnAction(event -> {
                        InstanceScene instance = getTableView().getItems().get(getIndex());
                        instance.setSelect(checkBox.isSelected());
                        checkboxStates.put(instance.getId(), checkBox.isSelected());

                        // If any CheckBox is selected, make the remove button visible.
                        // If no CheckBox is selected, make the remove button invisible.
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

    /**
     * Starts a Docker container.
     * This method sends a POST request to the WATCHDOG REST API to start a Docker container with a given ID.
     * If the container is currently paused, it sends a POST request to unpause the container instead.
     * After the container is started or unpaused, it refreshes the instances table view.
     *
     * @param instance The InstanceScene object representing the Docker container to be started.
     * @throws IOException If an I/O error occurs when sending or receiving the HTTP request.
     * @throws InterruptedException If the operation is interrupted.
     * @throws URISyntaxException If the URI of the HTTP request is not formatted correctly.
     */
    private void startContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
        // Check if the container is currently paused.
        if (instance.getStatus().equals("paused")) {
            // If the container is paused, send a POST request to the WATCHDOG REST API to unpause the container.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/unpause"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is 200 (meaning the request was successful), refresh the instances.
            if (response.statusCode() == 200) {
                refreshInstances();
            }
        } else {
            // If the container is not paused, send a POST request to the WATCHDOG REST API to start the container.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/start"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is 200 (meaning the request was successful), refresh the instances.
            if (response.statusCode() == 200) {
                refreshInstances();
            }
        }
    }

    /**
     * Stops a Docker container.
     * This method sends a POST request to the WATCHDOG REST API to stop a Docker container with a given ID.
     * After the container is stopped, it refreshes the instances table view.
     *
     * @param instance The InstanceScene object representing the Docker container to be stopped.
     * @throws IOException If an I/O error occurs when sending or receiving the HTTP request.
     * @throws InterruptedException If the operation is interrupted.
     * @throws URISyntaxException If the URI of the HTTP request is not formatted correctly.
     */
    private void stopContainer(InstanceScene instance) throws IOException, InterruptedException, URISyntaxException {
        // Create a POST request to the WATCHDOG REST API to stop the Docker container with the given ID.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + instance.getId() + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        // Send the request and get the response.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // If the response status code is 200 (meaning the request was successful), refresh the instances.
        if (response.statusCode() == 200) {
            // Also, show a notification to the user to wait a few seconds for the container to stop.
            showNotification("Be patient.." , "Container is stopping.", 2);
            refreshInstances();
        }
    }

    /**
     * Removes the selected containers.
     * This method iterates over the checkboxStates map, which holds the container ID and its selection state.
     * If a container is selected (checkbox state is true), it sends a POST request to the WATCHDOG REST API to delete the container.
     * After all selected containers are removed, it refreshes the instances table view.
     * If any container is still selected after the removal, it makes the remove button visible.
     * If no container is selected, it hides the remove button.
     */
    public void removeSelectedContainers() {
        try {
            // Get the checkbox states map.
            Map<String, Boolean> checkboxStates = getCheckboxStates();

            // Iterate over the checkbox states map.
            for (Map.Entry<String, Boolean> entry : checkboxStates.entrySet()) {
                String containerId = entry.getKey();
                boolean isSelected = entry.getValue();

                // If the container is selected, send a POST request to delete the container.
                if (isSelected) {
                    // Send a POST request to delete the container
                    HttpRequest request2 = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/delete"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    client.send(request2, HttpResponse.BodyHandlers.ofString());
                    // Set the checkbox state to false.
                    checkboxStates.put(containerId, false);
                }
            }
            // Update the checkbox states map.
            updateCheckboxStates(checkboxStates);

            // If any container is still selected after the removal, make the remove button visible.
            removeButton.visibleProperty().setValue(checkboxStates.containsValue(true));

            // Refresh the instances table view.
            refreshInstances();
        } catch (Exception e) {
            logger.error("Failed to remove selected containers.", e);
        }
    }

    /**
     * Updates the checkbox states map with new states.
     * This method clears the existing checkboxStates map and puts all entries from the newStates map into it.
     *
     * @param newStates The map containing the new checkbox states.
     */
    private void updateCheckboxStates(Map<String, Boolean> newStates) {
        checkboxStates.clear();
        checkboxStates.putAll(newStates);
    }

    /**
     * Returns the checkbox states map.
     * This map holds the container ID and its selection state.
     *
     * @return The checkbox states map.
     */
    private Map<String, Boolean> getCheckboxStates() {
        return checkboxStates;
    }

    /**
     * Returns the checkbox state of a container by its ID.
     * If the container ID does not exist in the checkboxStates map, it returns false.
     *
     * @param id The ID of the container.
     * @return The checkbox state of the container.
     */
    private boolean getCheckboxStateById(String id) {
        return checkboxStates.getOrDefault(id, false);
    }

    /**
     * Refreshes the instances table view.
     * This method retrieves all instances of the Docker containers by calling the getAllInstances method.
     * It then clears the instances table view and adds the retrieved instances to it.
     * It also updates the total, running, and stopped containers labels.
     * If the selectedDateTime is false (meaning no datetime is currently selected by the user),
     * it also updates the metrics label with the maximum metric ID.
     * If the runningInstancesCheckbox is selected, it only adds the running instances to the table view.
     * If the runningInstancesCheckbox is not selected, it adds all instances to the table view.
     * If an error occurs while refreshing the instances, it throws a RuntimeException.
     */
    public void refreshInstances(){
        try {
            // Retrieve all instances of the Docker containers.
            List<InstanceScene> instances = getAllInstances();

            // Get the maximum metric ID.
            Integer maxMetricId = getMaxMetricId();

            // Clear the instances table view.
            instancesTableView.getItems().clear();

            // Initialize the counters for the total, running, and stopped containers.
            int totalContainers = instances.size();
            int runningContainers = 0;
            int stoppedContainers = 0;

            // Iterate over the instances.
            for(InstanceScene instance : instances) {
                // If the instance is running, increment the running containers counter.
                // If the instance is not running, increment the stopped containers counter.
                int running = instance.getStatus().equals("running") ? runningContainers++ : stoppedContainers++;

                // If the instance's name contains the text in the search field, add the instance to the table view.
                // If the runningInstancesCheckbox is selected, only add the instance if it is running.
                // If the runningInstancesCheckbox is not selected, add the instance regardless of its status.
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

            // If no datetime is currently selected by the user, update the total, running, and stopped containers labels and the metrics label.
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
            logger.error("Failed to refresh instances.", e);
        }
    }

    /**
     * Retrieves all instances of the Docker cluster.
     * This method sends a GET request to the WATCHDOG REST API to retrieve all instances, and then
     * it then parses the response body to extract the details of each instance,
     * such as the ID, name, image, status, CPU usage, memory usage, block I/O, volumes, subnet, gateway, and prefix length.
     * These details are used to create a new InstanceScene object for each instance, which are then added to a list.
     * If the status of an instance is not "running", the CPU usage, memory usage, and block I/O are set to "N/A".
     * The method returns the list of InstanceScene objects.
     *
     * @return A list of InstanceScene objects representing all instances of the Docker containers.
     * @throws Exception If an error occurs when sending or receiving the HTTP request.
     */
    public List<InstanceScene> getAllInstances() throws Exception {
        // Send a GET request to the WATCHDOG REST API to retrieve all instances of the Docker cluster.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // This error indicates that we be in trouble. User should try again.
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to retrieve instances. Try run Watchdog again.");
        }

        // Parse the response body to extract the details of each instance.
        JSONArray jsonArray = new JSONArray(response.body());
        List<InstanceScene> instances = new ArrayList<>();
        // Iterate over the instances.
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

            // Create a new InstanceScene object for each instance and add it to the list.
            if (status.equals("running")) {
                instances.add(new InstanceScene(id, name, image
                        , status, memoryUsage, pids, cpuUsage, blockI
                        , blockO, volumes, subnet, gateway, prefixLen,
                        getCheckboxStateById(id)));
            } else {
                // If the status of the instance is not "running", set the CPU usage, memory usage, and block I/O to "N/A".
                instances.add(new InstanceScene(id,
                        name,
                        image,
                        status,
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A",
                        "N/A",
                        volumes,
                        subnet,
                        gateway,
                        prefixLen,
                        getCheckboxStateById(id)));
            }
        }
        // Return the list of InstanceScene objects.
        return instances;
    }

    /**
     * Retrieves the maximum metric ID.
     * This method sends a GET request to the WATCHDOG REST API to retrieve the maximum metric ID.
     * The maximum metric ID represents the highest number of changes (metrics) that have been recorded for the Docker containers.
     * If the response body is empty, it returns 1. Otherwise, it parses the response body to an integer and returns it.
     *
     * @return The maximum metric ID.
     * @throws Exception If an error occurs when sending or receiving the HTTP request.
     */
    public Integer getMaxMetricId() throws Exception {
        // Send a GET request to the WATCHDOG REST API to retrieve the maximum metric ID.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/lastMetricId"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response body to extract the maximum metric ID.
        String responseBody = response.body();
        if (responseBody.isEmpty()) {
            return 1;
        } else {
            return Integer.parseInt(responseBody);
        }
    }

    /**
     * Displays the state of the Docker cluster at a given datetime.
     * This method is called when the user inputs a datetime
     * and wants to see the state of the Docker cluster at that time.
     * It sends a GET request to the WATCHDOG REST API to retrieve
     * the metrics of the Docker cluster at the given datetime.
     * The metrics include the number of metrics done (meaning changes),
     * running instances, total instances, and stopped instances.
     * These metrics are then displayed in the corresponding labels in the user interface.
     * If the datetime is not in the correct format, it shows a notification to the user.
     */
    public void showDataFromGivenDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            // Set selectedDateTime to true to indicate that a datetime is currently selected.
            selectedDateTime = true;

            // Parse the datetime given by the user.
            Date chosenDate = dateFormat.parse(datetimeTextField.getText());

            // Encode the datetime to be used in the URL of the HTTP request.
            String chosenDateString = URLEncoder.encode(dateFormat.format(chosenDate), "UTF-8");

            // Send a GET request to the WATCHDOG REST API to retrieve the metrics at the given datetime.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/metrics?chosenDate=" + chosenDateString))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response body to extract the metrics.
            String responseBody = response.body();

            // The response body is in the format "metricsDone,runningInstances,totalInstances,stoppedInstances".
            String metricsDone = responseBody.split(",")[0].replaceAll("[^0-9]", "");
            String runningInstances = responseBody.split(",")[1].replaceAll("[^0-9]", "");
            String totalInstances = responseBody.split(",")[2].replaceAll("[^0-9]", "");
            String stoppedInstances = responseBody.split(",")[3].replaceAll("[^0-9]", "");

            // Display the metrics in the corresponding labels in the user interface.
            metricsLabel.setText(metricsDone);
            runningContainersText.setText(runningInstances);
            totalContainersText.setText(totalInstances);
            stoppedContainersText.setText(stoppedInstances);
        } catch (ParseException e) {
            showNotification("Invalid datetime", "Valid format datetime is 'yyyy-MM-dd HH:mm:ss'.", 3);
        } catch (Exception e) {
            logger.error("Failed to show data from given datetime.", e);
        }
    }

    /**
     * Clears the datetime input and refreshes the instances.
     * This method is used when the user wants to reset the cluster state to the current state and clear the datetime given by the user.
     * It sets the selectedDateTime to false and calls the refreshInstances method to update the instances table view.
     */
    public void clearInfo(){
        // Set selectedDateTime to false so to indicate that no datetime is currently selected.
        selectedDateTime = false;

        // Refresh the instances table view to reflect the current state of the instances.
        refreshInstances();
    }

    /**
     * Handles the upload of a Docker Compose file.
     * This method opens a FileChooser dialog that allows the user to select a Docker Compose file (.yaml) from their file system.
     * If a file is selected, it gets the absolute path of the file and loads the Compose scene.
     * The path of the selected file is then passed to the ComposeController.
     * If no file is selected, the method does nothing.
     *
     * @param actionEvent The event that triggered the file upload.
     */
    public void handleUploadFile(ActionEvent actionEvent) {
        // Create a FileChooser.
        FileChooser fileChooser = new FileChooser();

        // Set the extension filter to .yaml files.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("YAML files (*.yaml)", "*.yaml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Open the FileChooser dialog.
        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        // If a file is selected, load the Compose scene and pass the file path to the ComposeController.
        if (file != null) {
            try {
                // Get the absolute path of the selected Docker Compose file.
                String dockerComposeFilePath = file.getAbsolutePath();

                // Create a new FXMLLoader and load the Compose scene from the FXML file.
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/composeScene.fxml"));
                Parent root = loader.load();

                // Get the controller for the Compose scene.
                ComposeController controller = loader.getController();

                // Pass the path of the Docker Compose file to the ComposeController.
                controller.setYamlFilePath(dockerComposeFilePath);

                // Get the current stage from the action event source.
                stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                // Set the Compose scene as the root of the stage and display it.
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                logger.error("Failed to handle upload file.", e);
            }
        }
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
        // Get the controller for the Volumes scene and refresh the volumes before user sees it.
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

    /**
     * Changes the current scene to a new scene.
     * This method loads the FXML file for the new scene,
     * sets it as the root of the current stage,
     * and displays the new scene. It is used to navigate between different scenes in the application.
     * It also stops the Timeline to prevent Watchdog from lagging and keep it clean.
     *
     * @param actionEvent The event that triggered the scene change.
     * @param fxmlFile The name of the FXML file for the new scene.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        // Stop the Timeline to prevent Watchdog from lagging.
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
     * Changes the current scene to the User scene.
     * This method loads the User scene and sets it as the root of the current stage.
     * It also passes the name of the current scene to the UserController so it will know where
     * to go back when the user clicks the back button.
     * If an error occurs while loading the User scene, it throws a RuntimeException.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while loading the User scene.
     */
    public void changeToUserScene(ActionEvent actionEvent) throws IOException {
        // Create a new FXMLLoader for the User scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userScene.fxml"));
        try {
            // Load the User scene
            root = loader.load();
        } catch (IOException e) {
            // If an error occurs while loading the User scene, throw a RuntimeException
            throw new RuntimeException(e);
        }
        // Get the UserController for the User scene
        UserController userController = loader.getController();
        // Pass the name of the current scene to the UserController
        userController.onUserSceneLoad( "containersScene.fxml");
        // Get the current stage
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        // Set the User scene as the root of the current stage
        stage.getScene().setRoot(root);
        // Display the User scene
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

    /**
     * Displays a notification with the given title and content.
     * This method creates a new Popup and adds a VBox to it that contains two Labels: one for the title and one for the content of the notification.
     * The Popup is displayed on the screen at a position relative to the notificationBoxStatic.
     * A FadeTransition is applied to the VBox, which gradually decreases its opacity from 1.0 to 0.0 over the course of 1 second.
     * The FadeTransition is started after 4 seconds by a Timeline.
     * When the FadeTransition is finished, the Popup is hidden.
     *
     * @param title The title of the notification.
     * @param content The content of the notification.
     * @param duration The duration of the notification in seconds.
     */
    public static void showNotification(String title, String content, int duration) {
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
            Point2D point = notificationBoxStatic.localToScreen(0, 0);

            // Show the Popup on the screen at the calculated position.
            // notificationBoxStatic can be null if user has changed panels.
            if (notificationBoxStatic.getScene() != null ) {
                notification.show(notificationBoxStatic.getScene().getWindow(), point.getX(), point.getY());
            }

            // Create a FadeTransition for the VBox.
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), box);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);

            // Create a Timeline that will start the FadeTransition after given seconds.
            Timeline timelineNotification = new Timeline(new KeyFrame(Duration.seconds(duration), evt -> fadeTransition.play()));
            timelineNotification.play();

            // Hide the Popup when the FadeTransition is finished.
            fadeTransition.setOnFinished(event -> notification.hide());
        });
    }

    /**
     * Changes the visibility of the loading image.
     * This method is used to show or hide the loading image in the user interface.
     * It is called by ApiService upon start or complete of an operation.
     *
     * @param toBeShown A boolean indicating whether the loading image should be shown or hidden.
     */
    public static void showLoading(boolean toBeShown) {
        loadingImageViewStatic.setVisible(toBeShown);
    }
}
