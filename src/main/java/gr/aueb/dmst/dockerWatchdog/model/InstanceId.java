package gr.aueb.dmst.dockerWatchdog.model;

import java.io.Serializable;
import java.util.Objects;

public class InstanceId implements Serializable {

    private String id;
    private Integer metricid;

    // default constructor

    public InstanceId() {}

    public InstanceId(String id, Integer metricid) {
        this.id = id;
        this.metricid = metricid;
    }

    // getters, setters, equals, and hashCode methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMetricid() {
        return metricid;
    }

    public void setMetricId(Integer metricId) {
        this.metricid = metricId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceId that = (InstanceId) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(metricid, that.metricid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metricid);
    }
}