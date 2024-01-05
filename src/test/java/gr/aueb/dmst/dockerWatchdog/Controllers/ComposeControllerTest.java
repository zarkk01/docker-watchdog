package gr.aueb.dmst.dockerWatchdog.Controllers;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ComposeControllerTest extends ApplicationTest {

    private ComposeController composeController;
    private static final String VALID_YAML_PATH = "valid-compose.yaml";
    private static final String INVALID_YAML_PATH = "invalid-compose.yaml";

    @Override
    public void start(Stage stage) throws IOException {
        new JFXPanel(); // Initializes the JavaFX Toolkit
        composeController = new ComposeController();
        composeController.setYamlFilePath(VALID_YAML_PATH);

        // You may need to set up your FXML file and other dependencies here.
    }

    @Test
    public void testValidateYaml_ValidYaml() {
        composeController.setYamlFilePath(VALID_YAML_PATH);
        clickOn("#validateButton");

        assertEquals("Valid", composeController.validateButton.getText());
        assertNull(composeController.validateButton.getStyle());
        assertTrue(composeController.validateButton.isDisabled());
    }

    @Test
    public void testValidateYaml_InvalidYaml() {
        composeController.setYamlFilePath(INVALID_YAML_PATH);
        clickOn("#validateButton");

        assertEquals("Sorry not valid", composeController.validateButton.getText());
        assertEquals("-fx-background-color: red;", composeController.validateButton.getStyle());
        assertTrue(composeController.validateButton.isDisabled());
    }

    @Test
    public void testStartDockerCompose_Success() throws IOException, InterruptedException {
        DockerComposeExecutor mockExecutor = mock(DockerComposeExecutor.class);

        // Set the mock executor in the ComposeController
        composeController.setDockerComposeExecutor(mockExecutor);

        // Specify the behavior of the mock
        when(mockExecutor.execute(anyString())).thenReturn(0);

        int exitCode = composeController.startDockerCompose();
        assertEquals(0, exitCode);

        // Verify that the execute method was called with the correct arguments
        verify(mockExecutor).execute(anyString());
    }

    @Test
    public void testStartDockerCompose_Error() throws IOException, InterruptedException {
        // Mock the Docker Compose execution to simulate an error
        int exitCode = composeController.startDockerComposeWithErrorMock();
        assertNotEquals(0, exitCode);
        // Add assertions based on your specific requirements
    }

    @Test
    public void testStopDockerCompose_Success() throws IOException, InterruptedException {
        // Mock the Docker Compose stopping process
        int exitCode = composeController.stopDockerComposeMock();
        assertEquals(0, exitCode);
        // Add assertions based on your specific requirements
    }

    @Test
    public void testStopDockerCompose_Error() throws IOException, InterruptedException {
        // Mock the Docker Compose stopping process to simulate an error
        int exitCode = composeController.stopDockerComposeWithErrorMock();
        assertNotEquals(0, exitCode);
        // Add assertions based on your specific requirements
    }

    @Test
    public void testSaveYaml_Success() throws IOException {
        // Mock the file system to simulate writing to a YAML file
        String newContent = "new content";
        composeController.yamlContentArea.setText(newContent);
        composeController.saveYaml();

        String savedContent = new String(Files.readAllBytes(Paths.get(VALID_YAML_PATH)));
        assertEquals(newContent, savedContent);
        assertEquals("Saved", composeController.savedLabel.getText());
    }

    @Test
    public void testSaveYaml_Error() throws IOException {
        // Mock the file system to simulate an error during the saving process
        composeController.setYamlFilePath("nonexistent.yaml");
        composeController.saveYaml();

        // Add assertions based on your specific requirements for error handling
    }

    @Test
    public void testShowConfig_ShowConfig() {
        clickOn("#showConfigButton");
        assertTrue(composeController.isShowingConfig);
        assertEquals("Show YAML", composeController.showConfigButton.getText());
    }

    @Test
    public void testShowConfig_ShowYaml() {
        clickOn("#showConfigButton");
        clickOn("#showConfigButton");
        assertFalse(composeController.isShowingConfig);
        assertEquals("Show Config", composeController.showConfigButton.getText());
    }

    @Test
    public void testChangeScene() {
        clickOn("#containersButton");
        // Verify that the scene has changed to the containers scene
        // Add assertions based on your specific requirements
    }
}
