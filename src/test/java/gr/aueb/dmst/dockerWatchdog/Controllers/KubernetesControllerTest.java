package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasTableCell;
import static org.testfx.matcher.control.TableViewMatchers.hasItems;

public class KubernetesControllerTest extends ApplicationTest {

    private KubernetesController controller;

    @Override
    public void start(Stage stage) throws Exception {
        // Set up the JavaFX application for testing
        FxToolkit.setupApplication(DesktopApp.class);
        // Initialize the Kubernetes controller
        controller = new KubernetesController();
    }

    @BeforeEach
    public void beforeEach() throws TimeoutException {
        // Set up the JavaFX testing fixture before each test
        FxToolkit.setupFixture(() -> {
            // Initialize the Kubernetes controller
            controller.initialize(null, null);
        });
    }

    // Test for the Pods table view
    @Test
    void testPodsTableView() {
        // Verify that the Pods table view has the expected items and cells
        verifyThat("#podsTableView", hasItems(3)); // Assuming there are three pods in the mock data
        verifyThat("#podNameColumn", hasTableCell("Pod1"));
        verifyThat("#podNamespaceColumn", hasTableCell("Namespace1"));
        verifyThat("#podStatusColumn", hasTableCell("Running"));
    }

    // Test for the Deployments table view
    @Test
    void testDeploymentsTableView() {
        // Verify that the Deployments table view has the expected items and cells
        verifyThat("#deploymentsTableView", hasItems(2)); // Assuming there are two deployments in the mock data
        verifyThat("#deploymentNameColumn", hasTableCell("Deployment1"));
        verifyThat("#deploymentNamespaceColumn", hasTableCell("Namespace1"));
    }

    // Test for the StatefulSets table view
    @Test
    void testStatefulSetsTableView() {
        // Verify that the StatefulSets table view has the expected items and cells
        verifyThat("#statefulSetsTableView", hasItems(1)); // Assuming there is one StatefulSet in the mock data
        verifyThat("#statefulSetNameColumn", hasTableCell("StatefulSet1"));
        verifyThat("#statefulSetNamespaceColumn", hasTableCell("Namespace1"));
    }

    // Test for the Services table view
    @Test
    void testServicesTableView() {
        // Verify that the Services table view has the expected items and cells
        verifyThat("#servicesTableView", hasItems(2)); // Assuming there are two services in the mock data
        verifyThat("#serviceNameColumn", hasTableCell("Service1"));
        verifyThat("#serviceNamespaceColumn", hasTableCell("Namespace1"));
    }

    // Test for changing to the Containers scene
    @Test
    void testChangeToContainersScene() {
        // Simulate a click on the button to change to the Containers scene
        clickOn("#changeToContainersSceneButton"); // Assuming you have a button with this ID
        // Add verifications based on expected changes in the UI elements or navigation
    }

    // Test for changing to the Images scene
    @Test
    void testChangeToImagesScene() {
        // Simulate a click on the button to change to the Images scene
        clickOn("#changeToImagesSceneButton"); // Assuming you have a button with this ID
        // Add verifications based on expected changes in the UI elements or navigation
    }

    // Test for changing to the Volumes scene
    @Test
    void testChangeToVolumesScene() {
        // Simulate a click on the button to change to the Volumes scene
        clickOn("#changeToVolumesSceneButton"); // Assuming you have a button with this ID
        // Add verifications based on expected changes in the UI elements or navigation
    }

    // Test for changing to the Graphics scene
    @Test
    void testChangeToGraphicsScene() {
        // Simulate a click on the button to change to the Graphics scene
        clickOn("#changeToGraphicsSceneButton"); // Assuming you have a button with this ID
        // Add verifications based on expected changes in the UI elements or navigation
    }
}
