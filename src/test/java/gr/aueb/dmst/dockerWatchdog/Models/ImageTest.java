package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImageTest {

    @Test
    public void testConstructorInitialization() {
        Image image = new Image();
        assertNotNull(image);
    }

    @Test
    public void testGetterAndSetter() {
        Image image = new Image();
        image.setId("123");
        image.setName("TestImage");
        image.setStatus("Active");
        image.setSize(1024L);

        assertEquals("123", image.getId());
        assertEquals("TestImage", image.getName());
        assertEquals("Active", image.getStatus());
        assertEquals(1024L, image.getSize());
    }

    @Test
    public void testImmutableProperties() {
        Image image = new Image();
        assertThrows(UnsupportedOperationException.class, () -> image.getId().toUpperCase());
        assertThrows(UnsupportedOperationException.class, () -> image.getName().toUpperCase());
        assertThrows(UnsupportedOperationException.class, () -> image.getSize().intValue());
    }
}
