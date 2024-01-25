package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceSceneTest {
    private ServiceScene serviceScene;

    @BeforeEach
    public void setUp() {
        serviceScene = new ServiceScene("TestService", "TestNamespace");
    }

    @Test
    public void testConstructor() {
        ServiceScene serviceScene = new ServiceScene("TestService", "TestNamespace");
        assertEquals("TestService", serviceScene.getName());
        assertEquals("TestNamespace", serviceScene.getNamespace());
    }

    @Test
    public void testGetters() {
        assertEquals("TestService", serviceScene.getName());
        assertEquals("TestNamespace", serviceScene.getNamespace());
    }
}