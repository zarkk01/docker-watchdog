package gr.aueb.dmst.dockerWatchdog.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import gr.aueb.dmst.dockerWatchdog.Exceptions.VolumeFetchException;
import gr.aueb.dmst.dockerWatchdog.Models.VolumeScene;
import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * FX Controller class for the Volumes panel.
 * This class handles user interactions with the Volumes scene, such as navigating to other scenes and refreshing the volumes table.
 * It also retrieves all volumes from the REST API and parses the response into a list of VolumeScene objects.
 * The class uses the ExecutorThread class to send HTTP requests to the REST API.
 * It also uses the LogManager class to log error messages.
 */
public class VolumesController implements Initializable {

    // Logger instance used mainly for errors.
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
    public ImageView watchdogImage;

    /**
     * This method is automatically called after the fxml file has been loaded.
     * It sets up the table columns, applies the hover effect to the sidebar buttons, sets the placeholder for the volumes table,
     * and refreshes the volumes table.
     * It also installs a tooltip on the watchdog image.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     * @throws IOException If an error occurs while initializing the VolumesController.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up the table columns.
            setupTableColumns();
            // Apply the hover effect to the sidebar buttons.
            hoveredSideBarImages();
            // Set the placeholder for the volumes table.
            volumesTableView.setPlaceholder(new Label("No volumes available."));
            // Refresh the volumes table.
            refreshVolumes();

            // Install funny tooltip on watchdog imageView
            Tooltip woof = new Tooltip("Woof!");
            woof.setShowDelay(Duration.millis(20));
            Tooltip.install(watchdogImage,woof);

        } catch (Exception e) {
            System.err.println("An error occurred while initializing the VolumesController: " + e.getMessage());
            volumesTableView.setPlaceholder(new Label("An error occurred while loading the volumes."));
        }
    }

    /**
     * This method sets the cell value factories for the table columns.
     * The cell value factory determines the data with name, driver, mountpoint, containerNamesUsing
     * that should be displayed in each cell of the table column.
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        mountpointColumn.setCellValueFactory(new PropertyValueFactory<>("mountpoint"));
        containerNamesUsingColumn.setCellValueFactory(new PropertyValueFactory<>("containerNamesUsing"));
    }

    /**
     * Refreshes the volumes table.
     * This method retrieves all volumes by calling the `getAllVolumes` method and updates the volumes table with the new data.
     * If an error occurs while retrieving the volumes, it logs the error message.
     */
    public void refreshVolumes() {
        try {
            // Retrieve all volumes.
            List<VolumeScene> volumes = getAllVolumes();

            // Clear the volumes table and add the new data.
            volumesTableView.getItems().clear();
            volumesTableView.getItems().addAll(volumes);
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
            // Create a new HTTP request to the Docker API.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/volumes"))
                    .GET()
                    .build();

            // Send the request and parse the response into a JSONGArray.
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray jsonArray = new JSONArray(response.body());

            // Create a list to hold the VolumeScene objects.
            List<VolumeScene> volumes = new ArrayList<>();

            // Loop through the JSONArray and create a new VolumeScene object for each JSONObject.
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                String driver = jsonObject.getString("driver");
                String mountpoint = jsonObject.getString("mountpoint");
                String containerNamesUsing = jsonObject.getString("containerNamesUsing");
                volumes.add(new VolumeScene(name, driver, mountpoint, containerNamesUsing));
            }

            // Return the list of VolumeScene objects.
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
        // Change the scene to the Volumes scene.
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
}
