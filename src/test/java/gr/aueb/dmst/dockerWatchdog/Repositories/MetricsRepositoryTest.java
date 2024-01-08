package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MetricsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MetricsRepository metricsRepository;

    private Metric metric1;
    private Metric metric2;

    @BeforeEach
    public void setup() {
        metric1 = new Metric();
        metric1.setId(1);
        metric1.setDatetime(new Timestamp(System.currentTimeMillis() - 10000));
        entityManager.merge(metric1);

        metric2 = new Metric();
        metric2.setId(2);
        metric2.setDatetime(new Timestamp(System.currentTimeMillis()));
        entityManager.merge(metric2);

        entityManager.flush();
    }

    @Test
    public void testCountByDatetimeBefore() {
        long count = metricsRepository.countByDatetimeBefore(new Timestamp(System.currentTimeMillis()));

        assertEquals(2, count);
    }

    @Test
    public void testFindFirstByDatetimeBeforeOrderByDatetimeDesc() {
        Optional<Metric> metric = metricsRepository.findFirstByDatetimeBeforeOrderByDatetimeDesc(new Timestamp(System.currentTimeMillis()));

        assertTrue(metric.isPresent());
        assertEquals(2, metric.get().getId());
    }

    @Test
    public void testFindLastMetricId() {
        Integer lastMetricId = metricsRepository.findLastMetricId();

        assertEquals(2, lastMetricId);
    }

    @AfterEach
    public void tearDown() {
        metricsRepository.deleteAll();
    }
}