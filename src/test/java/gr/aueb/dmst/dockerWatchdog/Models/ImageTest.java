package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImageTest {
    private Image image;

    @BeforeEach
    public void setUp() {
        image = new Image();
        image.setId("123");
        image.setName("TestImage");
        image.setStatus("Active");
        image.setSize(1024L);
    }

    @Test
    public void testGetters() {
        assertEquals("123", image.getId());
        assertEquals("TestImage", image.getName());
        assertEquals("Active", image.getStatus());
        assertEquals(1024L, image.getSize());
    }

    @Test
    public void testSetters() {
        image.setId("456");
        image.setName("NewImage");
        image.setStatus("Inactive");
        image.setSize(2048L);

        assertEquals("456", image.getId());
        assertEquals("NewImage", image.getName());
        assertEquals("Inactive", image.getStatus());
        assertEquals(2048L, image.getSize());
    }
}