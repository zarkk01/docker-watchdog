package gr.aueb.dmst.dockerWatchdog.model;


import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Metrics")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "datetime")
    private Timestamp datetime;

    public Integer getId() {
        return id;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
