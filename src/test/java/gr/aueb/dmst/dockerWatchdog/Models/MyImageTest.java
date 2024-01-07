package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyImageTest {
    private MyImage image;

    @BeforeEach
    public void setUp() {
        image = new MyImage("ubuntu", "1721829283", 1024L, "Unused");
    }

    @Test
    public void testGetters() {
        assertEquals("ubuntu", image.getName());
        assertEquals("1721829283", image.getId());
        assertEquals(1024L, image.getSize());
        assertEquals("Unused", image.getStatus());
    }

    @Test
    public void testSetters() {
        image.setStatus("Unused");
        assertEquals("Unused", image.getStatus());
    }
}