package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import gr.aueb.dmst.dockerWatchdog.exceptions.VolumeFetchException;
import gr.aueb.dmst.dockerWatchdog.gui.models.VolumeScene;
import static gr.aueb.dmst.dockerWatchdog.gui.GuiApplication.client;


/**
 * FX Controller class for the Volumes panel.
 * This class handles user interactions with the Volumes scene, such as navigating to other scenes and refreshing the volumes table. Also,
 * it handles the removal of volumes from the database.
 * It additionally retrieves all volumes using WATCHDOG REST API and parses the response into a list of VolumeScene objects.
 * It also uses the LogManager class to log error messages.
 */
public class VolumesController implements Initializable {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(VolumesController.class);

    private Stage stage;
    private Parent root;

    @FXML
    private TableColumn<VolumeScene, String> nameColumn;
    @FXML
    private TableColumn<VolumeScene, String> driverColumn;
    @FXML
    private TableColumn<VolumeScene, String> mountpointColumn;
    @FXML
    private TableColumn<VolumeScene, String> containerNamesUsingColumn;
    @FXML
    private TableColumn<VolumeScene, Void> removeVolumeColumn;
    @FXML
    private TableView<VolumeScene> volumesTableView;

    @FXML
    private Button containersButton;
    @FXML
    private Button imagesButton;
    @FXML
    private Button graphicsButton;
    @FXML
    private Button kubernetesButton;
    @FXML
    private ImageView watchdogImage;

    @FXML
    private Button createVolumeButton;
    @FXML
    private VBox sideBar;
    @FXML
    private HBox topBar;
    @FXML
    private Text volumesHead;
    @FXML
    private Button userButton;

    @FXML
    private VBox notificationBox;

    /**
     * This method is automatically called after the fxml file has been loaded.
     * It sets up the table columns, applies the hover effect to the sidebar buttons, sets the placeholder for the volumes table,
     * and refreshes the volumes table.
     * It also installs a tooltip on the watchdog image.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {



            if (UserController.token == null) {
                userButton.setText("Log in");
            } else {
                userButton.setText("Logged in");
            }


            // Set up the drop shadow effect for various components in the scene
            setUpShadows();

            // Set up the table columns
            setUpTableColumns();
            // Apply the hover effect to the sidebar buttons
            hoveredSideBarImages();
            // Set the placeholder for the volumes table
            volumesTableView.setPlaceholder(new Label("No volumes available."));
            // Refresh the volumes table
            refreshVolumes();

            // Install funny tooltip on watchdog imageView
            Tooltip woof = new Tooltip("Woof!");
            woof.setShowDelay(Duration.millis(20));
            Tooltip.install(watchdogImage,woof);
        } catch (Exception e) {
           logger.error("An error occurred while initializing the VolumesController: " + e.getMessage());
        }
    }

    /**
     * Sets up the drop shadow effect for various components in the scene.
     * This method creates a new DropShadow effect and applies it to the volumesHead, topBar, sideBar, volumesTableView, and createVolumeButton.
     * The radius of the shadow is set to 7.5 and the color is set to a semi-transparent black.
     */
    private void setUpShadows() {
        // Set up drop shadow effect for the components.
        DropShadow shadow = new DropShadow();
        shadow.setRadius(7.5);
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        volumesHead.setEffect(shadow);
        topBar.setEffect(shadow);
        sideBar.setEffect(shadow);
        volumesTableView.setEffect(shadow);
        createVolumeButton.setEffect(shadow);
    }

