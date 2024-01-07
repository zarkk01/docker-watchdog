package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatefulSetSceneTest {
    private StatefulSetScene statefulSetScene;

    @BeforeEach
    public void setUp() {
        statefulSetScene = new StatefulSetScene("TestStatefulSet", "TestNamespace");
    }

    @Test
    public void testConstructorInitialization() {
        StatefulSetScene statefulSetScene1 = new StatefulSetScene("TestStatefulSet", "TestNamespace");
        assertNotNull(statefulSetScene1);
    }

    @Test
    public void testGetters() {
        assertEquals("TestStatefulSet", statefulSetScene.getName());
        assertEquals("TestNamespace", statefulSetScene.getNamespace());
    }
}
