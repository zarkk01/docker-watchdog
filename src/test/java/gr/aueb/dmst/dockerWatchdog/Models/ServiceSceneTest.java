package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceSceneTest {

    @Test
    public void testConstructorInitialization() {
        ServiceScene serviceScene = new ServiceScene("TestService", "TestNamespace");
        assertNotNull(serviceScene);
    }

    @Test
    public void testGetterMethods() {
        ServiceScene serviceScene = new ServiceScene("TestService", "TestNamespace");

        assertEquals("TestService", serviceScene.getName());
        assertEquals("TestNamespace", serviceScene.getNamespace());
    }
}
