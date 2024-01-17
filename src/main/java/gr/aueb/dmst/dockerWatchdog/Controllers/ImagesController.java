package gr.aueb.dmst.dockerWatchdog.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

import gr.aueb.dmst.dockerWatchdog.Exceptions.ImageActionException;
import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The ImagesController class is an FX Controller responsible for managing the Images Panel in the application.
 * It provides methods for handling user interactions with images, such as creating a container from an image,
 * starting all containers from an image, stopping all containers from an image, pulling a given image, and removing an image.
 * It also provides methods for changing panels, showing notifications, and updating the images displayed in the TableView.
 * The class uses the WATCHDOG REST API to communicate with the backend and our database and send requests for information and actions.
 */
public class ImagesController implements Initializable {
    // Base URL for Watchdog API regarding images
    private static final String BASE_URL = "http://localhost:8080/api/images/";

    private Stage stage;
    private Parent root;

    @FXML
    private TableColumn<ImageScene, String> idColumn;
    @FXML
    private TableColumn<ImageScene, String> nameColumn;
    @FXML
    private TableColumn<ImageScene, String> statusColumn;
    @FXML
    private TableColumn<ImageScene, Long> sizeColumn;
    @FXML
    private TableColumn<ImageScene, Void> createContainerCollumn;
    @FXML
    private TableColumn<ImageScene, Void> startAllCollumn;
    @FXML
    private TableColumn<ImageScene, Void> stopAllCollumn;
    @FXML
    private TableColumn<ImageScene, Void> removeImageColumn;
    @FXML
    private TableView<ImageScene> imagesTableView;

    @FXML
    private CheckBox usedImagesCheckbox;
    @FXML
    private TextField pullImageTextField;
    @FXML
    private TextField searchField;
    @FXML
    private VBox notificationBox;

    @FXML
    private Button containersButton;
    @FXML
    private Button graphicsButton;
    @FXML
    private Button kubernetesButton;
    @FXML
    private Button volumesButton;

    @FXML
    private Label startingLabel;
    @FXML
    private Label imageNameLabel;
    @FXML
    private Pane totalContainersCircle;
    @FXML
    private Pane runningContainersCircle;
    @FXML
    private Pane stoppedContainersCircle;
    @FXML
    private Label totalContainersTextLabel;
    @FXML
    private Label runningContainersTextLabel;
    @FXML
    private Label stoppedContainersTextLabel;

    @FXML
    private TableView<InstanceScene> instancesTableView;
    @FXML
    private TableColumn<InstanceScene, String> instancesNameColumn;
    @FXML
    private TableColumn<InstanceScene, String> instancesStatusColumn;

    @FXML
    private Text totalContainersText;
    @FXML
    private Text runningContainersText;
    @FXML
    private Text stoppedContainersText;

    @FXML
    private ImageView watchdogImage;
    private Timeline timeline;
    // This variable is used to know which image is selected and displayed in the down info panel.
    private ImageScene imageScene;


