package gr.aueb.dmst.dockerWatchdog.api.entities;

import java.util.Objects;
import java.io.Serializable;


/**
 * This class represents the composite primary key for the Instance entity.
 * The primary key consists of a metricId and an instance id.
 * It is used as part of our REST API in our Spring Boot application.
 */
public class InstanceId implements Serializable {

    // Unique identifier for the instance
    private String id;

    // Metric id for the instance
    private Integer metricid;

    /**
     * Default constructor for the InstanceId class.
     */
    public InstanceId() { }

    /**
     * Constructor for the InstanceId class.
     *
     * @param id The id of the instance.
     * @param metricid The metric id of the instance.
     */
    public InstanceId(String id, Integer metricid) {
        this.id = id;
        this.metricid = metricid;
    }

    /**
     * Returns the id of the instance.
     *
     * @return The id of the instance.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the instance.
     *
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the metric id of the instance.
     *
     * @return The metric id of the instance.
     */
    public Integer getMetricid() {
        return metricid;
    }

    /**
     * Sets the metric id of the instance.
     *
     * @param metricid The metric id to set.
     */
    public void setMetricId(Integer metricid) {
        this.metricid = metricid;
    }

    /**
     * Compares this InstanceId to the specified object.
     *
     * @param o The object to compare this InstanceId against.
     * @return true if the given object represents an InstanceId equivalent to this InstanceId, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstanceId that = (InstanceId) o;
        return Objects.equals(id, that.id)
                && Objects.equals(metricid, that.metricid);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, metricid);
    }
}
