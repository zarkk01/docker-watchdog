package gr.aueb.dmst.dockerWatchdog.Services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.exception.DockerException;

import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerNameConflictException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerNotFoundException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerNotModifiedException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerRunningException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ImageNotFoundException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ContainerCreationException;
import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import gr.aueb.dmst.dockerWatchdog.Repositories.ImagesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.VolumesRepository;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(ExecutorThread.class);

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
     * Starts a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to start
     */
    public void startContainer(String containerId) {
        try {
            ExecutorThread.startContainer(containerId);
        } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Stops a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to stop
     */
    public void stopContainer(String containerId) {
        try {
            ExecutorThread.stopContainer(containerId);
        } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Deletes a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to delete
     */
    public void removeContainer(String containerId) {
        try {
            ExecutorThread.removeContainer(containerId);
        } catch (ContainerNotFoundException | ContainerRunningException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Renames a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to rename
     * @param newName the new name for the Docker container
     */
    public void renameContainer(String containerId, String newName) {
        try {
            ExecutorThread.renameContainer(containerId, newName);
        } catch (ContainerNotFoundException | ContainerNameConflictException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Pauses a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to pause
     */
    public void pauseContainer(String containerId) {
        try {
            ExecutorThread.pauseContainer(containerId);
        } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Unpauses a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to unpause
     */
    public void unpauseContainer(String containerId) {
        try {
            ExecutorThread.unpauseContainer(containerId);
        } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Restarts a Docker container with the given ID.
     * Basically, it stops and then starts again the container calling
     * the right methods of Executor Thread.
     *
     * @param containerId the ID of the Docker container to restart
     */
    public void restartContainer(String containerId) {
        try {
            ExecutorThread.stopContainer(containerId);
            ExecutorThread.startContainer(containerId);
        } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Creates a Docker container with the given image name, calling
     * the right method of Executor Thread.
     *
     * @param imageName the name of the Docker image to use for creating the container
     */
    public void createContainer(String imageName) {
        try {
            ExecutorThread.runContainer(imageName);
        } catch (ImageNotFoundException | ContainerCreationException | ContainerNotModifiedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Starts all Docker containers with the given image name
     * searching for them in the database.
     *
     * @param imageName the name of the Docker image whose containers to start
     * */
    public void startAllContainers(String imageName) {
        // Get all containers and then iterate them to start them
        List<Instance> containers =
                instancesRepository.findAllByImageName(imageName);
        for (Instance container : containers) {
            try {
                ExecutorThread.startContainer(container.getId());
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Stops all Docker containers with the given image name
     * searching for them in the database.
     *
     * @param imageName the name of the Docker image whose containers to stop
     */
    public void stopAllContainers(String imageName) {
        // Get all containers and then iterate them to stop them
        List<Instance> containers =
                instancesRepository.findAllByImageName(imageName);
        for (Instance container : containers) {
            try {
                ExecutorThread.stopContainer(container.getId());
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Pulls a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to pull
     */
    public void pullImage(String imageName) {
        try {
            ExecutorThread.pullImage(imageName);
        } catch (ImageNotFoundException | DockerException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Removes a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to remove
     */
    public void removeImage(String imageName) {
        try {
            ExecutorThread.removeImage(imageName);
        } catch (ImageNotFoundException e) {
            logger.error(e.getMessage());
        }
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
     * Retrieves the last metric ID so to understand
     * which is the present state of the containers.
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
     * which display the number of total containers, running containers, stoped containers
     * and changes.
     *
     * @param chosenDate the timestamp to retrieve metrics before
     * @return a list of metrics before the given timestamp
     */
    public List<Long> getMetrics(Timestamp chosenDate) {
        // Get the number of metrics before the given timestamp
        long manyMetrics = metricsRepository.countByDatetimeBefore(chosenDate);
        // Get the maximum metric ID before the given timestamp
        Optional<Metric> metricOptional =
                metricsRepository.
                        findFirstByDatetimeBeforeOrderByDatetimeDesc(chosenDate);
        int wantedId = 0;
        if (metricOptional.isPresent()) {
            // Assign the maximum metric ID to wantedId
            wantedId =  metricOptional.get().getId();
        }
        // Given the metricId, get the number of running and total containers
        long runningContainers =
                instancesRepository.countByMetricIdAndStatusRunning(wantedId);
        long totalContainers =
                instancesRepository.findAllByMetricId(wantedId);
        long stoppedContainers = totalContainers - runningContainers;
        // Return a list of the information we retrieved
        return List.of(
                manyMetrics,
                runningContainers,
                totalContainers,
                stoppedContainers);
    }

    /**
     * Retrieves all pulled Docker images from the database
     * so to display them and do actions on them
     * on our Images panel.
     *
     * @return a list of all pulled Docker images
     */
    public List<Image> getAllImages() {
        return imagesRepository.findAll();
    }

    /**
     * Retrieves all Docker volumes so to display them
     * in our Volumes panel.
     *
     * @return a list of all Docker volumes
     */
    public List<Volume> getAllVolumes() {
        return volumesRepository.findAll();
    }
}
