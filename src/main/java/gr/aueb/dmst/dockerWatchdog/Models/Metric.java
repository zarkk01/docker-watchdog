package gr.aueb.dmst.dockerWatchdog.Models;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

/**
 * This class represents a Metric entity.
 * A Metric has an id and a datetime.
 * It is used as part of our REST API in our Spring Boot application.
 * It is, basically, the model of what we get from Metrics table on our database.
 * In our app, we see it as Changes. Every time an event happens on containers,
 * Metrics table is updated, and we get a new Metric, meaning that a change happened.
 */
@Entity
@Table(name = "Metrics")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // Unique identifier for the metric (number of the change).
    private Integer id;

    @Column(name = "datetime")
    // Datetime of the metric (exact time of the change).
    private Timestamp datetime;

    /**
     * Returns the id of the metric.
     *
     * @return The id of the metric.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id of the metric.
     *
     * @param id The id to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the datetime of the metric.
     *
     * @return The datetime of the metric.
     */
    public Timestamp getDatetime() {
        return datetime;
    }

    /**
     * Sets the datetime of the metric.
     *
     * @param datetime The datetime to set.
     */
    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }
}
