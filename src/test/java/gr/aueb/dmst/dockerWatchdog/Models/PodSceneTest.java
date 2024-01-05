package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Models.PodScene;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PodSceneTest {

    @Test
    public void testConstructorInitialization() {
        PodScene podScene = new PodScene("TestPod", "TestNamespace", "Running");
        assertNotNull(podScene);
    }

    @Test
    public void testGetterMethods() {
        PodScene podScene = new PodScene("TestPod", "TestNamespace", "Running");

        assertEquals("TestPod", podScene.getName());
        assertEquals("TestNamespace", podScene.getNamespace());
        assertEquals("Running", podScene.getStatus());
    }
}
