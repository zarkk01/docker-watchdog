package gr.aueb.dmst.dockerWatchdog.repositories;

import gr.aueb.dmst.dockerWatchdog.model.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstanceRepository extends JpaRepository<Instance, Long> {
    // You can define custom query methods here if needed

    // Custom query to retrieve instances with the maximum metric ID

    @Query("SELECT i FROM Instance i WHERE i.metricid = (SELECT MAX(i2.metricid) FROM Instance i2)")
    List<Instance> findAllByMaxMetricId();
}