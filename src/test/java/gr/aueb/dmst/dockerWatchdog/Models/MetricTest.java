package gr.aueb.dmst.dockerWatchdog.Models;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class MetricTest {

    @Test
    public void testConstructorInitialization() {
        Metric metric = new Metric();
        assertNotNull(metric);
    }

    @Test
    public void testGetterAndSetter() {
        Metric metric = new Metric();
        metric.setId(1);
        metric.setDatetime(Timestamp.valueOf("2022-01-01 12:00:00"));

        assertEquals(1, metric.getId());
        assertEquals(Timestamp.valueOf("2022-01-01 12:00:00"), metric.getDatetime());
    }

    @Test
    public void testImmutableProperties() {
        Metric metric = new Metric();
        assertThrows(UnsupportedOperationException.class, () -> metric.getId().intValue());
        assertThrows(UnsupportedOperationException.class, () -> metric.getDatetime().setNanos(0));
        // Repeat for other properties
    }
}
