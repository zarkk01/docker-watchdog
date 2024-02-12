package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.aueb.dmst.dockerWatchdog.gui.models.DeploymentScene;
import gr.aueb.dmst.dockerWatchdog.gui.models.PodScene;
import gr.aueb.dmst.dockerWatchdog.gui.models.ServiceScene;
import gr.aueb.dmst.dockerWatchdog.gui.models.StatefulSetScene;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Config;


/**
 * FX Controller for the Kubernetes scene.
 * Handles the interaction between the user and the UI and displays all
 * appropriate information about Kubernetes Cluster like Pods, Deployments, StatefulSets and
 * Services.
 */
public class KubernetesController implements Initializable {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(VolumesController.class);

    // Stage and Parent for the scene
    private Stage stage;
    private Parent root;

    // Table columns for the Pods table
    @FXML
    private TableColumn<PodScene, String> podNameColumn;
    @FXML
    private TableColumn<PodScene, String> podNamespaceColumn;
    @FXML
    private TableColumn<PodScene, String> podStatusColumn;
    @FXML
    private TableView<PodScene> podsTableView;

    // Table columns for the Deployments table
    @FXML
    private TableColumn<DeploymentScene, String> deploymentNameColumn;
    @FXML
    private TableColumn<DeploymentScene, String> deploymentNamespaceColumn;
    @FXML
    TableView<DeploymentScene> deploymentsTableView;

    // Table columns for the StatefulSets table
    @FXML
    private TableColumn<StatefulSetScene, String> statefulSetNameColumn;
    @FXML
    private TableColumn<StatefulSetScene, String> statefulSetNamespaceColumn;
    @FXML
    private TableView<StatefulSetScene> statefulSetsTableView;

    // Table columns for the Services table
    @FXML
    private TableColumn<ServiceScene, String> serviceNameColumn;
    @FXML
    private TableColumn<ServiceScene, String> serviceNamespaceColumn;
    @FXML
    private TableView<ServiceScene> servicesTableView;

    // Buttons for the sidebar so to change scenes
    @FXML
    private Button containersButton;
    @FXML
    private Button imagesButton;
    @FXML
    private Button graphicsButton;
    @FXML
    private Button volumesButton;
    @FXML
    public ImageView watchdogImage;

    @FXML
    private HBox topBar;
    @FXML
    private VBox sideBar;
    @FXML
    private Text kubernetesHead;
    @FXML
    private Label podsHead;
    @FXML
    private Label deploymentsHead;
    @FXML
    private Label statefulSetsHead;
    @FXML
    private Label servicesHead;

    @FXML
    private ScrollPane scrollPane;

    /**
     * Initializes the KubernetesController.
     * This method is called after all @FXML annotated members have been injected.
     * It's the main entry point for the controller.
     * It sets up the Kubernetes API client, initializes the hover effect
     * for the sidebar images, and populates the Pods, Deployments,
     * StatefulSets, and Services tables. It handles any ApiException or IOException
     * that may occur during the setup and population process by printing the stack trace.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up the shadows for the components
            setUpShadows();

            // Set up the hover effect for the sidebar images
            hoveredSideBarImages();

            // Set up the Kubernetes API client
            setUpApiClient();

            // Populate the tables with kubernetes data
            populatePodsTable();
            populateDeploymentsTable();
            populateStatefulSetsTable();
            populateServicesTable();

            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);



            // Install funny tooltip on watchdog imageView
            setUpWoofTooltip();
        } catch (ApiException | IOException e) {
            logger.error(e);
        }
    }

    /**
     * Sets up the drop shadow effect for various components in the scene.
     * This method creates a new DropShadow effect and applies it to the kubernetesHead, topBar, sideBar, podsTableView, podsHead, deploymentsTableView, deploymentsHead, statefulSetsHead, statefulSetsTableView, servicesHead, and servicesTableView.
     * The radius of the shadow is set to 7.5 and the color is set to a semi-transparent black.
     */
    private void setUpShadows() {
        // Set up drop shadow effect for the components
        DropShadow shadow = new DropShadow();
        shadow.setRadius(7.5);
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        kubernetesHead.setEffect(shadow);
        sideBar.setEffect(shadow);
        podsTableView.setEffect(shadow);
        podsHead.setEffect(shadow);
        deploymentsTableView.setEffect(shadow);
        deploymentsHead.setEffect(shadow);
        statefulSetsHead.setEffect(shadow);
        statefulSetsTableView.setEffect(shadow);
        servicesHead.setEffect(shadow);
        servicesTableView.setEffect(shadow);
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
        setHoverEffect(volumesButton, "/images/volumesGrey.png", "/images/volumes.png");
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
     * Sets up the Kubernetes API client.
     * This method initializes the Kubernetes API client using the default configuration.
     * The initialized client is then set as the default client for the Kubernetes API.
     *
     * @throws IOException If an error occurs while setting up the API client.
     */
    private void setUpApiClient() throws IOException {
        // Initialize the Kubernetes API client using the default configuration
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
    }

    /**
     * Populates the Pods table.
     * This method retrieves a list of all Pods from all namespaces using the Kubernetes API.
     * It then creates an ObservableList of PodScene objects from the retrieved Pods.
     * Finally, it sets the cell value factories for the
     * table columns and sets the items of the Pods table.
     *
     * @throws ApiException If an error occurs while retrieving the Pods.
     */
    private void populatePodsTable() throws ApiException {
        // Create a new CoreV1Api instance
        CoreV1Api api = new CoreV1Api();

        // Retrieve a list of all Pods from all namespaces
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null);

        // Create an ObservableList to hold PodScene objects
        ObservableList<PodScene> podsData = FXCollections.observableArrayList();
        for (V1Pod pod : list.getItems()) {
            podsData.add(new PodScene(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), pod.getStatus().getPhase()));
        }

