package gr.aueb.dmst.dockerWatchdog.Repositories;

import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstanceRepository extends JpaRepository<Instance, Long> {

    // Custom query to retrieve instances with the maximum metric ID
    @Query("SELECT i FROM Instance i WHERE i.metricid = (SELECT MAX(i2.metricid) FROM Instance i2)")
    List<Instance> findAllByMaxMetricId();

    @Query("SELECT count(i) FROM Instance i WHERE i.metricid = :metricid AND i.status = 'running'")
    long countByMetricIdAndStatusRunning(@Param("metricid") Integer metricid);
}