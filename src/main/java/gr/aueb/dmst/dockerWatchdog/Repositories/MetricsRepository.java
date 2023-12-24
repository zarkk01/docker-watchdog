package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface MetricsRepository extends JpaRepository<Metric, Integer> {

    @Query("SELECT m FROM Metric m WHERE m.datetime BETWEEN :startDate AND :endDate")
    List<Metric> findAllByDatetimeBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

}