        // Set the cell value factories for the table columns and set items of the Pods table
        podNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        podNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));
        podStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        podsTableView.setItems(podsData);

        // Set a placeholder for the Pods table when there are no Pods available
        podsTableView.setPlaceholder(new Label("No Pods available"));
    }

    /**
     * Populates the Deployments table.
     * This method retrieves a list of all Deployments from all namespaces using the Kubernetes API.
     * It then creates an ObservableList of DeploymentScene objects from the retrieved Deployments.
     * Finally, it sets the cell value factories for the table
     * columns and sets the items of the Deployments table.
     *
     * @throws ApiException If an error occurs while retrieving the Deployments.
     */
    private void populateDeploymentsTable() throws ApiException {
        // Create a new AppsV1Api instance
        AppsV1Api appsApi = new AppsV1Api();

        // Retrieve a list of all Deployments from all namespaces
        V1DeploymentList deploymentList = appsApi.listDeploymentForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);

        // Create an ObservableList to hold DeploymentScene objects
        ObservableList<DeploymentScene> deploymentsData = FXCollections.observableArrayList();
        for (V1Deployment deployment : deploymentList.getItems()) {
            deploymentsData.add(new DeploymentScene(deployment.getMetadata().getName(), deployment.getMetadata().getNamespace()));
        }

        // Set the cell value factories for the table columns and set items of the Deployments table
        deploymentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        deploymentNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));
        deploymentsTableView.setItems(deploymentsData);

        // Set a placeholder for the Deployments table when there are no Deployments available
        deploymentsTableView.setPlaceholder(new Label("No Deployments available"));
    }

    /**
     * Populates the StatefulSets table.
     * This method retrieves a list of all StatefulSets from all namespaces using the Kubernetes API.
     * It then creates an ObservableList of StatefulSetScene objects from the retrieved StatefulSets.
     * Finally, it sets the cell value factories for the table
     * columns and sets the items of the StatefulSets table.
     *
     * @throws ApiException If an error occurs while retrieving the StatefulSets.
     */
    private void populateStatefulSetsTable() throws ApiException {
        // Create a new AppsV1Api instance
        AppsV1Api appsApi = new AppsV1Api();

        // Retrieve a list of all StatefulSets from all namespaces
        V1StatefulSetList statefulSetList = appsApi.listStatefulSetForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null);

        // Create an ObservableList to hold StatefulSetScene objects
        ObservableList<StatefulSetScene> statefulSetsData = FXCollections.observableArrayList();
        for (V1StatefulSet statefulSet : statefulSetList.getItems()) {
            statefulSetsData.add(new StatefulSetScene(statefulSet.getMetadata().getName(), statefulSet.getMetadata().getNamespace()));
        }

        // Set the cell value factories for the table columns and set items of the StatefulSets table
        statefulSetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statefulSetNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));
        statefulSetsTableView.setItems(statefulSetsData);

        // Set a placeholder for the StatefulSets table when there are no StatefulSets available
        statefulSetsTableView.setPlaceholder(new Label("No StatefulSets available"));
    }

    /**
     * Populates the Services table.
     * This method retrieves a list of all Services from all namespaces using the Kubernetes API.
     * It then creates an ObservableList of ServiceScene
     * objects from the retrieved Services.
     * Finally, it sets the cell value factories for the table columns
     * and sets the items of the Services table.
     *
     * @throws ApiException If an error occurs while retrieving the Services.
     */
    private void populateServicesTable() throws ApiException {
        // Create a new CoreV1Api instance
        CoreV1Api api = new CoreV1Api();

        // Retrieve a list of all Services from all namespaces
        V1ServiceList serviceList = api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null, null, null);

        // Create an ObservableList to hold ServiceScene objects
        ObservableList<ServiceScene> servicesData = FXCollections.observableArrayList();
        for (V1Service service : serviceList.getItems()) {
            servicesData.add(new ServiceScene(service.getMetadata().getName(), service.getMetadata().getNamespace()));
        }

        // Set the cell value factories for the table columns and set items of the Services table
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        serviceNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));
        servicesTableView.setItems(servicesData);

        // Set a placeholder for the Services table when there are no Services available
        servicesTableView.setPlaceholder(new Label("No Services available"));
    }

    /**
     * Sets up the tooltip for the watchdog image.
     * This method creates a new Tooltip and sets it to be displayed when the mouse hovers over the watchdog image.
     * The tooltip is set to show after a delay of 20 milliseconds.
     */
    private void setUpWoofTooltip() {
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage,woof);
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
        // Load the Volumes scene and refresh the volumes
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/volumesScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();

        // Change the scene to the Volumes scene
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
        userController.onUserSceneLoad( "containersScene.fxml");
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}
