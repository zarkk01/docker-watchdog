package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.FileInputStream;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;


/**
 * The ComposeController class is an FX Controller responsible for
 * managing the Compose scene in the application.
 * It provides methods for loading and saving YAML files,
 * validating the YAML content, and managing Docker Compose operations.
 * It also handles scene transitions and sets up UI components such as buttons and text areas.
 * The class uses JavaFX for the UI and the SnakeYAML library for parsing YAML files.
 * It also uses the ProcessBuilder class to start and stop Docker Compose files.
 */
public class ComposeController {

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(ComposeController.class);
    private Stage stage;
    private Parent root;

    private String yamlFilePath;

    @FXML
    private TextArea yamlContentArea;
    @FXML
    private Button backButton;
    @FXML
    private Button showConfigButton;
    private boolean isShowingConfig = false;
    @FXML
    private Button validateButton;
    @FXML
    private Button startComposeButton;
    @FXML
    private Button stopComposeButton;
    @FXML
    private Button saveButton;
    @FXML
    private VBox sideBar;
    @FXML
    private Button userButton;

    @FXML
    private Label fileNameLabel;
    @FXML
    private Label savedLabel;
    @FXML
    public VBox notificationBox;

    @FXML
    private ImageView watchdogImage;

    /**
     * Initializes the Compose scene to be ready for use and play.
     * This method sets up a listener for the text property of the yamlContentArea,
     * which updates the savedLabel to "Unsaved" whenever the text changes.
     * It also calls the setupBackButton method to set up the back button,
     * and installs a Tooltip on the watchdogImage.
     */
    public void initialize() {
        // Set up the shadows for the components of the Compose scene.
        setUpShadows();

        // Set up a listener for the text property of the yamlContentArea.
        // This listener updates the savedLabel to "Unsaved" whenever the text changes.
        yamlContentArea.textProperty().addListener((observable, oldValue, newValue) ->
                savedLabel.setText("Unsaved"));

        // Call the setupBackButton method to set up the back button.
        setUpBackButton();

        // Create a new Tooltip and install it on the logo.
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage, woof);

