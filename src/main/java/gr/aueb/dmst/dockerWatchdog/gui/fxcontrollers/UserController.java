package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.text.View;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class UserController {

    private Stage stage;
    private Object root;

    @FXML
    private Label wrongUserPass;

    @FXML
    private Button backbutton;
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
    private String fromWhichScene;
    public static String token;

    public void onUserSceneLoad(String fromWhere) {
        this.fromWhichScene = fromWhere;
        System.out.println(token);
        if (token != null && !token.isEmpty()) {
            System.out.println("User is logged in");
            loggedInLabel.setVisible(true);
            hideForm();
        } else {
            System.out.println("User is not logged in");
            wrongUserPass.setVisible(false);
            passwordTextField.setVisible(true);
            loginButton.setVisible(true);
            usernameTextField.setVisible(true);
            loginToDockerhubLabel.setVisible(true);
        }

        InputStream imageStream = getClass().getResourceAsStream("/images/back.png");
        Image image = new Image(imageStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20); // adjust the height as needed
        imageView.setFitWidth(20); // adjust the width as needed
        backButton.setGraphic(imageView);
        backButton.setStyle("-fx-background-color: transparent;");

    }

    public void logIn() throws IOException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        token = ApiService.authenticateDockerHub(username, password);
        if (token != null && !token.isEmpty()) {
            loggedInLabel.setVisible(true);
            hideForm();
        } else {
            wrongUserPass.setVisible(true);
        }
    }

    public void hideForm() {
        wrongUserPass.setVisible(false);
        loginToDockerhubLabel.setVisible(false);
        passwordTextField.setVisible(false);
        loginButton.setVisible(false);
        usernameTextField.setVisible(false);
        loginPrompt.setVisible(false);
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        if(Objects.equals(fromWhichScene, "containersScene.fxml")) {
            changeScene(actionEvent, "containersScene.fxml");
        } else if ( Objects.equals(fromWhichScene, "imagesScene.fxml")) {
            changeScene(actionEvent, "imagesScene.fxml");
        } else if ( Objects.equals(fromWhichScene, "volumesScene.fxml")) {
            changeScene(actionEvent, "volumesScene.fxml");
        } else if ( Objects.equals(fromWhichScene, "graphicsScene.fxml")) {
            changeScene(actionEvent, "graphicsScene.fxml");
        } else if ( Objects.equals(fromWhichScene, "kubernetesScene.fxml")) {
            changeScene(actionEvent, "kubernetesScene.fxml");
        }
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
        changeScene(actionEvent, "kubernetesScene.fxml");
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
