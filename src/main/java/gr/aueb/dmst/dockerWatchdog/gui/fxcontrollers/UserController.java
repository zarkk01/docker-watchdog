package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * UserController is a JavaFX controller class that handles user interactions with the User scene.
 * This includes logging in to Docker Hub, navigating back to the previous scene, and changing to other scenes.
 * It also holds the JWT token and username of the user that is logged in to Docker Hub.
 * These can be shared among all classes.
 */
public class UserController {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private Stage stage;
    private Object root;

    @FXML
    private Label wrongUserPass;
    @FXML
    private Pane underLinePane;
    @FXML
    private Pane loginForm;

    @FXML
    private Label loginToDockerhubLabel;
    @FXML
    private Button loginButton;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label loginPrompt;
    @FXML
    public Label loggedInLabel;
    @FXML
    private Button backButton;
    @FXML
    private Button dummyButton;
    private String fromWhichScene;

    // The token and name of the user that is logged in to Docker Hub.
    // This can be shared among all classes.
    public static String token;
    public static String name ;

    /**
     * Prepares the User scene when it is loaded.
     * This method sets the scene from which the User scene was loaded,
     * checks if the user is logged in, and updates the UI accordingly.
     * It also sets the image for the back button.
     *
     * @param fromWhere The name of the scene from which the User scene was loaded.
     */
    public void onUserSceneLoad(String fromWhere) {
        // Set the scene from which the User scene was loaded
        this.fromWhichScene = fromWhere;

        // Request focus for the dummy button to prevent the text field from getting focus
        Platform.runLater(() -> dummyButton.requestFocus());

        // Add a shadow effect to the loginForm Pane to make it more aggressive
        addShadowToLoginForm();

        // Set up the back button with the image and hover effect
        setupButton(backButton, new ImageView(), "back.png", "backHover.png", 20);

        // Check if the user is logged in
        if (token != null && !token.isEmpty()) {
            // Update the loggedInLabel with the user's name and make it visible
            loggedInLabel.setText("Logged in as " + name);
            loggedInLabel.setVisible(true);

            // Hide the login form
            hideForm();
        } else {
            // Make the login form visible
            wrongUserPass.setVisible(false);
            passwordTextField.setVisible(true);
            loginButton.setVisible(true);
            underLinePane.setVisible(true);
            usernameTextField.setVisible(true);
            loginToDockerhubLabel.setVisible(true);
        }

        // Load the image for the back button
        InputStream imageStream = getClass().getResourceAsStream("/images/back.png");
        Image image = new Image(imageStream);
        ImageView imageView = new ImageView(image);

        // Adjust the size of the image as needed
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
    }

    /**
     * Adds a shadow effect to the loginForm Pane.
     * This method creates a DropShadow effect with a specified radius, spread, offsets, and color.
     * The shadow is then applied to the loginForm Pane and makes it appear as if it is floating.
     */
    public void addShadowToLoginForm() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setSpread(0.5);

        // Set the horizontal and vertical offsets of the shadow to 0.0
        // so it is exactly behind the loginForm Pane in the center
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(0.0);

