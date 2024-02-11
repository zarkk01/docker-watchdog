package gr.aueb.dmst.dockerWatchdog.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


/**
 * This class represents an Image entity.
 * An Image has an id, name, status, and size.
 * It is used as part of our REST API in our Spring Boot application.
 * It is, basically, the model of what we get from Images table on our database.
 */
@Entity
@Table(name = "images")
public class Image {

    @Id
    @Column(name = "id")
    // Unique identifier for the image
    private String id;

    @Column(name = "name")
    // Name of the image
    private String name;

    @Column(name = "status")
    // Status of the image
    private String status;

    @Column(name = "size")
    // Size of the image in bytes
    private Long size;

    /**
     * This method returns the id of the image.
     * @return The id of the image.
     */
    public String getId() {
        return id;
    }

    /**
     * This method sets the id of the image.
     *
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method returns the name of the image.
     *
     * @return The name of the image.
     */
    public String getName() {
        return name;
    }

    /**
     * This method sets the name of the image.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method returns the status of the image.
     *
     * @return The status of the image.
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method sets the status of the image.
     *
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This method returns the size of the image.
     *
     * @return The size of the image.
     */
    public Long getSize() {
        return size;
    }

    /**
     * This method sets the size of the image.
     *
     * @param size The size to set.
     */
    public void setSize(Long size) {
        this.size = size;
    }
}
