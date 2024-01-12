package gr.aueb.dmst.dockerWatchdog.Repositories;

import java.util.Optional;
import java.sql.Timestamp;

import gr.aueb.dmst.dockerWatchdog.Models.Metric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * MetricsRepository is an interface that helps us with executing MYSQL queries on the Metric entity.
 * More specifically, declares custom methods for specific database access needs.
 */
public interface MetricsRepository extends JpaRepository<Metric, Integer> {

    /**
     * This method returns the count of metrics with a datetime before the specified datetime.
     * We use it to get the number of changes in the Docker Cluster. It is this query that supports
     * our functionality of detecting changes in the past.
     *
     * @param datetime the datetime to filter by.
     * @return the count of metrics.
     */
    @Query("SELECT count(m) FROM Metric m WHERE m.datetime < :datetime")
    long countByDatetimeBefore(@Param("datetime") Timestamp datetime);

    /**
     * This method returns the first metric with a datetime before the specified datetime, ordered by datetime in descending order.
     * We use it to get the most recent metric that was recorded before a certain point in time.
     *
     * @param datetime the datetime to filter by.
     * @return the metric.
     */
    Optional<Metric> findFirstByDatetimeBeforeOrderByDatetimeDesc(Timestamp datetime);

    /**
     * This method returns the ID of the last recorded metric.
     * We use it to get the ID of the most recent chang, meaning the active state.
     *
     * @return the ID of the last metric.
     */
    @Query("SELECT max(m.id) FROM Metric m")
    Integer findLastMetricId();
}