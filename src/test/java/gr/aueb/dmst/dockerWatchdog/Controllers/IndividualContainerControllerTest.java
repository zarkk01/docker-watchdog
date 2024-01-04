package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class IndividualContainerControllerTest extends ApplicationTest {

    private IndividualContainerController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FxToolkit.setupApplication(DesktopApp.class);
        controller = new IndividualContainerController();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        // Load the individual container scene before each test
        FxToolkit.setupFixture(() -> {
            try {
                controller.onInstanceDoubleClick(new InstanceScene("testId", "testName", "testImage", "testStatus",
                        1024L, 10L, 0.5, 100.0, 50.0, "testVolumes", "testSubnet", "testGateway", 24, false));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testChangeToContainersScene() {
        clickOn("#backButton"); // Assuming you have a button with this ID
        verifyThat("#containerIdLabel", hasText("ID : "));
    }

    @Test
    void testChangeToImagesScene() throws IOException {
        clickOn("#changeToImagesSceneButton"); // Assuming you have a button with this ID
        verifyThat(controller.getScene().getRoot(), hasFxml("imagesScene.fxml"));
    }

    @Test
    void testChangeToVolumesScene() throws IOException {
        clickOn("#changeToVolumesSceneButton"); // Assuming you have a button with this ID
        verifyThat(controller.getScene().getRoot(), hasFxml("volumesScene.fxml"));
    }

    @Test
    void testChangeToGraphicsScene() throws IOException {
        clickOn("#changeToGraphicsSceneButton"); // Assuming you have a button with this ID
        verifyThat(controller.getScene().getRoot(), hasFxml("graphicsScene.fxml"));
    }

    @Test
    void testChangeToKubernetesScene() throws IOException {
        clickOn("#changeToKubernetesSceneButton"); // Assuming you have a button with this ID
        verifyThat(controller.getScene().getRoot(), hasFxml("kubernetesScene.fxml"));
    }

    @Test
    void testRemoveContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock necessary dependencies and set up the scene for this test
        DesktopApp.client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok(""));

        clickOn("#removeButton"); // Assuming you have a button with this ID

        // Add verifications based on expected changes in the UI elements or navigation
        verifyThat(controller.getScene().getRoot(), hasFxml("containersScene.fxml"));
    }

    @Test
    void testPauseContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock necessary dependencies and set up the scene for this test
        DesktopApp.client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok(""));

        clickOn("#pauseButton"); // Assuming you have a button with this ID

        // Add verifications based on expected changes in the UI elements or notifications
        verifyThat("#containerStatusLabel", hasText("Status: Paused"));
    }

    @Test
    void testUnpauseContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock necessary dependencies and set up the scene for this test
        DesktopApp.client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok(""));

        clickOn("#unpauseButton"); // Assuming you have a button with this ID

        // Add verifications based on expected changes in the UI elements or notifications
        verifyThat("#containerStatusLabel", hasText("Status: Unpaused"));
    }

    @Test
    void testRenameContainer() {
        clickOn("#renameButton"); // Assuming you have a button with this ID
        verifyThat("#containerNameLabel", hasText("Name: testNewName"));
    }

    @Test
    void testStartContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock necessary dependencies and set up the scene for this test
        DesktopApp.client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok(""));

        clickOn("#startButton"); // Assuming you have a button with this ID

        // Add verifications based on expected changes in the UI elements or notifications
        verifyThat("#containerStatusLabel", hasText("Status: running"));
    }

    @Test
    void testStopContainer() throws IOException, InterruptedException, URISyntaxException {
        // Mock necessary dependencies and set up the scene for this test
        DesktopApp.client = mock(HttpClient.class);
        when(client.send(any(), any())).thenReturn(HttpResponse.ok(""));

        clickOn("#stopButton"); // Assuming you have a button with this ID

        // Add verifications based on expected changes in the UI elements or notifications
        verifyThat("#containerStatusLabel", hasText("Status: exited"));
    }

   
