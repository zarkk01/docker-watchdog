package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstanceTest {

    @Test
    public void testConstructorInitialization() {
        Instance instance = new Instance();
        assertNotNull(instance);
    }

    @Test
    public void testGetterAndSetter() {
        Instance instance = new Instance();
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

        assertEquals("123", instance.getId());
        assertEquals(1, instance.getMetricid());
        assertEquals("TestInstance", instance.getName());
        assertEquals("TestImage", instance.getImage());
        assertEquals("Active", instance.getStatus());
        assertEquals(1024L, instance.getMemoryUsage());
        assertEquals(5L, instance.getPids());
        assertEquals(0.8, instance.getCpuUsage());
        assertEquals(10.5, instance.getBlockI());
        assertEquals(8.3, instance.getBlockO());
        assertEquals("Volume1", instance.getVolumes());
        assertEquals("192.168.0.1", instance.getSubnet());
        assertEquals("192.168.0.254", instance.getGateway());
        assertEquals(24, instance.getPrefixLen());
    }
}
