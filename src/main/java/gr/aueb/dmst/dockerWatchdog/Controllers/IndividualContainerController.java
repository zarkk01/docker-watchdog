package gr.aueb.dmst.dockerWatchdog.Controllers;

import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;
import static gr.aueb.dmst.dockerWatchdog.Main.dockerClient;

public class IndividualContainerController {

    @FXML
    private SplitPane infoCard;

    @FXML
    private Text headTextContainer;
    @FXML
    private Label containerIdLabel;
    @FXML
    private Label containerNameLabel;
    @FXML
    private Label containerStatusLabel;
    @FXML
    private Label containerImageLabel;

    private InstanceScene instanceScene;
    private Stage stage;
    private Parent root;
    @FXML
    private TextArea textArea;


    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "volumesScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    public void onInstanceDoubleClick(InstanceScene instance) {
        this.instanceScene = instance;
        headTextContainer.setText("Container: " + instance.getName());
        containerIdLabel.setText("ID: " + instance.getId());
        containerNameLabel.setText("Name: " + instance.getName());
        containerStatusLabel.setText("Status: " + instance.getStatus());
        containerImageLabel.setText("Image: " + instance.getImage());
        dockerClient = DockerClientBuilder.getInstance().build();


        // Specify container ID or name
        String containerId = instance.getId();

        // Create LogContainerCmd
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true);

        // Execute the command and update the TextArea with each log frame
        dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        // Process each log frame
                        String logLine = item.toString();

                        // Update the TextArea on the JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> {
                            textArea.appendText(logLine + "\n");
                        });
                    }
                });
        infoCard.setVisible(true);
    }

    public void removeContainer(ActionEvent actionEvent) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/delete"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        changeScene(actionEvent, "containersScene.fxml");
    }

    public void pauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/pause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        this.instanceScene.setStatus("Paused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void unpauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/unpause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        this.instanceScene.setStatus("Unpaused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void renameContainer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Container");
        dialog.setHeaderText("Enter the new name for the container:");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName == null || newName.trim().isEmpty()) {
                return;
            }

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/rename?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8)))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                client.send(request, HttpResponse.BodyHandlers.ofString());

                // Update the container name label and the instanceScene object
                containerNameLabel.setText("Name: " + newName);
                this.instanceScene.setName(newName);
                headTextContainer.setText("Container: " + newName);
            } catch (IOException | InterruptedException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public void startContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        this.instanceScene.setStatus("running");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void stopContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
        this.instanceScene.setStatus("exited");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void restartContainer() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Restarting the container with ID " + this.instanceScene.getId() + "...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/restart"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}