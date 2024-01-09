package gr.aueb.dmst.dockerWatchdog.Services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import gr.aueb.dmst.dockerWatchdog.Repositories.ImagesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.VolumesRepository;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;

/**
 * SpringBoot's Service class for managing Docker instances, images, volumes
 * and changes(metrics).
 * Executes the logic of our REST API, calling the appropriate methods from
 * {@link ExecutorThread}. Contains methods for actions like starting and stopping
 * containers and also methods for retrieving data from the database.
 */
@Service
@Transactional
public class DockerService {

    private final InstancesRepository instancesRepository;
    private final MetricsRepository metricsRepository;
    private final ImagesRepository imagesRepository;
    private final VolumesRepository volumesRepository;

    /**
     * Constructor for DockerService.
     *
     * @param instancesRepository the repository for Docker instances
     * @param metricsRepository the repository for Docker metrics
     * @param imagesRepository the repository for Docker images
     * @param volumesRepository the repository for Docker volumes
     */
    public DockerService(InstancesRepository instancesRepository,
                         MetricsRepository metricsRepository,
                         ImagesRepository imagesRepository,
                         VolumesRepository volumesRepository) {
        this.instancesRepository = instancesRepository;
        this.metricsRepository = metricsRepository;
        this.imagesRepository = imagesRepository;
        this.volumesRepository = volumesRepository;
    }

    /**
     * Starts a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to start
     */
    public void startContainer(String containerId) {
        ExecutorThread.startContainer(containerId);
    }

    /**
     * Stops a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to stop
     */
    public void stopContainer(String containerId) {
        ExecutorThread.stopContainer(containerId);
    }

    /**
     * Deletes a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to delete
     */
    public void deleteContainer(String containerId) {
        ExecutorThread.removeContainer(containerId);
    }

    /**
     * Renames a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to rename
     * @param newName the new name for the Docker container
     */
    public void renameContainer(String containerId, String newName) {
        ExecutorThread.renameContainer(containerId, newName);
    }

    /**
     * Pauses a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to pause
     */
    public void pauseContainer(String containerId) {
        ExecutorThread.pauseContainer(containerId);
    }

    /**
     * Unpauses a Docker container with the given ID.
     *
     * @param containerId the ID of the Docker container to unpause
     */
    public void unpauseContainer(String containerId) {
        ExecutorThread.unpauseContainer(containerId);
    }

    /**
     * Restarts a Docker container with the given ID.
     * Basically, it stops and then starts again the container.
     *
     * @param containerId the ID of the Docker container to restart
     */
    public void restartContainer(String containerId) {
        ExecutorThread.stopContainer(containerId);
        ExecutorThread.startContainer(containerId);
    }

    /**
     * Creates a Docker container with the given image name
     * while if the image is not pulled, it pulls it first.
     *
     * @param imageName the name of the Docker image to use for creating the container
     */
    public void createContainer(String imageName) {
        try {
            ExecutorThread.runContainer(imageName);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Starts all Docker containers with the given image name
     * searching for them in the database.
     *
     * @param imageName the name of the Docker image whose containers to start
     */
    public void startAllContainers(String imageName) {
        List<Instance> containers =
                instancesRepository.findAllByImageName(imageName);
        for (Instance container : containers) {
            ExecutorThread.startContainer(container.getId());
        }
    }

    /**
     * Stops all Docker containers with the given image name
     * searching for them in the database.
     *
     * @param imageName the name of the Docker image whose containers to stop
     */
    public void stopAllContainers(String imageName) {
        List<Instance> containers =
                instancesRepository.findAllByImageName(imageName);
        for (Instance container : containers) {
            ExecutorThread.stopContainer(container.getId());
        }
    }

    /**
     * Pulls a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to pull
     */
    public void pullImage(String imageName) {
        ExecutorThread.pullImage(imageName);
    }

    /**
     * Removes a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to remove
     */
    public void removeImage(String imageName) {
        ExecutorThread.removeImage(imageName);
    }

    /**
     * Retrieves all Docker instances with the maximum metric ID.
     *
     * @return a list of Docker instances with the maximum metric ID
     */
    public List<Instance> getAllInstancesMaxId() {
        return instancesRepository.findAllByMaxMetricId();
    }

    /**
     * Retrieves information about a Docker instance with the given ID.
     *
     * @param id the ID of the Docker instance to retrieve information about
     * @return the Docker instance with the given ID
     */
    public Instance getInstanceInfo(String id) {
        return instancesRepository.findByContainerId(id);
    }

    /**
     * Retrieves the last metric ID so understand
     * which is the present / latest state of the containers.
     *
     * @return the last metric ID
     */
    public Integer getLastMetricId() {
        return metricsRepository.findLastMetricId();
    }

    /**
     * Retrieves metrics from database before the given timestamp.
     * Also retrieves the number of running, total and stopped containers.
     * This method is used for our 4 panes in the main window
     * which display the number of changes, running containers, total containers
     * and stopped containers.
     *
     * @param chosenDate the timestamp to retrieve metrics before
     * @return a list of metrics before the given timestamp
     */
    public List<Long> getMetrics(Timestamp chosenDate) {
        long manyMetrics = metricsRepository.countByDatetimeBefore(chosenDate);
        Optional<Metric> metricOptional =
                metricsRepository.
                        findFirstByDatetimeBeforeOrderByDatetimeDesc(chosenDate);
        int wantedId = 0;
        if (metricOptional.isPresent()) {
            wantedId =  metricOptional.get().getId();
        }
        long runningContainers =
                instancesRepository.countByMetricIdAndStatusRunning(wantedId);
        long totalContainers =
                instancesRepository.findAllByMetricId(wantedId);
        long stoppedContainers = totalContainers - runningContainers;
        return List.of(
                manyMetrics,
                runningContainers,
                totalContainers,
                stoppedContainers);
    }

    /**
     * Retrieves all pulled Docker images from the database
     *
     * @return a list of all pulled Docker images
     */
    public List<Image> getAllImages() {
        return imagesRepository.findAll();
    }

    /**
     * Retrieves all Docker volumes.
     *
     * @return a list of all Docker volumes
     */
    public List<Volume> getAllVolumes() {
        return volumesRepository.findAll();
    }
}
