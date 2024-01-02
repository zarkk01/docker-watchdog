package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MetricsRepository extends JpaRepository<Metric, Integer> {

    @Query("SELECT count(m) FROM Metric m WHERE m.datetime < :datetime")
    long countByDatetimeBefore(@Param("datetime") Timestamp datetime);

    Optional<Metric> findFirstByDatetimeBeforeOrderByDatetimeDesc(Timestamp datetime);

    @Query("SELECT max(m.id) FROM Metric m")
    Integer findLastMetricId();
}
