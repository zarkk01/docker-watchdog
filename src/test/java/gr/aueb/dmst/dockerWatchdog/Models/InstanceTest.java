package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstanceTest {
    private Instance instance;

    @BeforeEach
    public void setUp() {
        instance = new Instance();
        instance.setId("123");
        instance.setMetricId(1);
        instance.setName("TestInstance");
        instance.setImage("TestImage");
        instance.setStatus("Active");
        instance.setMemoryUsage(1024L);
        instance.setPids(5L);
        instance.setCpuUsage(0.8);
        instance.setBlockI(10.5);
        instance.setBlockO(8.3);
        instance.setVolumes("Volume1");
        instance.setSubnet("192.168.0.1");
        instance.setGateway("192.168.0.254");
        instance.setPrefixLen(24);
    }

    @Test
    public void testGetters() {
        assertEquals("123", instance.getId());
        assertEquals(1, instance.getMetricid());
        assertEquals("TestInstance", instance.getName());
        assertEquals("TestImage", instance.getImage());
        assertEquals("Active", instance.getStatus());
        assertEquals(1024L, instance.getMemoryUsage());
        assertEquals(5L, instance.getPids());
        assertEquals(0.8, instance.getCpuUsage(), 0.001);
        assertEquals(10.5, instance.getBlockI(), 0.001);
        assertEquals(8.3, instance.getBlockO(), 0.001);
        assertEquals("Volume1", instance.getVolumes());
        assertEquals("192.168.0.1", instance.getSubnet());
        assertEquals("192.168.0.254", instance.getGateway());
        assertEquals(24, instance.getPrefixLen());
    }

    @Test
    public void testSetters() {
        instance.setId("456");
        instance.setMetricId(2);
        instance.setName("NewInstance");
        instance.setImage("NewImage");
        instance.setStatus("Inactive");
        instance.setMemoryUsage(2048L);
        instance.setPids(10L);
        instance.setCpuUsage(0.5);
        instance.setBlockI(20.0);
        instance.setBlockO(16.0);
        instance.setVolumes("Volume2");
        instance.setSubnet("192.168.1.1");
        instance.setGateway("192.168.1.254");
        instance.setPrefixLen(28);

        assertEquals("456", instance.getId());
        assertEquals(2, instance.getMetricid());
        assertEquals("NewInstance", instance.getName());
        assertEquals("NewImage", instance.getImage());
        assertEquals("Inactive", instance.getStatus());
        assertEquals(2048L, instance.getMemoryUsage());
        assertEquals(10L, instance.getPids());
        assertEquals(0.5, instance.getCpuUsage(), 0.001);
        assertEquals(20.0, instance.getBlockI(), 0.001);
        assertEquals(16.0, instance.getBlockO(), 0.001);
        assertEquals("Volume2", instance.getVolumes());
        assertEquals("192.168.1.1", instance.getSubnet());
        assertEquals("192.168.1.254", instance.getGateway());
        assertEquals(28, instance.getPrefixLen());
    }
}