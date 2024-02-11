package gr.aueb.dmst.dockerWatchdog.api.entities;

import gr.aueb.dmst.dockerWatchdog.api.entities.Volume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VolumeTest {
    private Volume volume;

    @BeforeEach
    public void setUp() {
        volume = new Volume();
        volume.setName("TestVolume");
        volume.setDriver("TestDriver");
        volume.setMountpoint("/mnt/test");
        volume.setContainerNamesUsing("Container1");
    }

    @Test
    public void testGetters() {
        assertEquals("TestVolume", volume.getName());
        assertEquals("TestDriver", volume.getDriver());
        assertEquals("/mnt/test", volume.getMountpoint());
        assertEquals("Container1", volume.getContainerNamesUsing());
    }

    @Test
    public void testSetters() {
        volume.setName("NewVolume");
        volume.setDriver("NewDriver");
        volume.setMountpoint("/mnt/new");
        volume.setContainerNamesUsing("Container2");

        assertEquals("NewVolume", volume.getName());
        assertEquals("NewDriver", volume.getDriver());
        assertEquals("/mnt/new", volume.getMountpoint());
        assertEquals("Container2", volume.getContainerNamesUsing());
    }
}