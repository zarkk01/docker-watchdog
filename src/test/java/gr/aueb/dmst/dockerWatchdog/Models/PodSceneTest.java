package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PodSceneTest {
    private PodScene podScene;

    @BeforeEach
    public void setUp() {
        podScene = new PodScene("TestPod", "TestNamespace", "Running");
    }

    @Test
    public void testConstructor() {
        PodScene podScene = new PodScene("TestPod", "TestNamespace", "Running");
        assertEquals("TestPod", podScene.getName());
        assertEquals("TestNamespace", podScene.getNamespace());
        assertEquals("Running", podScene.getStatus());
    }

    @Test
    public void testGetters() {
        assertEquals("TestPod", podScene.getName());
        assertEquals("TestNamespace", podScene.getNamespace());
        assertEquals("Running", podScene.getStatus());
    }
}