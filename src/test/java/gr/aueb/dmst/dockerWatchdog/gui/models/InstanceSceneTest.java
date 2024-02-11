package gr.aueb.dmst.dockerWatchdog.gui.models;

import gr.aueb.dmst.dockerWatchdog.gui.models.InstanceScene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstanceSceneTest {
    private InstanceScene instanceScene;

    @BeforeEach
    public void setUp() {
        instanceScene = new InstanceScene("123", "TestInstance", "TestImage", "Active", "1024", "5", "0.8", "10.5", "8.3", "Volume1", "192.168.0.1", "192.168.0.254", 24, true);
    }

    @Test
    public void testConstructor() {
        InstanceScene instanceScene = new InstanceScene("123", "TestInstance", "TestImage", "Active", "1024", "5", "0.8", "10.5", "8.3", "Volume1", "192.168.0.1", "192.168.0.254", 24, true);
        assertEquals("123", instanceScene.getId());
        assertEquals("TestInstance", instanceScene.getName());
        assertEquals("TestImage", instanceScene.getImage());
        assertEquals("Active", instanceScene.getStatus());
        assertEquals("1024", instanceScene.getMemoryUsage());
        assertEquals("5", instanceScene.getPids());
        assertEquals("0.8", instanceScene.getCpuUsage());
        assertEquals("10.5", instanceScene.getBlockI());
        assertEquals("8.3", instanceScene.getBlockO());
        assertEquals("Volume1", instanceScene.getVolumes());
        assertEquals("192.168.0.1", instanceScene.getSubnet());
        assertEquals("192.168.0.254", instanceScene.getGateway());
        assertEquals(24, instanceScene.getPrefixLen());
        assertTrue(instanceScene.isSelect());
    }

    @Test
    public void testGetters() {
        assertEquals("123", instanceScene.getId());
        assertEquals("TestInstance", instanceScene.getName());
        assertEquals("TestImage", instanceScene.getImage());
        assertEquals("Active", instanceScene.getStatus());
        assertEquals("1024", instanceScene.getMemoryUsage());
        assertEquals("5", instanceScene.getPids());
        assertEquals("0.8", instanceScene.getCpuUsage());
        assertEquals("10.5", instanceScene.getBlockI());
        assertEquals("8.3", instanceScene.getBlockO());
        assertEquals("Volume1", instanceScene.getVolumes());
        assertEquals("192.168.0.1", instanceScene.getSubnet());
        assertEquals("192.168.0.254", instanceScene.getGateway());
        assertEquals(24, instanceScene.getPrefixLen());
        assertTrue(instanceScene.isSelect());
    }
}