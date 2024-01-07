package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class MyInstanceTest {
    private MyInstance instance;

    @BeforeEach
    public void setUp() {
        ArrayList<String> volumes = new ArrayList<>();
        volumes.add("Volume1");
        instance = new MyInstance("12345678910", "/excellent_lockin", "vodafone", "Running", 0.8, 1024L, 5L, 10.5, 8.3, "8080:80", volumes, "192.168.0.1", "192.168.0.254", 24);
    }

    @Test
    public void testGetters() {
        assertEquals("12345678910", instance.getId());
        assertEquals("/excellent_lockin", instance.getName());
        assertEquals("vodafone", instance.getImage());
        assertEquals("Running", instance.getStatus());
        assertEquals(1024L, instance.getMemoryUsage());
        assertEquals(5L, instance.getPids());
        assertEquals(0.8, instance.getCpuUsage(), 0.001);
        assertEquals(10.5, instance.getBlockI(), 0.001);
        assertEquals(8.3, instance.getBlockO(), 0.001);
        assertEquals("8080:80", instance.getPorts());
        assertEquals(1, instance.getVolumes().size());
        assertEquals("Volume1", instance.getVolumes().get(0));
        assertEquals("192.168.0.1", instance.getSubnet());
        assertEquals("192.168.0.254", instance.getGateway());
        assertEquals(24, instance.getPrefixLen());
    }

    @Test
    public void testSetters() {
        instance.setName("VodafoneContainer");
        instance.setStatus("Exited");
        instance.setMemoryUsage(2048L);
        instance.setPids(10L);
        instance.setCpuUsage(0.5);
        instance.setBlockI(20.0);
        instance.setBlockO(16.0);
        ArrayList<String> newVolumes = new ArrayList<>();
        newVolumes.add("Volume2");
        instance.setVolume(newVolumes);
        instance.setSubnet("192.168.1.1");
        instance.setGateway("192.168.1.254");
        instance.setPrefixLen(28);

        assertEquals("VodafoneContainer", instance.getName());
        assertEquals("Exited", instance.getStatus());
        assertEquals(2048L, instance.getMemoryUsage());
        assertEquals(10L, instance.getPids());
        assertEquals(0.5, instance.getCpuUsage(), 0.001);
        assertEquals(20.0, instance.getBlockI(), 0.001);
        assertEquals(16.0, instance.getBlockO(), 0.001);
        assertEquals(1, instance.getVolumes().size());
        assertEquals("Volume2", instance.getVolumes().get(0));
        assertEquals("192.168.1.1", instance.getSubnet());
        assertEquals("192.168.1.254", instance.getGateway());
        assertEquals(28, instance.getPrefixLen());
    }
}