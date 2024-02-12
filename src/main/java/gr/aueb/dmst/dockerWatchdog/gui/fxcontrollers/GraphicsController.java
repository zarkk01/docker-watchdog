package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import gr.aueb.dmst.dockerWatchdog.gui.models.InstanceScene;
import gr.aueb.dmst.dockerWatchdog.exceptions.ChartException;
import static gr.aueb.dmst.dockerWatchdog.gui.GuiApplication.client;


/**
 * The GraphicsController class is an FX Controller responsible for managing the Graphics Panel in the application.
 * It is used to display the information of the Docker Cluster in a playful form of charts.
 * It also provides methods for retrieving all instances of the application
 * and setting hover effects for the sidebar images.
 * The class uses the WATCHDOG REST API to communicate with the backend
 * and send requests for information so to update the charts.
 */
public class GraphicsController implements Initializable {

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(GraphicsController.class);

    private Stage stage;
    private Parent root;

    private LocalDateTime currentTime;

    @FXML
    private LineChart<String, Number> cpuChart;
    private XYChart.Series<String, Number> cpuSeries;

    @FXML
    private BarChart<String, Number> pidsChart;
    private XYChart.Series<String, Number> pidsSeries;

    @FXML
    private LineChart<String, Number> memoryChart;
    private XYChart.Series<String, Number> memorySeries;

    @FXML
    private VBox sideBar;
    @FXML
    private Button userButton;
    @FXML
    private Text graphicsHead;

    @FXML
    private  Button containersButton;
    @FXML
    private Button imagesButton;
    @FXML
    private Button kubernetesButton;
    @FXML
    private Button volumesButton;
    @FXML
    private ImageView watchdogImage;

    @FXML
    private PieChart pieChartImages;

    private Timeline timelineForCpuMemory;
    private Timeline timelineForPidsPie;

    /**
     * This method is called after all @FXML annotated members have been injected and
     * set up the environment for charts. After starting them,
     * it sets a timeline to update the CPU and Memory charts every 4 seconds
     * and the PIDs chart and Allocation pie every 30 seconds.
     * Finally, it sets a hover effect for the sidebar images.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up shadows for the components.
            setUpShadows();

            // Start the charts.
            startCharts();

            // Update the CPU and Memory charts.
            updateCpuMemoryCharts();
            // Update the PIDs chart and the Pie chart.
            updatePidsChart();
            updatePieChart();

            // Set the user button text to "Log in" if the user is not logged in, or "Logged in" if the user is logged in.
            if (UserController.token == null) {
                // If the user is not logged in, set the user button text to "Log in".
                userButton.setText("Log in");
            } else {
                // If the user is logged in, set the user button text to "Logged in".
                userButton.setText("Logged in");
            }
        } catch (Exception e) {
            // Log any errors that occur.
            logger.error(e.getMessage());
        }

        // Set a timeline to update the CPU and Memory charts every 4 seconds.
        timelineForCpuMemory = new Timeline(new KeyFrame(Duration.seconds(4), event -> {
            try {
                // Update the CPU and Memory charts.
                updateCpuMemoryCharts();
            } catch (Exception e) {
                // Log any errors that occur.
                logger.error(e.getMessage());
            }
        }));
        timelineForCpuMemory.setCycleCount(Timeline.INDEFINITE);
        timelineForCpuMemory.play();

        // Set a timeline to update the PIDs chart and our Allocation Pie every 30 seconds.
        timelineForPidsPie = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            try {
                updatePidsChart();
                updatePieChart();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }));
        timelineForPidsPie.setCycleCount(Timeline.INDEFINITE);
        timelineForPidsPie.play();

        // Install funny tooltip on watchdog logo.
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage, woof);

        // Set a hover effect for the sidebar images.
        hoveredSideBarImages();
    }

    /**
     * Sets up shadows for the components.
     * This method applies a drop shadow effect to the components in the Graphics Panel header,top bar and sidebar, along
     * with the charts, to make them stand out and give a 3D effect.
     */
    private void setUpShadows() {
        // Set up drop shadow effect for the components.
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        cpuChart.setEffect(shadow);
        memoryChart.setEffect(shadow);
        pidsChart.setEffect(shadow);
        pieChartImages.setEffect(shadow);
        sideBar.setEffect(shadow);
        graphicsHead.setEffect(shadow);
    }

    /**
     * This method initializes the CPU, Memory, PIDs, and Pie charts.
     * Each chart is represented by a Series object, which is added to the corresponding chart.
     * If an error occurs while initializing a chart, a ChartException is thrown.
     *
     * @throws ChartException If an error occurs while initializing a chart.
     */
    public void startCharts() throws ChartException {
        try {
            // Initialize the CPU chart.
            cpuSeries = new XYChart.Series<>();
            cpuChart.getData().add(cpuSeries);
        } catch (Exception e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("CPU chart");
        }

        try {
            // Initialize the Memory chart.
            memorySeries = new XYChart.Series<>();
            memoryChart.getData().add(memorySeries);
        } catch (Exception e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("Memory chart");
        }

        try {
            // Initialize the PIDs chart.
            pidsSeries = new XYChart.Series<>();
            pidsChart.getData().add(pidsSeries);
        } catch (Exception e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("PIDs chart");
        }

        try {
            // Initialize the Pie chart.
            startPieChart();
        } catch (Exception e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("Pie chart");
        }
    }

