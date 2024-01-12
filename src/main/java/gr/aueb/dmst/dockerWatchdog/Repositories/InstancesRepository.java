package gr.aueb.dmst.dockerWatchdog.Repositories;

import java.util.List;

import gr.aueb.dmst.dockerWatchdog.Models.Instance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * InstancesRepository is an interface that help us with executing MYSQL queries on the Instance entity.
 * InstancesRepository declares custom methods for specific database access needs.
 */
public interface InstancesRepository extends JpaRepository<Instance, Long> {

    /**
     * This method returns a list of all instances associated with the maximum metric ID.
     * We use it so to get the instances of the latest metric, that means the current state
     * of the Docker Cluster.
     *
     * @return a list of instances.
     */
    @Query("SELECT i FROM Instance i WHERE i.metricid = (SELECT MAX(i2.metricid) FROM Instance i2)")
    List<Instance> findAllByMaxMetricId();

    /**
     * This method returns the count of instances with a specific metric ID and a status of 'running'.
     * We use it so to get the number of running instances in the past or in the present.
     *
     * @param metricid the metric ID to filter by.
     * @return the count of instances.
     */
    @Query("SELECT count(i) FROM Instance i WHERE i.metricid = :metricid AND i.status = 'running'")
    long countByMetricIdAndStatusRunning(@Param("metricid") Integer metricid);

    /**
     * This method returns an instance with a specific container ID.
     *
     * @param containerid the container ID to filter by.
     * @return the instance.
     */
    @Query("SELECT i FROM Instance i WHERE i.id = :containerid")
    Instance findByContainerId(@Param("containerid") String containerid);

    /**
     * This method returns the count of instances with a specific metric ID.
     * We use it so to get the total number of existing instances in the first box
     * in the dashboard.
     *
     * @param metricid the metric ID to filter by.
     * @return the count of instances.
     */
    @Query("SELECT count(i) FROM Instance i WHERE i.metricid = :metricid")
    Long findAllByMetricId(@Param("metricid") Integer metricid);

    /**
     * This method returns a list of all instances associated with a specific image name.
     * We use it so to get all instances of an image and start or stop them.
     *
     * @param imageName the image name to filter by.
     * @return a list of instances.
     */
    @Query("SELECT i FROM Instance i WHERE i.image = :imageName")
    List<Instance> findAllByImageName(@Param("imageName") String imageName);
}