package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Main;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MyVolumeTest {

    @Test
    public void testConstructorInitialization() {
        MyVolume myVolume = new MyVolume("TestVolume", "TestDriver", "/mnt/test", new ArrayList<>());
        assertNotNull(myVolume);
    }



    @Test
    public void testImmutableProperties() {
        MyVolume myVolume = new MyVolume("TestVolume", "TestDriver", "/mnt/test", new ArrayList<>());
        assertThrows(UnsupportedOperationException.class, () -> myVolume.getName().toUpperCase());
        assertThrows(UnsupportedOperationException.class, () -> myVolume.getDriver().toUpperCase());
        assertThrows(UnsupportedOperationException.class, () -> myVolume.getMountpoint().toUpperCase());
    }

    @Test
    public void testAddContainerNameUsing() {
        MyVolume myVolume = new MyVolume("TestVolume", "TestDriver", "/mnt/test", new ArrayList<>());
        myVolume.addContainerNameUsing("Container1");

        assertTrue(myVolume.getContainerNamesUsing().contains("Container1"));
    }

    // Add tests for setContainerNamesUsing and removeContainerNameUsing methods

    @Test
    public void testGetVolumeByName() {
        MyVolume myVolume1 = new MyVolume("TestVolume1", "TestDriver", "/mnt/test", new ArrayList<>());
        MyVolume myVolume2 = new MyVolume("TestVolume2", "TestDriver", "/mnt/test", new ArrayList<>());
        Main.myVolumesList.addAll(Arrays.asList(myVolume1, myVolume2));

        assertEquals(myVolume1, MyVolume.getVolumeByName("TestVolume1"));
        assertNull(MyVolume.getVolumeByName("NonExistentVolume"));
    }
}
