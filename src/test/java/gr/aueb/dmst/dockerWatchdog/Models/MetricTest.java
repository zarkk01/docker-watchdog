package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class MetricTest {
    private Metric metric;

    @BeforeEach
    public void setUp() {
        metric = new Metric();
        metric.setId(1);
        metric.setDatetime(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    public void testConstructorInitialization() {
        Metric metric = new Metric();
        assertNotNull(metric);
    }

    @Test
    public void testGetters() {
        assertEquals(1, metric.getId());
        assertNotNull(metric.getDatetime());
    }

    @Test
    public void testSetters() {
        metric.setId(2);
        Timestamp newTimestamp = new Timestamp(System.currentTimeMillis());
        metric.setDatetime(newTimestamp);
        assertEquals(2, metric.getId());
        assertEquals(newTimestamp, metric.getDatetime());
    }
}