    /**
     * Pie chart initialization needs to be done separately from the other charts because it requires a different approach.
     * This method retrieves all instances of the application and counts the number of instances for each image.
     * Each image and its count are represented by a PieChart.Data object, which is added to the Pie chart.
     * The name of each PieChart.Data object is bound to the name and count of the image.
     * If an error occurs while initializing the Pie chart, an Exception is thrown.
     *
     * @throws Exception If an error occurs while initializing the Pie chart.
     */
    public void startPieChart() throws Exception {
        // Retrieve all instances of the application.
        List<InstanceScene> instances = getAllInstances();

        // Create a HashMap to store the images and their counts.
        HashMap<String, Integer> images = new HashMap<>();

        // Iterate through each instance and count the number of instances for each image.
        for (InstanceScene instance : instances) {
            String image = instance.getImage();

            if (images.containsKey(image)) {
                images.put(image, images.get(image) + 1);
            } else {
                images.put(image, 1);
            }
        }

        // Loop through each image in the HashMap.
        for (String image : images.keySet()) {
            // Create a new PieChart.Data object with the image and its count and add it to the Pie chart.
            pieChartImages.getData().add(new PieChart.Data(image, images.get(image)));
        }

        // Bind the name of each PieChart.Data object to the name and count of the image.
        pieChartImages.getData().forEach(data -> {
            data.nameProperty().bind(
                    Bindings.concat(
                            data.getName(), " : ", (int) data.pieValueProperty().get()
                    )
            );
        });
    }

    /**
     * Updates the CPU and Memory charts.
     * This method retrieves all instances of the application and calculates the total CPU and Memory usage.
     * The total CPU and Memory usage are then added to the CPU and Memory charts respectively.
     * If an error occurs while updating a chart, a ChartException is thrown.
     *
     * @throws Exception If an error occurs while updating a chart.
     */
    public void updateCpuMemoryCharts() throws Exception {
        // Retrieve all instances of the application from database using REST WATCHDOG API.
        List<InstanceScene> instances = getAllInstances();

        // Get the current time and format it.
        currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Initialize the total CPU and Memory usage.
        double totalCpuUsage = 0;
        double totalMemoryUsage = 0;

        try {
            // Calculate the total CPU usage.
            for (InstanceScene instance : instances) {
                String cpuUsage = instance.getCpuUsage();
                Double num = Double.parseDouble(cpuUsage);
                totalCpuUsage += num;
            }
            // If the total CPU usage is more than 40%, update the CPU and Memory charts again
            // because something might have gone wrong with the previous measurement.
            if (totalCpuUsage * 100 > 40) {
                updateCpuMemoryCharts();
            } else {
                // Add the total CPU usage to the CPU chart.
                cpuSeries.getData().add(new XYChart.Data<>(formattedTime, totalCpuUsage * 100));
            }
        } catch (Exception e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("CPU chart");
        }

        try {
            // Calculate the total Memory usage.
            for (InstanceScene instance : instances) {
                Double num = Double.parseDouble(instance.getMemoryUsage());
                totalMemoryUsage += num;
            }
            // Add the total Memory usage to the Memory chart.
            memorySeries.getData().add(new XYChart.Data<>(formattedTime, totalMemoryUsage));
        } catch (NumberFormatException e) {
            // If an error occurs, throw a ChartException.
            throw new ChartException("Memory chart");
        }
    }
    /**
     * This method retrieves all instances of the application and adds the number of PIDs for each instance to the PIDs chart.
     * Each instance and its number of PIDs are represented by a XYChart.Data object, which is added to the PIDs chart.
     * If an error occurs while updating the PIDs chart, a ChartException is thrown.
     *
     * @throws ChartException If an error occurs while updating the PIDs chart.
     */
    public void updatePidsChart() throws ChartException {
        try {
            // Retrieve all instances of the application.
            List<InstanceScene> instances = getAllInstances();

            // Clear the existing data in the PIDs chart.
            pidsChart.getData().clear();

            // Loop through each instance.
            for (InstanceScene instance : instances) {
                // Get a simpler name for the instance if the name is too long.
                String simplerName;
                if(instance.getName().length() > 10){
                    simplerName = instance.getName().substring(0, 8) + "...";
                } else {
                    simplerName = instance.getName();
                }

                // Add the number of PIDs for the instance to the PIDs chart.
                pidsSeries.getData().add(new XYChart.Data<>(simplerName, Long.parseLong(instance.getPids())));
            }

            // Add the series to the PIDs chart.
            pidsChart.getData().add(pidsSeries);
        } catch (Exception e) {
            throw new ChartException("PIDs chart");
        }
    }