    /**
     * This method sets the cell value factories for the table columns.
     * The cell value factory determines the data with name, driver, mountpoint, containerNamesUsing
     * that should be displayed in each cell of the table column. Also, it creates an additional column
     * with a button for each volume, which allows the user to remove the volume.
     */
    private void setUpTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        mountpointColumn.setCellValueFactory(new PropertyValueFactory<>("mountpoint"));
        containerNamesUsingColumn.setCellValueFactory(new PropertyValueFactory<>("containerNamesUsing"));
        // Using a custom cell factory to create a button for each volume
        removeVolumeColumn.setCellFactory(createButtonCellFactory(
                "Delete volume",
                "/images/binRed.png",
                "/images/binHover.png",
                "/images/binClick.png", volume -> {
                    try {
                        // If volume name is too long, cut it
                        String volumeNameCut = volume.getName();
                        if (volume.getName().length() > 6) {
                            volumeNameCut = volume.getName().substring(0, 6) + "...";
                        }
                        // Check if the volume is currently in use by a container
                        if (volume.getContainerNamesUsing().isEmpty()) {
                            removeVolume(volume.getName());
                            // Show a notification to the user that the volume was successfully removed
                            showNotification("Woof!", "Volume " + volumeNameCut + "... was successfully removed.");
                        } else {
                            // Show a notification to the user that the volume is currently in use by a container
                            showNotification("Grrr!", "Volume " + volumeNameCut + "... is currently in use by a container.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
    }

    /**
     * Creates a cell factory for the Remove column. This factory produces cells that contain a button with various properties.
     * In our case, we use it to create a button for a volume's removal.
     *
     * @param tooltipText The text to be displayed when the mouse hovers over the button.
     * @param imagePath The path to the image to be displayed on the button.
     * @param hoverImagePath The path to the image to be displayed on the button when the mouse hovers over it.
     * @param clickImagePath The path to the image to be displayed on the button when it is clicked.
     * @param action The action to be performed when the button is clicked.
     * @return A Callback that produces TableCell objects containing the button.
     */
    private Callback<TableColumn<VolumeScene, Void>, TableCell<VolumeScene, Void>> createButtonCellFactory(String tooltipText, String imagePath, String hoverImagePath, String clickImagePath, Consumer<VolumeScene> action) {
        return new Callback<>() {
            // This method creates a new TableCell containing a button with the given properties
            @Override
            public TableCell<VolumeScene, Void> call(final TableColumn<VolumeScene, Void> param) {
                final TableCell<VolumeScene, Void> cell = new TableCell<>() {
                    private final Button btn = new Button();
                    private final Tooltip tooltip = new Tooltip(tooltipText);

                    // Create the images for the button
                    private final ImageView view = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
                    private final ImageView viewHover = new ImageView(new Image(getClass().getResource(hoverImagePath).toExternalForm()));
                    private final ImageView viewClick = new ImageView(new Image(getClass().getResource(clickImagePath).toExternalForm()));

                    // Block of code regarding the button's setup
                    {
                        // Set up the button and its effects
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

                        // Set up the removal action for the button
                        btn.setOnAction((ActionEvent event) -> {
                            VolumeScene volume = getTableView().getItems().get(getIndex());
                            action.accept(volume);
                            // Then refresh the volumes table to be without the old volume
                            refreshVolumes();
                        });

                        // Set up the hover and click effects for the button
                        btn.setOnMouseEntered(e -> view.setImage(viewHover.getImage()));
                        btn.setOnMouseExited(e -> view.setImage(new Image(getClass().getResource(imagePath).toExternalForm())));
                        btn.setOnMousePressed(e -> view.setImage(viewClick.getImage()));
                        btn.setOnMouseReleased(e -> view.setImage(viewHover.getImage()));
                    }

                    // This method updates the item whenever the cell is updated
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
     * Removes a volume from our database using our WATCHDOG REST API.
     * This method sends a POST request to the REST API to remove a volume
     * with the given name. If an error occurs while sending the request,
     * it logs the error message.
     */
    public void removeVolume(String volumeName) {
        // Send a POST request to the WATCHDOG REST API to remove the volume.
        try {
            // Create a new HTTP request to the WATCHDOG REST API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/volumes/" + volumeName + "/remove"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send the request.
            client.send(request, HttpResponse.BodyHandlers.ofString());

            // Refresh the volumes table to be always up-to-date with this
            // way of Thread.sleep to break this asynchronous nature of the program.
            Thread.sleep(50);
            refreshVolumes();
        } catch (Exception e) {
            if (volumeName.length() > 6) {
                volumeName = volumeName.substring(0,6) + "...";
            }
            showNotification("Error", "Volume " + volumeName + " was not removed.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Creates a new volume with a specified name.
     * This method prompts the user to enter a name for the new volume using a TextInputDialog.
     * If a volume with the entered name already exists, a notification is shown to the user and the method returns.
     * Otherwise, a POST request is sent to the WATCHDOG REST API to create a new volume with the entered name.
     * After the volume is created, the volumes table is refreshed and a notification is shown to the user.
     */
    public void createVolume(){
        try {
            // Create a TextInputDialog so to get the name of the volume to be created
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Volume");
            dialog.setHeaderText("Enter Volume Name");
            dialog.setContentText("Name:");
            String volumeName = dialog.showAndWait().get();

            // Check if the volume with this name already exists
            List<VolumeScene> volumes = getAllVolumes();
            for (VolumeScene volume : volumes) {
                if (volume.getName().equals(volumeName)) {
                    // If volume name is too long, cut it
                    String volumeNameCut = volumeName;
                    if(volumeName.length() > 6){
                        volumeNameCut = volumeName.substring(0,6) + "...";
                    }
                    // If yes, show a notification to the user that he cannot create and return
                    showNotification("Error", "Volume " + volumeNameCut + " already exists.");
                    return;
                }
            }

            // If not, create a new volume with the given name
            try {
                // Create a new HTTP request to the WATCHDOG REST API
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("http://localhost:8080/api/volumes/" + volumeName + "/create"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                // Send the request
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() ==  200){
                    // If volume name is too long, cut it
                    String volumeNameCut = volumeName;
                    if(volumeName.length() > 6){
                        volumeNameCut = volumeName.substring(0,6) + "...";
                    }
                    // Show a notification to the user that the volume was successfully created
                    showNotification("Woof!", "Volume " + volumeNameCut + " was successfully created.");
                }
            } catch (Exception e) {
                showNotification("Error", "Volume " + volumeName + " was not created.");
                logger.error(e.getMessage());
            }

            // Refresh the volumes table to be always up-to-date with this
            // way of Thread.sleep to break this asynchronous nature of the program.
            Thread.sleep(50);
            refreshVolumes();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Refreshes the volumes table.
     * This method retrieves all volumes by calling the {@link #getAllVolumes()} method and updates the volumes table with the new data.
     * If an error occurs while retrieving the volumes, it logs the error message.
     */
    public void refreshVolumes() {
        try {
            // Clear the volumes table
            volumesTableView.getItems().clear();

            // Retrieve all volumes
            List<VolumeScene> volumes = getAllVolumes();

            // Create an ObservableList to hold the VolumeScene objects
            ObservableList<VolumeScene> observableVolumes = FXCollections.observableArrayList();

            // Add all volumes to the ObservableList
            observableVolumes.addAll(volumes);

            // Add the ObservableList to the volumes table
            volumesTableView.setItems(observableVolumes);
        } catch (VolumeFetchException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Retrieves all volumes from our database using our REST API.
     * This method sends a GET request to the REST API to retrieve a list of all volumes.
     * It then parses the response into a list of VolumeScene objects.
     *
     * @return A list of VolumeScene objects representing all volumes.
     * @throws VolumeFetchException If an error occurs while sending the request or parsing the response.
     */
    public List<VolumeScene> getAllVolumes() throws VolumeFetchException {
        try {
            // Create a new HTTP request to the Docker API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/volumes"))
                    .GET()
                    .build();

            // Send the request and parse the response into a JSONGArray
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray jsonArray = new JSONArray(response.body());

            // Create a list to hold the VolumeScene objects
            List<VolumeScene> volumes = new ArrayList<>();

            // Loop through the JSONArray and create a new VolumeScene object for each JSONObject
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                String driver = jsonObject.getString("driver");
                String mountpoint = jsonObject.getString("mountpoint");
                String containerNamesUsing = jsonObject.getString("containerNamesUsing");
                volumes.add(new VolumeScene(name, driver, mountpoint, containerNamesUsing));
            }

            // Return the list of VolumeScene objects
            return volumes;
        } catch (Exception e) {
            throw new VolumeFetchException("Error while retrieving volumes, assure that the REST API is running and try again. Error: " + e.getMessage());
        }
    }

    /**
     * Sets the hover effect for the sidebar images.
     * This method applies a hover effect to the sidebar buttons.
     * The `setHoverEffect` method takes a button and two image paths as parameters:
     * the path to the original image and the path to the image to be displayed when the button is hovered over.
     */
    private void hoveredSideBarImages() {
        setHoverEffect(containersButton, "/images/containerGrey.png", "/images/container.png");
        setHoverEffect(imagesButton, "/images/imageGrey.png", "/images/image.png");
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
        // Load the original image and the hovered image
        Image originalImage = new Image(getClass().getResourceAsStream(originalImagePath));
        Image hoveredImage = new Image(getClass().getResourceAsStream(hoveredImagePath));

        // Set the original image as the button's graphic
        ((ImageView) button.getGraphic()).setImage(originalImage);

        // Set the hover effect: when the mouse enters the button, change the image and add the hover style class
        button.setOnMouseEntered(event -> {
            button.getStyleClass().add("button-hovered");
            ((ImageView) button.getGraphic()).setImage(hoveredImage);
        });

        // Remove the hover effect: when the mouse exits the button, change the image back to the original and remove the hover style class
        button.setOnMouseExited(event -> {
            button.getStyleClass().remove("button-hovered");
            ((ImageView) button.getGraphic()).setImage(originalImage);
        });
    }

    /**
     * Displays a notification with the given title and content.
     * It helps us keep user informed about events that occur in the volume.
     * After 3 seconds, the Popup is automatically hidden.
     *
     * @param title The title of the notification.
     * @param content The content of the notification.
     */
    public void showNotification(String title, String content) {
        Platform.runLater(() -> {
            // Create a new Popup for the notification
            Popup notification = new Popup();

            // Create a Label for the title of the notification and style it
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

            // Create a Label for the content of the notification and style it
            Label contentLabel = new Label(content);
            contentLabel.setTextFill(Color.WHITE);

            // Create a VBox to hold the title and content Labels and style it with our brand colors
            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #f14246; -fx-padding: 10px; -fx-border-color: #525252; -fx-border-width: 1px;");

            // Add the VBox to the Popup
            notification.getContent().add(box);

            // Calculate the position of the Popup on the screen
            Point2D point = notificationBox.localToScreen(notificationBox.getWidth() - box.getWidth(), notificationBox.getHeight() - box.getHeight());

            // Show the Popup on the screen at the calculated position
            notification.show(notificationBox.getScene().getWindow(), point.getX(), point.getY());

            // Create a Timeline that will hide the Popup after 3 seconds
            Timeline timelineNotification = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timelineNotification.play();
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
        // Load the FXML file for the new scene
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));

        // Get the current stage
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        // Set the new scene as the root of the stage and display it
        stage.getScene().setRoot(root);
        stage.show();
    }




    public void changeToUserScene(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserController userController = loader.getController();
        // Pass the selected instance to the IndividualContainerController and the scene we are coming from.
        userController.onUserSceneLoad( "volumesScene.fxml");
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}
