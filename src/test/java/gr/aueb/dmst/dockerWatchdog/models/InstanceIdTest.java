package gr.aueb.dmst.dockerWatchdog.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstanceIdTest {
    private InstanceId instanceId;

    @BeforeEach
    public void setUp() {
        instanceId = new InstanceId("123", 1);
    }

    @Test
    public void testConstructor() {
        InstanceId instanceId = new InstanceId("123", 1);
        assertEquals("123", instanceId.getId());
        assertEquals(1, instanceId.getMetricid());
    }

    @Test
    public void testGetters() {
        assertEquals("123", instanceId.getId());
        assertEquals(1, instanceId.getMetricid());
    }

    @Test
    public void testSetters() {
        instanceId.setId("456");
        instanceId.setMetricId(2);

        assertEquals("456", instanceId.getId());
        assertEquals(2, instanceId.getMetricid());
    }

    @Test
    public void testEqualsAndHashCode() {
        InstanceId instanceId1 = new InstanceId("123", 1);
        InstanceId instanceId2 = new InstanceId("123", 1);
        InstanceId instanceId3 = new InstanceId("456", 2);

        assertEquals(instanceId1, instanceId2);
        assertNotEquals(instanceId1, instanceId3);
        assertEquals(instanceId1.hashCode(), instanceId2.hashCode());
    }
}