package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class UserController {

    private Stage stage;
    private Object root;

    @FXML
    private Button backbutton;
    @FXML
    private Label loginToDockerhubLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Label loggedInLabel;
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
            passwordLabel.setVisible(true);
            passwordTextField.setVisible(true);
            loginButton.setVisible(true);
            usernameLabel.setVisible(true);
            usernameTextField.setVisible(true);
            loginToDockerhubLabel.setVisible(true);
        }
    }

    public void logIn() throws IOException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        token = ApiService.authenticateDockerHub(username, password);
        hideForm();
        loggedInLabel.setVisible(true);
    }

    public void hideForm() {
        loginToDockerhubLabel.setVisible(false);
        passwordLabel.setVisible(false);
        passwordTextField.setVisible(false);
        loginButton.setVisible(false);
        usernameLabel.setVisible(false);
        usernameTextField.setVisible(false);
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
