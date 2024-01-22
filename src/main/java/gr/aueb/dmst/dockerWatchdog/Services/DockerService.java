package gr.aueb.dmst.dockerWatchdog.Services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.dockerjava.api.exception.DockerException;

import gr.aueb.dmst.dockerWatchdog.Controllers.ContainersController;
import gr.aueb.dmst.dockerWatchdog.Controllers.ImagesController;
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
 * {@link ExecutorThread} using CompletableFutures so the actions can be performed on the background.
 * Contains methods for actions like starting and stopping
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
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.startContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. started");
        });
    }

    /**
     * Stops a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to stop
     */
    public void stopContainer(String containerId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.stopContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. stopped");
        });
    }

    /**
     * Deletes a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to delete
     */
    public void removeContainer(String containerId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.removeContainer(containerId);
            } catch (ContainerNotFoundException | ContainerRunningException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. removed");
        });
    }

    /**
     * Renames a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to rename
     * @param newName the new name for the Docker container
     */
    public void renameContainer(String containerId, String newName) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.renameContainer(containerId, newName);
            } catch (ContainerNotFoundException | ContainerNameConflictException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Container " + containerId + " renamed to " + newName);
        });
    }

    /**
     * Pauses a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to pause
     */
    public void pauseContainer(String containerId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.pauseContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Container " + containerId + " paused");
        });
    }

    /**
     * Unpauses a Docker container with the given ID calling
     * the right method of Executor Thread.
     *
     * @param containerId the ID of the Docker container to unpause
     */
    public void unpauseContainer(String containerId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.unpauseContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Container " + containerId.substring(0,5) + ".. unpaused");
        });
    }

    /**
     * Restarts a Docker container with the given ID.
     * Basically, it stops and then starts again the container calling
     * the right methods of Executor Thread.
     *
     * @param containerId the ID of the Docker container to restart
     */
    public void restartContainer(String containerId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.stopContainer(containerId);
                ExecutorThread.startContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Container " + containerId.substring(0,5) + ".. restarted");
        });
    }

    /**
     * Creates a Docker container with the given image name.
     * This method uses a CompletableFuture to perform the container creation operation in the background,
     * ensuring that the main thread is not blocked.
     * Once the container creation operation is complete, a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image to use for creating the container
     */
    public void createContainer(String imageName) {
        // Create a CompletableFuture to run the container creation operation in the background
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's runContainer method to create the Docker container
                ExecutorThread.runContainer(imageName);
            } catch (ContainerCreationException | ImageNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container creation operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user
        future.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!", "Container created from " + imageName);
        });
    }

    /**
     * Starts all Docker containers associated with the given image name.
     * This method retrieves all containers associated with the image from the database,
     * and then iterates over them to start each one that is currently stopped.
     * The container start operation is performed asynchronously using a CompletableFuture,
     * ensuring that the main thread is not blocked.
     * Once a container is started, a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image whose containers to start
     */
    public void startAllContainers(String imageName) {
        // Get all containers associated with the image from the database
        List<Instance> containers = instancesRepository.findAllByImageName(imageName);

        // Iterate over the containers
        for (Instance container : containers) {
            // If the container is stopped, start it
            if (container.getStatus().equals("exited")) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Call the ExecutorThread's startContainer method to start the Docker container
                        ExecutorThread.startContainer(container.getId());
                    } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                        // Log any exceptions that occur during the container start operation
                        logger.error(e.getMessage());
                    }
                });

                // Once the CompletableFuture is complete, display a notification to the user
                future.thenRun(() -> {
                    // Call the ImagesController's showNotification method to display the notification
                    ImagesController.showNotification("Woof!", "Container " + container.getName() + " started");
                });
            }
        }
    }

    /**
     * Stops all Docker containers associated with the given image name.
     * This method retrieves all containers associated with the image from the database,
     * and then iterates over them to stop each one that is currently running.
     * The container stop operation is performed asynchronously using a CompletableFuture,
     * ensuring that the main thread is not blocked.
     * Once a container is stopped, a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image whose containers to stop
     */
    public void stopAllContainers(String imageName) {
        // Get all containers associated with the image from the database
        List<Instance> containers = instancesRepository.findAllByImageName(imageName);

        // Iterate over the containers
        for (Instance container : containers) {
            // If the container is running, stop it
            if (container.getStatus().equals("running")) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Call the ExecutorThread's stopContainer method to stop the Docker container
                        ExecutorThread.stopContainer(container.getId());
                    } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                        // Log any exceptions that occur during the container stop operation
                        logger.error(e.getMessage());
                    }
                });

                // Once the CompletableFuture is complete, display a notification to the user
                future.thenRun(() -> {
                    // Call the ImagesController's showNotification method to display the notification
                    ImagesController.showNotification("Woof!", "Container " + container.getName() + " stopped");
                });
            }
        }
    }

    /**
     * Pulls a Docker image with the given name.
     * This method uses a CompletableFuture to perform the image pulling operation in the background,
     * ensuring that the main thread is not blocked.
     * Once the image pulling operation is complete, a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image to pull
     */
    public void pullImage(String imageName) {
        // Create a CompletableFuture to run the image pulling operation in the background
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's pullImage method to pull the Docker image
                ExecutorThread.pullImage(imageName);
            } catch (DockerException | ImageNotFoundException | InterruptedException e) {
                // Log any exceptions that occur during the image pulling operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user
        future.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!", imageName + " pulled");
        });
    }

    /**
     * Asynchronously removes a Docker image with the given name.
     * This method uses a CompletableFuture to perform the image removal operation in the background,
     * ensuring that the main thread is not blocked.
     * Once the image removal operation is complete, a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image to remove
     */
    public void removeImage(String imageName) {
        // Create a CompletableFuture to run the image removal operation in the background.
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's removeImage method to remove the Docker image.
                ExecutorThread.removeImage(imageName);
            } catch (DockerException | ImageNotFoundException e) {
                // Log any exceptions that occur during the image removal operation.
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user.
        future.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification.
            ImagesController.showNotification("Woof!",imageName + " removed");
        });
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
     * Retrieves all Docker instances with the given image name
     * so to display them in the dashboard in Images Panel.
     *
     * @param imageName the name of the image whose instances to retrieve
     * @return a list of Docker instances with the given image name
     */
    public List<Instance> getInstancesByImage(String imageName) {
        return instancesRepository.findAllByImageName(imageName);
    }

    /**
     * Retrieves most recent information about a Docker instance with the given ID.
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

    /**
     * Removes a Docker volume with the given name.
     *
     * @param volumeName the name of the Docker volume to remove
     */
    public void removeVolume(String volumeName) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.removeVolume(volumeName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Volume " + volumeName + " removed");
        });
    }

    /**
     * Creates a Docker volume with the given name.
     *
     * @param volumeName the name of the Docker volume to create
     */
    public void createVolume(String volumeName) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                ExecutorThread.createVolume(volumeName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });

        future.thenRun(() -> {
            System.out.println("Volume " + volumeName + " created");
        });
    }
}
