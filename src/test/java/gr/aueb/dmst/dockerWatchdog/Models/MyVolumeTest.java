package gr.aueb.dmst.dockerWatchdog.Models;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyVolumeTest {
    private MyVolume volume;

    @BeforeEach
    public void setUp() {
        ArrayList<String> containerNames = new ArrayList<>();
        containerNames.add("Container1");
        volume = new MyVolume("VodafoneVolume", "TestDriver", "/mnt/test", containerNames);
    }

    @Test
    public void testGetters() {
        assertEquals("VodafoneVolume", volume.getName());
        assertEquals("TestDriver", volume.getDriver());
        assertEquals("/mnt/test", volume.getMountpoint());
        assertEquals(1, volume.getContainerNamesUsing().size());
        assertEquals("Container1", volume.getContainerNamesUsing().get(0));
    }

    @Test
    public void testContainerNamesUsing() {
        volume.addContainerNameUsing("Container2");
        assertEquals(2, volume.getContainerNamesUsing().size());
        assertTrue(volume.getContainerNamesUsing().contains("Container2"));

        volume.removeContainerNameUsing("Container1");
        assertEquals(1, volume.getContainerNamesUsing().size());
        assertFalse(volume.getContainerNamesUsing().contains("Container1"));
    }
}