package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatefulSetSceneTest {

    @Test
    public void testConstructorInitialization() {
        StatefulSetScene statefulSetScene = new StatefulSetScene("TestStatefulSet", "TestNamespace");
        assertNotNull(statefulSetScene);
    }

    @Test
    public void testGetterMethods() {
        StatefulSetScene statefulSetScene = new StatefulSetScene("TestStatefulSet", "TestNamespace");

        assertEquals("TestStatefulSet", statefulSetScene.getName());
        assertEquals("TestNamespace", statefulSetScene.getNamespace());
    }
}