    /**
     * This method retrieves all instances of the application and counts the number of instances for each image.
     * Each image and its count are represented by a PieChart.Data object, which is updated in the Pie chart.
     * The name of each PieChart.Data object is bound to the name and count of the image.
     * If an error occurs while updating the Pie chart, an Exception is thrown.
     *
     * @throws ChartException If an error occurs while updating the Pie chart.
     */
    public void updatePieChart() throws Exception {
        // Retrieve all instances of the application.
        List<InstanceScene> instances = getAllInstances();

        try {
            // Create a HashMap to store the images and their counts.
            HashMap<String, Integer> images = new HashMap<>();

            // Loop through each instance and get the image of the instance.
            for (InstanceScene instance : instances) {
                String image = instance.getImage();

                // If the image is already in the HashMap, increment its count.
                // Otherwise, add the image to the HashMap with a count of 1.
                if (images.containsKey(image)) {
                    images.put(image, images.get(image) + 1);
                } else {
                    images.put(image, 1);
                }
            }

            // Clear the existing data in the Pie chart.
            pieChartImages.getData().clear();

            // Loop through each image in the HashMap and create a new PieChart.Data object
            for (String image : images.keySet()) {
                pieChartImages.getData().add(new PieChart.Data(image, images.get(image)));
            }

            // Bind the name of each PieChart.Data object to the name and count of the image.
            pieChartImages.getData().forEach(data -> {
                data.nameProperty().bind(
                        Bindings.concat(
                                data.getName(), " : ", (int) data.pieValueProperty().get()
                        )
                );
            });
        } catch (Exception e) {
            throw new ChartException("Pie chart");
        }
    }

    /**
     * Retrieves all instances of the application.
     * This method sends a GET request to the WATCHDOG REST API to fetch all instances.
     * Each instance is then converted into an InstanceScene object and added to a list.
     * The list of all instances is returned at the end.
     *
     * @return A list of all instances in the form of InstanceScene objects.
     * @throws Exception If an error occurs while sending the GET request or processing the response.
     */
    public List<InstanceScene> getAllInstances() throws Exception {
        // Create a new HTTP request to the WATCHDOG REST API.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();

        // Send the HTTP request and get the response.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Convert the response body to a JSON array.
        JSONArray jsonArray = new JSONArray(response.body());

        // Create a new list to store the instances.
        List<InstanceScene> instances = new ArrayList<>();

        // Loop through each object in the JSON array.
        for (int i = 0; i < jsonArray.length(); i++) {
            // Get the current JSON object.
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Extract the data from the JSON object.
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String image = jsonObject.getString("image");
            String status = jsonObject.getString("status");
            Long memoryUsageL = jsonObject.getLong("memoryUsage");
            String memoryUsage = String.valueOf(memoryUsageL);
            Long pidsL = jsonObject.getLong("pids");
            String pids = String.valueOf(pidsL);
            Double cpuUsageD = jsonObject.getDouble("cpuUsage");
            String cpuUsage = String.valueOf(cpuUsageD);
            Double blockID = jsonObject.getDouble("blockI");
            double roundedI = Math.round(blockID * 10.0) / 10.0;
            String blockI = roundedI + "B";
            Double blockOD = jsonObject.getDouble("blockO");
            double rounded0 = Math.round(blockOD * 10.0) / 10.0;
            String blockO = rounded0 + "B";

            // Create a new InstanceScene object with the extracted data and add it to the list.
            instances.add(new InstanceScene(id, name, image , status, memoryUsage, pids, cpuUsage, blockI, blockO, null, null, null, null, false));
        }

        // Return the list of instances.
        return instances;
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
        setHoverEffect(imagesButton, "/images/imageGrey.png", "/images/image.png");
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
        // Get the controller of the Volumes scene and refresh the volumes so to be ready before displaying.
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();

        // Change the scene to the Volumes scene.
        changeScene(actionEvent, "volumesScene.fxml");
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
     * Changes the current scene to the User scene and passes the name of the current scene to the UserController.
     * This method loads the FXML file for the User scene, sets it as the root of the current stage,
     * and displays the new scene. It also passes the name of the current scene to the UserController.
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
        userController.onUserSceneLoad("graphicsScene.fxml");
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
     * It also stops the timelines to keep Watchdog clean and reduce lag.
     *
     * @param actionEvent The event that triggered the scene change.
     * @param fxmlFile The name of the FXML file for the new scene.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        // Stop the timelines to reduce lag.
        stopTimeline(timelineForPidsPie);
        stopTimeline(timelineForCpuMemory);
        // Load the FXML file for the new scene.
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));

        // Get the current stage.
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        // Set the new scene as the root of the stage and display it.
        stage.getScene().setRoot(root);
        stage.show();
    }

    /**
     * Stops the Timeline if it is not null.
     * This method is used to stop the Timeline when the user leaves the scene.
     * Stopping the Timeline can help to reduce lag in the program.
     */
    public void stopTimeline(Timeline timeline) {
        // Check if the timeline is not null
        if (timeline != null) {
            // If it's not null, stop the timeline
            timeline.stop();
        }
    }
}
