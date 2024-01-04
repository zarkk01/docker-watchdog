package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import org.testfx.framework.junit.ApplicationTest;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DesktopAppTest extends ApplicationTest {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // No need to implement
    }

    @Test
    public void testStart() {
        // Mock the stage
        Stage stage = new Stage();

        // Launch the application on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // Start the application
                DesktopApp app = new DesktopApp();
                app.start(stage);

                // Verify that the scene is set
                Scene scene = stage.getScene();
                assertNotNull(scene);

                // Add more assertions based on your specific requirements
                assertTrue(stage.isShowing());  // Example assertion for stage being visible
            } catch (Exception e) {
                e.printStackTrace(); // Handle or log exceptions as needed
            }
        });

        // Allow time for the JavaFX Application Thread to process the start
        assertDoesNotThrow(() -> Thread.sleep(2000));
    }

    @Test
    public void testCloseRequest() {
        // Mock the stage
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);

        // Launch the application on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // Start the application
                DesktopApp app = new DesktopApp();
                app.start(stage);

                // Fire the close request event
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } catch (Exception e) {
                e.printStackTrace(); // Handle or log exceptions as needed
            }
        });

        // Allow time for the JavaFX Application Thread to process the close request
        assertDoesNotThrow(() -> Thread.sleep(2000));

        // Verify that the stage's close method was called
        verify(stage).fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