    /**
     * This method is automatically called when user navigates to Images Panel.
     * It sets up the TableView columns, hover effects for the sidebar images, and starts a timeline to refresh images.
     * It also sets up the cell factories for the TableView columns that contain buttons. Also, it sets up a tooltip for the watchdog logo saying Woof!.
     * Finally, it adds an action listener to the TableView that fill down info panel with the selected image's info when
     * the user clicks on an image.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up the TableView columns.
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Set up the hover effects for the sidebar images.
            hoveredSideBarImages();

            // Set up a tooltip for the watchdog logo saying Woof!.
            Tooltip woof = new Tooltip("Woof!");
            woof.setShowDelay(Duration.millis(20));
            Tooltip.install(watchdogImage, woof);

            // Set up the cell factories for the TableView columns that contain buttons.
            createContainerCollumn.setCellFactory(createButtonCellFactory(
                    "Create a Container",
                    "/images/create.png",
                    "/images/createHover.png",
                    "/images/playClick.png", image -> {
                        try {
                            this.createContainer(image);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));

            startAllCollumn.setCellFactory(createButtonCellFactory(
                    "Start All Containers",
                    "/images/play.png",
                    "/images/playHover.png",
                    "/images/playClick.png", image -> {
                        try {
                            this.startAllContainers(image.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));

            stopAllCollumn.setCellFactory(createButtonCellFactory(
                    "Stop All Containers",
                    "/images/stop.png",
                    "/images/stopHover.png",
                    "/images/stopClick.png", image -> {
                        try {
                            this.stopAllContainers(image.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));

            removeImageColumn.setCellFactory(createButtonCellFactory(
                    "Delete An Image",
                    "/images/binRed.png",
                    "/images/binHover.png",
                    "/images/binClick.png", image -> {
                        try {
                            this.removeImage(image.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));

            // Set the placeholder for the TableView.
            imagesTableView.setPlaceholder(new Label("No images available."));

            // Set up the action for the usedImagesCheckbox.
            usedImagesCheckbox.setOnAction(event -> {
                refreshImages();
            });

            // Refresh the images.
            refreshImages();

            // Set up the TableView columns for the instancesTableView in the down info panel.
            instancesNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            instancesStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Add an action listener to the TableView that fill down info panel with the selected image's info.
            imagesTableView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            startingLabel.setVisible(false);
                            // Get the selected image and set it to the imageScene variable.
                            ImageScene selectedImage = newSelection;
                            imageScene = selectedImage;
                            try {
                                // Depending on the usage of the image, fill down info panel with the selected image's info.
                                if(selectedImage.getStatus().equals("In use")) {
                                    adjustInfoPanel(selectedImage);
                                } else {
                                    adjustZeroInfoPanel(selectedImage);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            // Start a timeline to refresh images every 1.5 seconds.
            timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), evt -> refreshImages()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This method is called when the user clicks on an image in the TableView.
     * It fills down info panel with the selected image's info and sets up the instancesTableView
     * so to be ready when clicked to move in individualContainersPanel.
     *
     * @param image
     * @throws Exception
     */
    public void adjustInfoPanel(ImageScene image) throws Exception {
        // Get all instances of the selected image.
        List<InstanceScene> instances = getAllInstancesByImage(image.getName());

        // Set the text of the label with the name of the image.
        imageNameLabel.setText(image.getName());

        // Set the text of the labels with the number of total, running, and stopped containers.
        totalContainersText.setText(String.valueOf(instances.size()));
        long runningContainers = 0;
        long stoppedContainers = 0;
        for (InstanceScene instance : instances) {
            if (instance.getStatus().equals("running")) {
                runningContainers++;
            } else {
                stoppedContainers++;
            }
        }
        runningContainersText.setText(String.valueOf(runningContainers));
        stoppedContainersText.setText(String.valueOf(stoppedContainers));

        // Clear the current items in the instancesTableView and add the instances.
        instancesTableView.getItems().clear();
        instancesTableView.getItems().addAll(instances);

        // Move user to individualContainersPanel when he clicks on an instance so to see more info about instance.
        instancesTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && (!instancesTableView.getSelectionModel().isEmpty())) {
                // Get the selected instance.
                InstanceScene selectedInstance = instancesTableView.getSelectionModel().getSelectedItem();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/individualContainerScene.fxml"));
                try {
                    root = loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // Load the controller of the individualContainerScene.
                IndividualContainerController individualContainerController = loader.getController();
                // Pass the selected instance to the controller and the scene we're coming from.
                individualContainerController.onInstanceDoubleClick(selectedInstance, "imagesScene.fxml");
                stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.show();
            }
        });

        // Make all these visible because firstly only the startingLabel which says
        // "Click...for more information" is visible.
        instancesTableView.setVisible(true);
        totalContainersCircle.setVisible(true);
        runningContainersCircle.setVisible(true);
        stoppedContainersCircle.setVisible(true);
        totalContainersTextLabel.setVisible(true);
        runningContainersTextLabel.setVisible(true);
        stoppedContainersTextLabel.setVisible(true);
        totalContainersText.setVisible(true);
        runningContainersText.setVisible(true);
        stoppedContainersText.setVisible(true);
        imageNameLabel.setVisible(true);
    }

    /**
     * This method is sending a GET request to the WATCHDOG REST API to retrieve all instances of the specific image user clicked.
     * It parses the response body to extract the details of each instance, and
     * it creates a new InstanceScene object for each instance and adds it to the list.
     * It returns a list of all instances of the specific image user clicked as InstanceScene objects
     * so to be ready to be displayed and handled.
     *
     * @param imageName
     * @return A list of all instances of the specific image user clicked.
     * @throws Exception
     */
    public List<InstanceScene> getAllInstancesByImage(String imageName) throws Exception {
        // Send a GET request to the WATCHDOG REST API to retrieve all instances of the specific image user clicked.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images/getContainers/" + imageName))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
            instances.add(new InstanceScene(id, name, image
                    , status, memoryUsage, pids, cpuUsage, blockI
                    , blockO, volumes, subnet, gateway, prefixLen,
                    false));
        }
        return instances;
    }

    /**
     * This method is called when the user clicks on an image in the TableView which is not in use from
     * any instance. In this case, we don't need to send a GET request to the WATCHDOG REST API, and we just set 0 all
     * the labels and circles of the down info panel.
     *
     * @param image
     */
    public void adjustZeroInfoPanel(ImageScene image) {
        // Set the text of the label with the name of the image.
        imageNameLabel.setText(image.getName());

        // Set the text of the labels with the number of total, running, and stopped containers.
        totalContainersText.setText("0");
        runningContainersText.setText("0");
        stoppedContainersText.setText("0");

        // Clear the current items in the instancesTableView and add the instances.
        instancesTableView.getItems().clear();
        instancesTableView.setPlaceholder(new Label("No instances available for " + image.getName() + "."));

        // Make all these visible because firstly only the startingLabel which says
        // "Click...for more information" is visible.
        instancesTableView.setVisible(true);
        totalContainersCircle.setVisible(true);
        runningContainersCircle.setVisible(true);
        stoppedContainersCircle.setVisible(true);
        totalContainersTextLabel.setVisible(true);
        runningContainersTextLabel.setVisible(true);
        stoppedContainersTextLabel.setVisible(true);
        totalContainersText.setVisible(true);
        runningContainersText.setVisible(true);
        stoppedContainersText.setVisible(true);
        imageNameLabel.setVisible(true);
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
     * Creates a cell factory for our each of the 4 lasts TableColumns of the Images TableView.
     * This method creates a cell factory for Create A Container, Stop All Containers,
     * Start All Containers, and Delete An Image buttons.
     * Each cell contains a button with an image(emoji), a tooltip(for indexing what it does),
     * and an action that is performed when the button is clicked.
     * The button's image changes when the button is hovered over or clicked.
     * The tooltip text, the image paths, and the action are specified by the parameters.
     *
     * @param tooltipText The text of the tooltip that is displayed when the button is hovered over.
     * @param imagePath The path to the image that is displayed on the button.
     * @param hoverImagePath The path to the image that is displayed on the button when it is hovered over.
     * @param clickImagePath The path to the image that is displayed on the button when it is clicked.
     * @param action The action that is performed when the button is clicked. It is a Consumer that takes an ImageScene object as input.
     * @return A Callback that can be used as a cell factory for a TableColumn.
     */
    private Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> createButtonCellFactory(String tooltipText, String imagePath, String hoverImagePath, String clickImagePath, Consumer<ImageScene> action) {
        return new Callback<>() {
            @Override
            public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                final TableCell<ImageScene, Void> cell = new TableCell<>() {
                    private final Button btn = new Button();
                    private final Tooltip tooltip = new Tooltip(tooltipText);
                    private final ImageView view = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
                    private final ImageView viewHover = new ImageView(new Image(getClass().getResource(hoverImagePath).toExternalForm()));
                    private final ImageView viewClick = new ImageView(new Image(getClass().getResource(clickImagePath).toExternalForm()));

                    {
                        // Set up the button and its effects.
                        DropShadow dropShadow = new DropShadow();
                        btn.setEffect(dropShadow);
                        tooltip.setShowDelay(Duration.millis(50));
                        Tooltip.install(btn, tooltip);
                        view.setFitHeight(30);
                        view.setPreserveRatio(true);
                        viewHover.setFitHeight(30);
                        viewHover.setPreserveRatio(true);
                        viewClick.setFitHeight(20);
                        viewClick.setPreserveRatio(true);
                        view.setOpacity(0.8);
                        btn.setGraphic(view);

                        // Set up the action for the button.
                        btn.setOnAction((ActionEvent event) -> {
                            ImageScene image = getTableView().getItems().get(getIndex());
                            action.accept(image);
                        });

                        // Set up the hover and click effects for the button.
                        btn.setOnMouseEntered(e -> view.setImage(viewHover.getImage()));
                        btn.setOnMouseExited(e -> view.setImage(new Image(getClass().getResource(imagePath).toExternalForm())));
                        btn.setOnMousePressed(e -> view.setImage(viewClick.getImage()));
                        btn.setOnMouseReleased(e -> view.setImage(viewHover.getImage()));
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
    }

    /**
     * Sets the hover effect for the sidebar images.
     * This method applies a hover effect to the sidebar buttons.
     * The `setHoverEffect` method takes a button and two image paths as parameters:
     * the path to the original image and the path to the image to be displayed when the button is hovered over.
     */
    private void hoveredSideBarImages() {
        setHoverEffect(containersButton, "/images/containerGrey.png", "/images/container.png");
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
     * Refreshes the images displayed in the TableView.
     * This method retrieves all images from the WATCHDOG REST API,
     * clears the current items in the TableView,
     * and adds the retrieved images to the TableView.
     * If the 'usedImagesCheckbox' is selected, only images that are in use are added to the TableView.
     * If the 'usedImagesCheckbox' is not selected, all images are added to the TableView.
     * The images are filtered by the text in the 'searchField' TextField.
     * Only images whose name contains the text in the 'searchField' are added to the TableView.
     */
    public void refreshImages() {
        try {
            // Retrieve all images from the WATCHDOG REST API.
            List<ImageScene> images = getAllImages();

            // Clear the current items in the TableView.
            imagesTableView.getItems().clear();

            // Iterate over the retrieved images.
            for (ImageScene image : images) {
                // If the image's name contains the text in the 'searchField' TextField...
                if (image.getName().contains(searchField.getText())) {
                    // If the 'usedImagesCheckbox' is selected...
                    if (usedImagesCheckbox.isSelected()) {
                        // If the image is in use, add it to the TableView.
                        if (image.getStatus().equals("In use")) {
                            imagesTableView.getItems().add(image);
                        }
                    } else {
                        // If the 'usedImagesCheckbox' is not selected, add the image to the TableView.
                        imagesTableView.getItems().add(image);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while refreshing images: " + e.getMessage());
        }
    }

    /**
     * Retrieves all images from the WATCHDOG REST API.
     * This method sends a GET request to the WATCHDOG REST API and receives a response.
     * The response body is a JSON array where each element is a JSON object that represents an image.
     * Each JSON object is parsed into an ImageScene object and added to a list.
     * The list of all ImageScene objects is returned.
     *
     * @return A list of all images, where each image is represented by an ImageScene object.
     * @throws Exception If an error occurs while sending the request or receiving the response.
     */
    public List<ImageScene> getAllImages() throws Exception {
        // Create a new HttpRequest that sends a GET request to the WATCHDOG REST API.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images"))
                .GET()
                .build();

        // Send the HttpRequest and receive the HttpResponse.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the body of the HttpResponse into a JSONArray.
        JSONArray jsonArray = new JSONArray(response.body());

        // Create a new list to hold the ImageScene objects.
        List<ImageScene> images = new ArrayList<>();

        // Iterate over each element in the JSONArray.
        for (int i = 0; i < jsonArray.length(); i++) {
            // Get the current element as a JSONObject.
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Extract the image's information from the JSONObject.
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String status = jsonObject.getString("status");
            Long size = jsonObject.getLong("size");

            // Create a new ImageScene object with the extracted information and add it to the list.
            images.add(new ImageScene(id, name, size, status));
        }

        // Return the list of ImageScene objects.
        return images;
    }

    /**
     * Creates a container from the given image.
     * This method sends a POST request to the WATCHDOG REST API to create a container from the image.
     * If the response status code is not 200 meaning we are in big trouble, an ImageActionException is thrown.
     * If the container is created successfully, the status of the image is set to "In use" and the images are refreshed.
     * Also, a noti is displayed.
     *
     * @param image The image from which the container is to be created.
     * @throws ImageActionException If an error occurs while creating the container.
     */
    public void createContainer(ImageScene image) throws ImageActionException {
        try {
            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "create/" + image.getName()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is not 200, throw an ImageActionException.
            if (response.statusCode() != 200) {
                throw new ImageActionException("Container creation failed", image.getName());
            }

            // If the container is created successfully, set the status of the image to "In use" and refresh the images.
            // Also, show a notification.
            showNotification("Success", "Container created successfully");
            image.setStatus("In use");

            refreshImages();

            // Also, if in the infoPanel, there is the same image, refresh the infoPanel.
            if(imageScene != null && imageScene.getName().equals(image.getName())) {
                adjustInfoPanel(imageScene);
            }
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while creating container: " + e.getMessage(), image.getName());
        }
    }

    /**
     * Starts all containers created from the given image.
     * This method sends a POST request to the WATCHDOG REST API to start all containers created from the image we want.
     * If the response status code is not 200 we are in big trouble and, an ImageActionException is thrown.
     * If all containers are started successfully, the images are refreshed and a noti is displayed.
     *
     * @param imageName The name of the image from which the containers were created.
     * @throws ImageActionException If an error occurs while starting the containers.
     */
    public void startAllContainers(String imageName) throws ImageActionException {
        try {
            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/startAll/" + imageName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is not 200, throw an ImageActionException.
            if (response.statusCode() != 200) {
                throw new ImageActionException("Starting all containers failed", imageName);
            }

            // If all containers are started successfully, refresh the images and show a notification.
            showNotification("Success", "All containers started successfully");
            refreshImages();

            // Also, if in the infoPanel, there is the same image, refresh the infoPanel.
            if(imageScene != null && imageScene.getName().equals(imageName)) {
                adjustInfoPanel(imageScene);
            }
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while starting all containers: " + e.getMessage(), imageName);
        }
    }

    /**
     * Stops all containers created from the given image.
     * This method sends a POST request to the WATCHDOG REST API to stop all containers created from the image.
     * If the response status code is not 200, an ImageActionException is thrown.
     * If all containers are stopped successfully, the images are refreshed and a noti is displayed.
     *
     * @param imageName The name of the image from which the containers were created.
     * @throws ImageActionException If an error occurs while stopping the containers.
     */
    public void stopAllContainers(String imageName) throws ImageActionException {
        try {
            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API to stop all containers created from the image.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/containers/stopAll/" + imageName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is not 200, throw an ImageActionException.
            if (response.statusCode() != 200) {
                throw new ImageActionException("Stopping all containers failed", imageName);
            }

            // If all containers are stopped successfully, refresh the images and show a notification.
            showNotification("Success", "All containers stopped successfully");
            refreshImages();

            // Also, if in the infoPanel, there is the same image, refresh the infoPanel.
            if(imageScene != null && imageScene.getName().equals(imageName)) {
                adjustInfoPanel(imageScene);
            }
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while stopping all containers: " + e.getMessage(), imageName);
        }
    }

    /**
     * Pulls the given image from the Docker registry.
     * This method sends a POST request to the WATCHDOG REST API to pull the image.
     * If the response status code is not 200, an ImageActionException is thrown.
     * If the image is pulled successfully, the images are refreshed and a notification is displayed.
     *
     * @throws ImageActionException If an error occurs while pulling the image.
     */
    public void pullGivenImage() throws ImageActionException {
        try {
            // Take the name of the image user wants to pull from DockerHub.
            String imageName = pullImageTextField.getText();

            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API to pull the image.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "pull/" + imageName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showNotification("Success", "Image pulled successfully");
                refreshImages();
            } else {
                // If the response status code is not 200, throw an ImageActionException.
                throw new ImageActionException("Image pull failed", imageName);
            }
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while pulling image: " + e.getMessage(), pullImageTextField.getText());
        }
    }

    /**
     * Removes the given image.
     * This method sends a POST request to the WATCHDOG REST API to remove the image.
     * If the response status code is not 200, an ImageActionException is thrown.
     * If the image is removed successfully, the images are refreshed and a notification is displayed.
     *
     * @param imageName The name of the image to be removed.
     * @throws ImageActionException If an error occurs while removing the image.
     */
    public void removeImage(String imageName) throws ImageActionException {
        try {
            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "remove/" + imageName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is not 200, throw an ImageActionException.
            if (response.statusCode() != 200) {
                throw new ImageActionException("Image removal failed", imageName);
            }

            // If the image is removed successfully, refresh the images and show a notification.
            showNotification("Success", "Image removed successfully");
            refreshImages();

            // Also, if in the infoPanel, there is the same image, refresh the infoPanel.
            if(imageScene != null && imageScene.getName().equals(imageName)) {
                adjustZeroInfoPanel(imageScene);
            }
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while removing image: " + e.getMessage(), imageName);
        }
    }

    /**
     * Displays a notification with the given title and content.
     * It helps us keep user informed about events that occur in images.
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
            Timeline timelineNotification = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timelineNotification.play();
        });
    }

    /**
     * Changes the current scene to a new scene.
     * This method loads the FXML file for the new scene,
     * sets it as the root of the current stage,
     * and displays the new scene. It is used to navigate between different scenes in the application.
     * It also stops the timeline to prevent our program from lagging and keep it clean.
     *
     * @param actionEvent The event that triggered the scene change.
     * @param fxmlFile The name of the FXML file for the new scene.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        // Stop the timeline to prevent our program from lagging.
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
     * Changes the current scene to the Kubernetes scene.
     * This method calls the `changeScene` method with
     * the action event that triggered the scene change
     * and the name of the FXML file for the Images scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while changing the scene.
     */
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
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
}