        // Set the color of the shadow to a semi-transparent black
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.5));

        // Finally, apply the shadow effect to the loginForm Pane
        loginForm.setEffect(shadow);
    }

    /**
     * Hides the login form.
     * This method sets the visibility of all components of the login form to false.
     * It is used to hide the login form when the user is already logged in.
     */
    public void hideForm() {
        // Hide the underline pane
        underLinePane.setVisible(false);

        // Hide the error message for wrong username or password
        wrongUserPass.setVisible(false);

        // Hide the label prompting the user to log in to Docker Hub
        loginToDockerhubLabel.setVisible(false);

        // Hide the password text field
        passwordTextField.setVisible(false);

        // Hide the login button
        loginButton.setVisible(false);

        // Hide the username text field
        usernameTextField.setVisible(false);

        // Hide the login prompt
        loginPrompt.setVisible(false);
    }

    /**
     * Sets up a button with an image and hover effect.
     * The image changes when the mouse enters and exits the button.
     * The button's graphic is set to an ImageView of the image.
     * It is used so to automate the process of setting up the hover effect for all 3 buttons.
     *
     * @param button The button to set up.
     * @param view The ImageView to set as the button's graphic.
     * @param imagePath The path to the image for the button.
     * @param hoverImagePath The path to the image for the button's hover effect.
     * @param fitHeight The height to fit the ImageView to.
     */
    private void setupButton(Button button, ImageView view, String imagePath, String hoverImagePath, double fitHeight) {
        // Load the image from the given path and set it to the ImageView.
        InputStream imgStream = getClass().getResourceAsStream("/images/" + imagePath);
        Image img = new Image(imgStream);
        view.setImage(img);

        // Fit the ImageView to the given height and preserve its ratio.
        view.setFitHeight(fitHeight);
        view.setPreserveRatio(true);
        button.setGraphic(view);

        // Load the hover image from the given path.
        InputStream imgHoverStream = getClass().getResourceAsStream("/images/" + hoverImagePath);
        Image imgHover = new Image(imgHoverStream);

        // Set the hover effect: when the mouse enters the button, change the image and reduce its opacity.
        button.setOnMouseEntered(event -> {
            view.setImage(imgHover);
            view.setOpacity(0.8);
        });

        // Remove the hover effect: when the mouse exits the button,
        // change the image back to the original and restore its opacity.
        button.setOnMouseExited(event -> {
            view.setImage(img);
            view.setOpacity(1);
        });
    }

    /**
     * Logs in the user to Docker Hub.
     * This method retrieves the username and password from the text fields,
     * authenticates the user with Docker Hub, and updates the UI accordingly.
     * If the authentication is successful, it hides the login form and displays the logged in label.
     * Also, if it was successfull it sends the user back to the previous scene after 1 second.
     * If the authentication fails, it displays an error message.
     *
     * @throws IOException if an I/O error occurs when sending the request
     */
    public void logIn(ActionEvent actionEvent) throws IOException {
        // Get the username and password from the text fields
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        // Authenticate the user with Docker Hub sending the username and password
        token = ApiService.authenticateDockerHub(username, password);

        // Check if the authentication was successful
        if (token != null && !token.isEmpty()) {
            // Make the logged in label visible
            loggedInLabel.setVisible(true);

            // If the username is longer than 7 characters, truncate it and add "..."
            if (username.length() > 7){
                name = username.substring(0, 6) + "...";
            } else {
                name = username;
            }

            // Hide the login form
            hideForm();

            // Create a ScheduledExecutorService so to make user go back after 1 seconds
            // Maybe there is a better way to do this but this is maybe the most efficient
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            // Schedule the goBack() method to be executed after 2 seconds
            executorService.schedule(() -> {
                Platform.runLater(() -> {
                    try {
                        // Go back to the previous scene
                        goBack(actionEvent);
                    } catch (IOException e) {
                        logger.error("Error occurred while going back to the previous scene", e);
                    }
                });
                // After 1 second
            }, 1, TimeUnit.SECONDS);
            // Shut down the executor service no reason to keep it running
            executorService.shutdown();
        } else {
            // If the log in proccess failed, display an error message
            wrongUserPass.setVisible(true);
        }
    }

    /**
     * Navigates back to the previous scene.
     * This method changes the current scene to the scene from which the User scene was loaded.
     * It uses the changeScene method to perform the scene change.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while loading the FXML file for the new scene.
     */
    public void goBack(ActionEvent actionEvent) throws IOException {
        // Change the current scene to the scene from which the User scene was loaded
        changeScene(actionEvent, fromWhichScene);
    }

    /**
     * Changes the current scene to a new scene.
     * This method loads the FXML file for the new scene,
     * sets it as the root of the current stage,
     * and displays the new scene. It is used to navigate between different scenes in the application.
     * It also stops the Timeline which keeps updating the CPU usage chart and prevents
     * the Timeline from running in the background when the user is not on the IndividualContainer scene.
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
        stage.getScene().setRoot((Parent) root);
        stage.show();
    }
}
