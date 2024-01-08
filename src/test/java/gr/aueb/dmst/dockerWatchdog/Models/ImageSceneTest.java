package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageSceneTest {
    private ImageScene imageScene;

    @BeforeEach
    public void setUp() {
        imageScene = new ImageScene("123", "TestImage", 1024L, "In use");
    }

    @Test
    public void testConstructor() {
        ImageScene imageScene = new ImageScene("123", "TestImage", 1024L, "In use");
        assertEquals("123", imageScene.getId());
        assertEquals("TestImage", imageScene.getName());
        assertEquals(1024L, imageScene.getSize());
        assertEquals("In use", imageScene.getStatus());
    }

    @Test
    public void testGetters() {
        assertEquals("123", imageScene.getId());
        assertEquals("TestImage", imageScene.getName());
        assertEquals(1024L, imageScene.getSize());
        assertEquals("In use", imageScene.getStatus());
    }

    @Test
    public void testSetters() {
        imageScene.setId("456");
        imageScene.setName("NewImage");
        imageScene.setSize(2048L);
        imageScene.setStatus("Unused");

        assertEquals("456", imageScene.getId());
        assertEquals("NewImage", imageScene.getName());
        assertEquals(2048L, imageScene.getSize());
        assertEquals("Unused", imageScene.getStatus());
    }
}