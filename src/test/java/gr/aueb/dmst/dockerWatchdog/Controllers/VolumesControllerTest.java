package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import gr.aueb.dmst.dockerWatchdog.Models.VolumeScene;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasItems;
import static org.testfx.matcher.control.TableViewMatchers.hasTableCell;

public class VolumesControllerTest extends ApplicationTest {

    private VolumesController controller;

    @Override
    public void start(Stage stage) throws Exception {
        // Set up the JavaFX application for testing
        FxToolkit.setupApplication(DesktopApp.class);
        // Initialize the controller instance
        controller = new VolumesController();
    }

    @BeforeEach
    public void beforeEach() throws TimeoutException {
        // Set up the JavaFX testing fixture before each test
        FxToolkit.setupFixture(() -> {
            // Initialize the Volumes controller
            controller.initialize(null, null);
        });
    }

    @Test
    void testInitialize() {
        // Verify that the volumes table view is initially empty
        verifyThat("#volumesTableView", hasItems(0));
    }

    @Test
    void testChangeToContainersScene() throws IOException {
        // Mock necessary methods or elements if needed
        // Verify the expected changes in the UI or navigation
        controller.changeToContainersScene(mock(ActionEvent.class));
        // Add assertions or verifications if needed
    }

    @Test
    void testChangeToImagesScene() throws IOException {
        // Mock necessary methods or elements if needed
        // Verify the expected changes in the UI or navigation
        controller.changeToImagesScene(mock(ActionEvent.class));
        // Add assertions or verifications if needed
    }

    @Test
    void testChangeToGraphicsScene() throws IOException {
        // Mock necessary methods or elements if needed
        // Verify the expected changes in the UI or navigation
        controller.changeToGraphicsScene(mock(ActionEvent.class));
        // Add assertions or verifications if needed
    }

    @Test
    void testChangeToKubernetesScene() throws IOException {
        // Mock necessary methods or elements if needed
        // Verify the expected changes in the UI or navigation
        controller.changeToKubernetesScene(mock(ActionEvent.class));
        // Add assertions or verifications if needed
    }

    @Test
    void testRefreshVolumes() throws Exception {
        // Mock the behavior of getAllVolumes method to return specific volumes
        when(controller.getAllVolumes()).thenReturn(List.of(new VolumeScene("Volume1", "Driver1", "/path/to/volume1", "Container1")));

        // Call the refreshVolumes method
        controller.refreshVolumes();

        // Verify that the table view is updated with the new volumes
        verifyThat("#volumesTableView", hasItems(1));
        verifyThat("#nameColumn", hasTableCell("Volume1"));
        verifyThat("#driverColumn", hasTableCell("Driver1"));
        verifyThat("#mountpointColumn", hasTableCell("/path/to/volume1"));
        verifyThat("#containerNamesUsingColumn", hasTableCell("Container1"));
    }
}
