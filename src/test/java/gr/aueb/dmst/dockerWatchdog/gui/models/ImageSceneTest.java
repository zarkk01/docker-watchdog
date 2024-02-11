package gr.aueb.dmst.dockerWatchdog.gui.models;

import gr.aueb.dmst.dockerWatchdog.gui.models.ImageScene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImageSceneTest {
    private ImageScene imageScene;

    @BeforeEach
    public void setUp() {
        imageScene = new ImageScene("12345678910111213", "TestImage", 1024L, "In use");
    }

    @Test
    public void testConstructor() {
        assertEquals("12345678910111213".substring(7), imageScene.getId());
        assertEquals("TestImage", imageScene.getName());
        assertEquals("In use", imageScene.getStatus());
    }

    @Test
    public void testGetters() {
        assertEquals("12345678910111213".substring(7), imageScene.getId());
        assertEquals("TestImage", imageScene.getName());
        assertEquals("In use", imageScene.getStatus());
    }

    @Test
    public void testSetters() {
        imageScene.setId("4567891011121314");
        imageScene.setName("NewImage");
        imageScene.setSize(2048L);
        imageScene.setStatus("Unused");

        assertEquals("4567891011121314".substring(7), imageScene.getId());
        assertEquals("NewImage", imageScene.getName());
        assertEquals("Unused", imageScene.getStatus());
    }
}