//package gr.aueb.dmst.dockerWatchdog.Controllers;
//
//import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
//import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.control.TableView;
//import javafx.stage.Stage;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.testfx.framework.junit.ApplicationTest;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class ContainersControllerTest extends ApplicationTest {
//
//    private ContainersController containersController;
//
//    @Mock
//    private TableView<InstanceScene> instancesTableView;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        containersController = new ContainersController();
//        containersController.instancesTableView = instancesTableView;
//        new JFXPanel(); // Initializes the JavaFX Toolkit
//    }
//
//    @Test
//    public void testRemoveSelectedContainers() {
//        containersController.removeSelectedContainers();
//        // Add assertions based on your specific requirements
//        // Verify that the HTTP requests and responses are mocked or handle it accordingly
//    }
//
//    @Test
//    public void testChangeScene() throws IOException {
//        containersController.changeScene(null, "newScene.fxml");
//        // Add assertions based on your specific requirements
//        // Verify that the scene change is handled properly
//    }
//
//    @Test
//    public void testStartContainer() throws IOException, InterruptedException, URISyntaxException {
//        InstanceScene instance = mock(InstanceScene.class);
//        when(instance.getId()).thenReturn("containerId");
//        when(instance.getName()).thenReturn("containerName");
//        when(instance.getStatus()).thenReturn("running");
//
//        assertDoesNotThrow(() -> containersController.startContainer(instance));
//        // Add assertions based on your specific requirements
//        // Verify that the container is started or handle it accordingly
//    }
//
//    // Add similar tests for other methods...
//
//    @Test
//    public void testShowDataThen() {
//        assertDoesNotThrow(() -> containersController.showDataThen(null));
//        // Add assertions based on your specific requirements
//        // Verify that data is shown based on the selected date or handle it accordingly
//    }
//
//    @Test
//    public void testClearInfo() {
//        containersController.clearInfo();
//        // Add assertions based on your specific requirements
//        // Verify that info is cleared or handle it accordingly
//    }
//
//    @Test
//    public void testShowNotification() {
//        Platform.runLater(() -> {
//            containersController.showNotification("Title", "Content");
//            // Add assertions based on your specific requirements
//            // Verify that the notification is displayed correctly or handle it accordingly
//        });
//    }
//
//    @Test
//    public void testHandleUploadFile() {
//        containersController.handleUploadFile(null);
//        // Add assertions based on your specific requirements
//        // Verify that file upload is handled or handle it accordingly
//    }
//
//    @Test
//    public void testInitialize() {
//        assertDoesNotThrow(() -> containersController.initialize(null, null));
//        // Add assertions based on your specific requirements
//        // Verify that the initialization is done properly or handle it accordingly
//        TableView tableView = containersController.instancesTableView;
//        assertNotNull(tableView);
//    }
//
//    // You can add more tests as needed for other methods...
//
//    @Override
//    public void start(Stage stage) throws Exception {
//    }
//}
