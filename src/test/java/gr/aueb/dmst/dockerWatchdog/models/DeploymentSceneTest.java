package gr.aueb.dmst.dockerWatchdog.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeploymentSceneTest {
    private DeploymentScene deploymentScene;

    @BeforeEach
    public void setUp() {
        deploymentScene = new DeploymentScene("TestDeployment", "TestNamespace");
    }

    @Test
    public void testConstructor() {
        DeploymentScene deploymentScene = new DeploymentScene("TestDeployment", "TestNamespace");
        assertEquals("TestDeployment", deploymentScene.getName());
    }

    @Test
    public void testGetters() {
        assertEquals("TestDeployment", deploymentScene.getName());
    }
}