package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VolumeSceneTest {
    private VolumeScene volumeScene;

    @BeforeEach
    public void setUp() {
        volumeScene = new VolumeScene("TestVolume", "TestDriver", "/mnt/test", "Container1");
    }

    @Test
    public void testGetters() {
        assertEquals("TestVolume", volumeScene.getName());
        assertEquals("TestDriver", volumeScene.getDriver());
        assertEquals("/mnt/test", volumeScene.getMountpoint());
        assertEquals("Container1", volumeScene.getContainerNamesUsing());
    }

    @Test
    public void testSetters() {
        volumeScene.setName("NewVolume");
        volumeScene.setDriver("NewDriver");
        volumeScene.setMountpoint("/mnt/new");
        volumeScene.setContainerNamesUsing("Container2");

        assertEquals("NewVolume", volumeScene.getName());
        assertEquals("NewDriver", volumeScene.getDriver());
        assertEquals("/mnt/new", volumeScene.getMountpoint());
        assertEquals("Container2", volumeScene.getContainerNamesUsing());
    }
}