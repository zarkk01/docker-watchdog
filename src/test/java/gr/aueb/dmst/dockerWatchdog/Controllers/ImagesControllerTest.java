package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasTableCell;

public class ImagesControllerTest extends ApplicationTest {

    private ImagesController imagesController;

    @Override
    public void start(Stage stage) throws IOException {
        imagesController = new ImagesController();
    }

    @Test
    void testChangeToContainersScene() throws IOException {
        // Assuming you have a button with ID "containersButton" that triggers this method
        clickOn("#containersButton");

        // Verify that the scene has changed to "containersScene.fxml"
        verifyThat(imagesController.getScene().getRoot(), hasFxml("containersScene.fxml"));
    }

    @Test
    void testChangeToGraphicsScene() throws IOException {
        // Assuming you have a button with ID "graphicsButton" that triggers this method
        clickOn("#graphicsButton");

        // Verify that the scene has changed to "graphicsScene.fxml"
        verifyThat(imagesController.getScene().getRoot(), hasFxml("graphicsScene.fxml"));
    }

    @Test
    void testChangeToVolumesScene() throws IOException {
        // Assuming you have a button with ID "volumesButton" that triggers this method
        clickOn("#volumesButton");

        // Verify that the scene has changed to "volumesScene.fxml"
        verifyThat(imagesController.getScene().getRoot(), hasFxml("volumesScene.fxml"));
    }

    @Test
    void testChangeToKubernetesScene() throws IOException {
        // Assuming you have a button with ID "kubernetesButton" that triggers this method
        clickOn("#kubernetesButton");

        // Verify that the scene has changed to "kubernetesScene.fxml"
        verifyThat(imagesController.getScene().getRoot(), hasFxml("kubernetesScene.fxml"));
    }

    @Test
    void testRefreshImages() {
        // Mock the client and response
        client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.okJson("[{\"id\":\"123\",\"name\":\"image1\",\"size\":1024,\"status\":\"In use\"}]"));

        // Call the method
        imagesController.refreshImages();

        // Verify that the table contains the expected data
        verifyThat("#imagesTableView", hasTableCell("123", 0, 0));
        verifyThat("#imagesTableView", hasTableCell("image1", 0, 1));
        verifyThat("#imagesTableView", hasTableCell("1024", 0, 3));
        verifyThat("#imagesTableView", hasTableCell("In use", 0, 2));
    }

    @Test
    void testCreateContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock the client and response
        client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok("Success"));

        // Call the method
        imagesController.createContainer(new ImageScene("123", "image1", 1024L, "In use"));

        // Verify that the notification is shown
        // Adjust the verification based on your notification implementation
        // For example, you may want to verify that the notification label contains "Container created successfully"
        // verifyThat("#notificationBox", hasText("Container created successfully"));
    }

    @Test
    void testStartAllContainers() throws Exception {
        // Mock the client and response
        client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok("Success"));

        // Call the method
        imagesController.startAllContainers("image1");

        // Verify that the notification is shown
        // Adjust the verification based on your notification implementation
        // For example, you may want to verify that the notification label contains "All containers started successfully"
        // verifyThat("#notificationBox", hasText("All containers started successfully"));
    }

    @Test
    void testStopAllContainers() throws Exception {
        // Mock the client and response
        client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok("Success"));

        // Call the method
        imagesController.stopAllContainers("image1");

        // Verify that the notification is shown
        // Adjust the verification based on your notification implementation
        // For example, you may want to verify that the notification label contains "All containers stopped successfully"
        // verifyThat("#notificationBox", hasText("All containers stopped successfully"));
    }
}