        // Set the text of the userButton to "Log in" if the user is not logged in, and to "Logged in" if the user is logged in.
        userButton.setText(UserController.token == null ? "Log in" : "Logged in");
    }

    /**
     * Sets up the drop shadow effect for various components in the scene.
     * This method creates a new DropShadow effect and applies it to the yamlContentArea,
     * backButton, showConfigButton, validateButton, startComposeButton, stopComposeButton,
     * saveButton, sideBar, and topBar.
     * The radius of the shadow is set to 5 and the color is set to a semi-transparent black.
     */
    private void setUpShadows() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.3));
        yamlContentArea.setEffect(shadow);
        backButton.setEffect(shadow);
        showConfigButton.setEffect(shadow);
        validateButton.setEffect(shadow);
        startComposeButton.setEffect(shadow);
        stopComposeButton.setEffect(shadow);
        saveButton.setEffect(shadow);
        sideBar.setEffect(shadow);
    }

    /**
     * This method loads the images for the back button, sets the image view,
     * and adds mouse event handlers to change the image when the mouse enters and exits the button.
     */
    private void setUpBackButton() {
        // Load the images for the back button.
        Image img = new Image(getClass().getResource("/images/back.png").toExternalForm());
        Image imgHover = new Image(getClass().getResource("/images/backHover.png").toExternalForm());

        // Create an ImageView and set it as the graphic of the back button.
        ImageView view = new ImageView(img);
        view.setFitHeight(20);
        view.setPreserveRatio(true);
        backButton.setGraphic(view);

        // Add a mouse event handler to change the image when the mouse enters the button.
        backButton.setOnMouseEntered(event -> {
            view.setImage(imgHover);
            view.setOpacity(0.8);
        });

        // Add a mouse event handler to change the image when the mouse exits the button.
        backButton.setOnMouseExited(event -> {
            view.setImage(img);
            view.setOpacity(1);
        });
    }

    /**
     * Sets the YAML file path and loads the YAML file.
     * This method is called from Containers Panel and
     * sets the YAML file path to the provided path,
     * and then calls the loadYamlFile method to load the YAML file.
     *
     * @param yamlFilePath The path of the YAML file.
     */
    public void setYamlFilePath(String yamlFilePath) {
        // Set the YAML file path
        this.yamlFilePath = yamlFilePath;
        // Load the YAML file
        loadYamlFile();
    }

    /**
     * Loads the YAML file content into the text area.
     * This method reads the YAML file, converts it to a string, and sets it as the text of the YAML text area.
     * It also extracts the file name from the YAML file path and sets it as the text of the fileNameLabel.
     * If an error occurs while reading the YAML file, it prints the error message.
     */
    private void loadYamlFile() {
        try {
            // Read the YAML file and convert it to a string
            String yamlContent = new String(Files.readAllBytes(Paths.get(yamlFilePath)));

            // Set the YAML file content as the text of the YAML text area
            yamlContentArea.setText(yamlContent);

            // Extract the file name from the YAML file path
            String fileName = Paths.get(yamlFilePath).getFileName().toString();

            // Set the file name as the text of the fileNameLabel
            fileNameLabel.setText(fileName);
        } catch (Exception e) {
            // Print the error message if an error occurs while reading the YAML file
            logger.error("Error reading YAML file: " + e.getMessage());
        }
    }

    /**
     * This method creates a new ProcessBuilder with the command to start the Docker Compose file.
     * It then starts the process and waits for it to finish.
     * If the process finishes with an exit code of 0, it shows user a notification.
     *
     * @throws IOException If an error occurs while starting the process.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to finish.
     */
    public void startDockerCompose() throws IOException, InterruptedException {
        // Create a new ProcessBuilder with the command to start the Docker Compose file.
        ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f", yamlFilePath, "up", "-d");

        // Start the process.
        Process process = processBuilder.start();

        // Wait for the process to finish.
        int exitCode = process.waitFor();

        // If the process finishes with an exit code of 0, show a notification.
        if (exitCode == 0) {
            // Extract the file name from the YAML file path
            String fileName = Paths.get(yamlFilePath).getFileName().toString();
            showNotification(fileName + " started", "The file " + fileName + " has been started");
        }
    }

    /**
     * This method creates a new ProcessBuilder with the command to stop the Docker Compose file.
     * It then starts the process and waits for it to finish.
     * If the process finishes with an exit code of 0, it shows user a notification.
     *
     * @throws IOException If an error occurs while starting the process.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to finish.
     */
    public void stopDockerCompose() throws IOException, InterruptedException {
        // Create a new ProcessBuilder with the command to stop the Docker Compose file.
        ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f", yamlFilePath, "stop");

        // Start the process.
        Process process = processBuilder.start();

        // Wait for the process to finish.
        int exitCode = process.waitFor();

        // If the process finishes with an exit code of 0, show a notification.
        if (exitCode == 0) {
            // Extract the file name from the YAML file path
            String fileName = Paths.get(yamlFilePath).getFileName().toString();
            showNotification(fileName + " stopped", "The Docker Compose file " + fileName + " has been stopped");
        }
    }

    /**
     * This method creates a new Yaml object and attempts to load the YAML file.
     * If the file is valid, it sets the text of the validateButton to "Valid", changes its color to green, and disables it.
     * If the file is not valid, it sets the text of the validateButton to "Sorry not valid", changes its color to red, and disables it.
     * After 2 seconds, it resets the validateButton to its original state.
     */
    public void validateYaml() {
        try {
            // Create a new Yaml object and attempt to load the YAML file.
            Yaml yaml = new Yaml();
            yaml.load(new FileInputStream(yamlFilePath));

            // If the file is valid.
            validateButton.setText("Valid");
            validateButton.setStyle("-fx-text-fill: white; -fx-background-color: #93C572;");
            validateButton.setDisable(true);
        } catch (Exception e) {
            // If the file is not valid.
            validateButton.setText("Sorry not valid");
            validateButton.setStyle("-fx-text-fill: white; -fx-background-color: #EC5F47;");
            validateButton.setDisable(true);
        }

        // After 2 seconds, reset the validateButton to its original state.
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            validateButton.setStyle(null);
            validateButton.setDisable(false);
            validateButton.setText("Validate");
        });
        pause.play();
    }

    /**
     * This method retrieves the current content of the YAML text area,
     * converts it to bytes, and writes it to the YAML file,
     * replacing the existing content of the file.
     * If the content is successfully saved, it updates the savedLabel to "Saved".
     */
    public void saveYaml() {
        try {
            // Retrieve the current content of the YAML text area.
            String yamlContent = yamlContentArea.getText();

            // Convert the content to bytes and write it to the YAML file.
            Files.write(Paths.get(yamlFilePath), yamlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            // Update the savedLabel to "Saved".
            savedLabel.setText("Saved");
        } catch (Exception e) {
            // Print the error message if an error occurs while saving the content.
            System.out.println("Error saving YAML file: " + e.getMessage());
        }
    }

    /**
     * Toggles between showing the YAML content and the configuration of the YAML file.
     * If the configuration is currently being shown, it loads the YAML file content back into the text area.
     * If the YAML content is currently being shown,
     * it reads the YAML file, parses it into a Map,
     * and displays the configuration in the text area.
     * The method also updates the text of the showConfigButton to reflect the current state.
     */
    public void showConfig() {
        // Check if the configuration is currently being shown.
        if (!isShowingConfig) {
            try {
                yamlContentArea.clear();
                // Create a new Yaml object to parse the YAML file.
                Yaml yaml = new Yaml();

                // Load the YAML file into a Map.
                Map<String, Object> yamlMap = yaml.load(new FileInputStream(yamlFilePath));

                // Append the configuration to the text area.
                yamlContentArea.appendText("Config:\n");
                for (Map.Entry<String, Object> entry : yamlMap.entrySet()) {
                    yamlContentArea.appendText(entry.getKey() + ": " + entry.getValue().toString() + "\n");
                }

                // Update the state and the button text.
                isShowingConfig = true;
                showConfigButton.setText("Show YAML");
            } catch (Exception e) {
                System.out.println("Error reading YAML file: " + e.getMessage());
            }
        } else {
            // Load the YAML file content back into the text area.
            loadYamlFile();
            // Update the state and the button text.
            isShowingConfig = false;
            showConfigButton.setText("Show Config");
        }
    }

    /**
     * Displays a notification with the given title and content.
     * It helps us keep user informed about events that occur in .yaml file.
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

            // Create a VBox to hold the title and content Labels and style it with our brand colors.
            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #f14246; -fx-padding: 10px; -fx-border-color: #525252; -fx-border-width: 1px;");

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
        // Change the scene to the Volumes scene.
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    /**
     * Changes the current scene to the User scene.
     * This method loads the FXML file for the User scene, sets it as the root of the current stage,
     * and displays the new scene. It also passes the name of the current scene to the UserController.
     * This method is typically triggered by a button click or similar action event.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeToUserScene(ActionEvent actionEvent) throws IOException {
        // Create a new FXMLLoader for the User scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userScene.fxml"));
        try {
            // Load the FXML file
            root = loader.load();
        } catch (IOException e) {
            // If an error occurs while loading the FXML file, throw a RuntimeException
            throw new RuntimeException(e);
        }
        // Get the controller for the User scene
        UserController userController = loader.getController();
        // Pass the name of the current scene to the UserController
        userController.onUserSceneLoad("composeScene.fxml");
        // Get the current stage
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        // Set the User scene as the root of the stage
        stage.getScene().setRoot(root);
        // Display the new scene
        stage.show();
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
}