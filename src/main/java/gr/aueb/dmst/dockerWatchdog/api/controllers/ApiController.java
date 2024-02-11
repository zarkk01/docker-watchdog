package gr.aueb.dmst.dockerWatchdog.api.controllers;

import java.util.List;
import java.sql.Timestamp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gr.aueb.dmst.dockerWatchdog.api.entities.Image;
import gr.aueb.dmst.dockerWatchdog.api.entities.Volume;
import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import gr.aueb.dmst.dockerWatchdog.api.entities.Instance;


/**
 * ApiController is a RESTful web service controller that manages Docker operations,
 * and it is basically the backbone of our backend.
 * It provides endpoints for starting, stopping, restarting, deleting,
 * renaming, pausing, and unpausing Docker containers.
 * It also provides endpoints for retrieving all instances,
 * metrics, the last metric ID, and specific instance information.
 * Furthermore, it provides endpoints for starting all containers
 * of a specific image, stopping all containers of a specific image,
 * creating a container from a specific image, pulling an image,
 * removing an image, and retrieving all images and volumes.
 * Each operation is performed by calling the corresponding method of a ApiService instance.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    /**
     * Constructs the ApiController with the specified ApiService.
     *
     * @param apiService the ApiService to be used by the ApiController
     */
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Endpoint for HTTP POST request to start a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be started
     * @return a ResponseEntity with a message indicating the container has been started
     */
    @PostMapping("/containers/{containerId}/start")
    public ResponseEntity<String> startContainer(@PathVariable("containerId") String containerId) {
        apiService.startContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " started");
    }

    /**
     * Endpoint for HTTP POST request to stop a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be stopped
     * @return a ResponseEntity with a message indicating the container has been stopped
     */
    @PostMapping("/containers/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable("containerId") String containerId) {
        apiService.stopContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " stopped");
    }

    /**
     * Endpoint for HTTP POST request to restart a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be restarted
     * @return a ResponseEntity with a message indicating the container has been restarted
     */
    @PostMapping("/containers/{containerId}/restart")
    public ResponseEntity<String> restartContainer(@PathVariable("containerId") String containerId) {
        apiService.restartContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " restarted");
    }

    /**
     * Endpoint for HTTP POST request to delete a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be deleted
     * @return a ResponseEntity with a message indicating the container has been deleted
     */
    @PostMapping("/containers/{containerId}/delete")
    public ResponseEntity<String> deleteContainer(@PathVariable("containerId") String containerId){
        apiService.removeContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + " deleted"));
    }

    /**
     * Endpoint for HTTP POST request to rename Docker container with the specified ID to a new name.
     *
     * @param containerId the ID of the Docker container to be renamed
     * @param newName the new name for the Docker container
     * @return a ResponseEntity with a message indicating the container has been renamed
     */
    @PostMapping("/containers/{containerId}/rename")
    public ResponseEntity<String> renameContainer(@PathVariable("containerId") String containerId,
                                                  @RequestParam("newName") String newName) {
        apiService.renameContainer(containerId,newName);
        return ResponseEntity.ok(("Container " + containerId + " renamed"));
    }

    /**
     * Endpoint for HTTP POST request to pause a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be paused
     * @return a ResponseEntity with a message indicating the container has been paused
     */
    @PostMapping("/containers/{containerId}/pause")
    public ResponseEntity<String> pauseContainer(@PathVariable("containerId") String containerId) {
        apiService.pauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "paused"));
    }

    /**
     * Endpoint for HTTP POST request to unpause a Docker container with the specified ID.
     *
     * @param containerId the ID of the Docker container to be unpaused
     * @return a ResponseEntity with a message indicating the container has been unpaused
     */
    @PostMapping("/containers/{containerId}/unpause")
    public ResponseEntity<String> unpauseContainer(@PathVariable("containerId") String containerId) {
        apiService.unpauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "unpaused"));
    }

    /**
     * Endpoint for HTTP GET request to retrieve all instances of Docker containers.
     * It is used by lots of panels in the application so to take information about the containers.
     *
     * @return a list of all instances of Docker containers
     */
    @GetMapping("/containers/instances")
    public List<Instance> getAllInstances() {
        return apiService.getAllInstancesMaxId();
    }

    /**
     * Endpoint for HTTP GET request to retrieve metrics for Docker containers for a specified date.
     * It is used for the functionality in the 4 boxes in the main panel.
     *
     * @param chosenDateString the date for which to retrieve metrics
     * @return a list of metrics for Docker containers for the specified date
     */
    @GetMapping("/containers/metrics")
    public List<Long> getMetrics(@RequestParam("chosenDate") String chosenDateString) {
        Timestamp chosenDate = Timestamp.valueOf(chosenDateString);
        return apiService.getMetrics(chosenDate);
    }

    /**
     * Endpoint for HTTP GET request to retrieve the ID of the last metric for Docker containers.
     * It is used so to understand what is the current state of the Docker Cluster.
     *
     * @return the ID of the last metric for Docker containers
     */
    @GetMapping("/containers/lastMetricId")
    public Integer getLastMetricId() {
        return apiService.getLastMetricId();
    }

    /**
     * Endpoint for HTTP GET request to retrieve information for a specific instance of a Docker container.
     * Used mainly in individual container panels.
     *
     * @param containerId the ID of the Docker container for which to retrieve information
     * @return information for the specified instance of a Docker container
     */
    @GetMapping("/containers/{containerId}/info")
    public Instance getInstanceInfo(@PathVariable("containerId") String containerId) {
        return apiService.getInstanceInfo(containerId);
    }

    /**
     * Endpoint for HTTP POST request to start all Docker containers for a specific image.
     * Used in Images panel to start all containers of a specific image.
     *
     * @param imageName the name of the image for which to start all Docker containers
     * @return a ResponseEntity with a message indicating all Docker
     * containers for the specified image have been started
     */
    @PostMapping("/containers/startAll/{imageName}")
    public ResponseEntity<String> startAllContainers(@PathVariable("imageName") String imageName) {
        apiService.startAllContainers(imageName);
        return ResponseEntity.ok("All containers started");
    }

    /**
     * Endpoint for HTTP POST request to stop all Docker containers for a specific image.
     * Used in Images panel to stop all containers of a specific image.
     *
     * @param imageName the name of the image for which to stop all Docker containers
     * @return a ResponseEntity with a message indicating all Docker
     * containers for the specified image have been stopped
     */
    @PostMapping("/containers/stopAll/{imageName}")
    public ResponseEntity<String> stopAllContainers(@PathVariable("imageName") String imageName) {
        apiService.stopAllContainers(imageName);
        return ResponseEntity.ok("All containers stopped");
    }

    /**
     * Endpoint for HTTP POST request to create a Docker container from a specific image.
     * Used in Images panel to create a container from a specific image.
     *
     * @param imageName the name of the image from which to create a Docker container
     * @return a ResponseEntity with a message indicating a Docker
     * container has been created from the specified image
     */
    @PostMapping("/images/create/{imageName}")
    public ResponseEntity<String> createContainer(@PathVariable("imageName") String imageName) {
        apiService.createContainer(imageName);
        return ResponseEntity.ok("Container created");
    }

    /**
     * Endpoint for HTTP GET request to retrieve all Docker containers for a specific image.
     * Used in Images panel so when a user clicks on an image to see all the containers
     * and info about them in the info panel.
     *
     * @param imageName the name of the image for which to retrieve all Docker containers
     * @return a list of all Docker containers for the specified image
     */
    @GetMapping("/images/getContainers/{imageName}")
    public List<Instance> getInstancesByImage(@PathVariable("imageName") String imageName) {
        return apiService.getInstancesByImage(imageName);
    }

    /**
     * Endpoint for HTTP POST request to pull a specific Docker image.
     * Used in Images panel to pull a specific Docker image.
     *
     * @param imageName the name of the Docker image to pull
     * @return a ResponseEntity with a message indicating the specified
     * Docker image has been pulled
     */
    @PostMapping("/images/pull/{imageName}")
    public ResponseEntity<String> pullImage(@PathVariable("imageName") String imageName) {
        apiService.pullImage(imageName);
        return ResponseEntity.ok("Image pulled " + imageName);
    }

    /**
     * Endpoint for HTTP POST request to remove a specific Docker image.
     * Used in Images panel to remove a specific Docker image.
     *
     * @param imageName the name of the Docker image to remove
     * @return a ResponseEntity with a message indicating the specified Docker
     * image has been removed
     */
    @PostMapping("/images/remove/{imageName}")
    public ResponseEntity<String> removeImage(@PathVariable("imageName") String imageName) {
        apiService.removeImage(imageName);
        return ResponseEntity.ok("Image removed " + imageName);
    }

    /**
     * Endpoint for HTTP GET request to retrieve all Docker images.
     *
     * @return a list of all Docker images
     */
    @GetMapping("/images")
    public List<Image> getAllImages() {
        return apiService.getAllImages();
    }

    /**
     * Endpoint for HTTP GET request to retrieve all Docker volumes.
     *
     * @return a list of all Docker volumes
     */
    @GetMapping("/volumes")
    public List<Volume> getAllVolumes() {
        return apiService.getAllVolumes();
    }

    /**
     * Endpoint for HTTP POST request to remove a Docker volume with the specified name.
     *
     * @param volumeName the name of the Docker volume to be deleted
     * @return a ResponseEntity with a message indicating the volume has been deleted
     */
    @PostMapping("/volumes/{volumeName}/remove")
    public ResponseEntity<String> deleteVolume(@PathVariable("volumeName") String volumeName) {
        apiService.removeVolume(volumeName);
        return ResponseEntity.ok(("Volume " + volumeName + " deleted"));
    }

    /**
     * Endpoint for HTTP POST request to create a Docker volume with the specified name.
     *
     * @param volumeName the name of the Docker volume to be created
     * @return a ResponseEntity with a message indicating the volume has been created
     */
    @PostMapping("/volumes/{volumeName}/create")
    public ResponseEntity<String> createVolume(@PathVariable("volumeName") String volumeName){
        apiService.createVolume(volumeName);
        return ResponseEntity.ok(("Volume " + volumeName + " created"));
    }
}

