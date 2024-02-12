package gr.aueb.dmst.dockerWatchdog.api.services;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.dockerjava.api.exception.DockerException;

import gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers.ContainersController;
import gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers.ImagesController;
import gr.aueb.dmst.dockerWatchdog.exceptions.*;
import gr.aueb.dmst.dockerWatchdog.api.entities.Image;
import gr.aueb.dmst.dockerWatchdog.api.entities.Volume;
import gr.aueb.dmst.dockerWatchdog.api.repositories.ImagesRepository;
import gr.aueb.dmst.dockerWatchdog.api.repositories.VolumesRepository;
import gr.aueb.dmst.dockerWatchdog.threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.api.entities.Instance;
import gr.aueb.dmst.dockerWatchdog.api.entities.Metric;
import gr.aueb.dmst.dockerWatchdog.api.repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.api.repositories.MetricsRepository;


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
public class ApiService {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(ExecutorThread.class);

    private final InstancesRepository instancesRepository;
    private final MetricsRepository metricsRepository;
    private final ImagesRepository imagesRepository;
    private final VolumesRepository volumesRepository;

    /**
     * Constructor for ApiService which initializes the repositories
     * for Docker instances, metrics, images, and volumes.
     *
     * @param instancesRepository the repository for Docker instances
     * @param metricsRepository the repository for Docker metrics
     * @param imagesRepository the repository for Docker images
     * @param volumesRepository the repository for Docker volumes
     */
    public ApiService (
            InstancesRepository instancesRepository,
            MetricsRepository metricsRepository,
            ImagesRepository imagesRepository,
            VolumesRepository volumesRepository
    ) throws IOException {
        this.instancesRepository = instancesRepository;
        this.metricsRepository = metricsRepository;
        this.imagesRepository = imagesRepository;
        this.volumesRepository = volumesRepository;
    }

    /**
     * Starts a Docker container with the given ID.
     * This method uses a CompletableFuture to perform the start operation in the background,
     * ensuring that the main thread is not blocked. Also, calls the ContainersController's
     * showLoading method passing true parameter so to display a loading animation
     * to the user while the start operation is in progress. In the end, calls the ContainersController's
     * showLoading method passing false parameter so to hide the loading animation.
     * Once the start operation is complete, a notification is displayed to the user.
     * Any exceptions that occur during the start operation are logged.
     *
     * @param containerId the ID of the Docker container to start
     */
    public void startContainer(String containerId) {
        // Create a CompletableFuture to run the container start operation in the background
        CompletableFuture<Void> startContainerCompletableFuture = CompletableFuture.runAsync(() -> {
            try {
                // Call the ContainersController's showLoading method to display the loading animation
                ContainersController.showLoading(true);
                // Call the ExecutorThread's startContainer method to start the Docker container
                ExecutorThread.startContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container start operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user and hide the loading animation
        startContainerCompletableFuture.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. started.", 3);
            ContainersController.showLoading(false);
        });
    }

    /**
     * Stops a Docker container with the given ID.
     * This method uses a CompletableFuture to perform the stop operation in the background,
     * ensuring that the main thread is not blocked. Also, calls the ContainersController's
     * showLoading method passing true parameter so to display a loading animation
     * to the user while the stop operation is in progress. In the end, calls the ContainersController's
     * showLoading method passing false parameter so to hide the loading animation.
     * Once the stop operation is complete, a notification is displayed to the user.
     * Any exceptions that occur during the stop operation are logged.
     *
     * @param containerId the ID of the Docker container to stop
     */
    public void stopContainer(String containerId) {
        // Create a CompletableFuture to run the container stop operation in the background
        CompletableFuture<Void> stopContainerCompletableFuture = CompletableFuture.runAsync(() -> {
            try {
                // Call the ContainersController's showLoading method to display the loading animation
                ContainersController.showLoading(true);
                // Call the ExecutorThread's stopContainer method to stop the Docker container
                ExecutorThread.stopContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container stop operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user and hide the loading animation
        stopContainerCompletableFuture.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. stopped.", 3);
            ContainersController.showLoading(false);
        });
    }

    /**
     * Deletes a Docker container with the given ID.
     * This method uses a CompletableFuture to perform the delete operation in the background,
     * ensuring that the main thread is not blocked.
     * Once the delete operation is complete, a notification is displayed to the user.
     * Any exceptions that occur during the delete operation are logged.
     *
     * @param containerId the ID of the Docker container to delete
     */
    public void removeContainer(String containerId) {
        // Create a CompletableFuture to run the container delete operation in the background
        CompletableFuture<Void> removeContainerCompletableFuture = CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's removeContainer method to delete the Docker container
                ExecutorThread.removeContainer(containerId);
            } catch (ContainerNotFoundException | ContainerRunningException e) {
                // Log any exceptions that occur during the container delete operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user
        removeContainerCompletableFuture.thenRun(() -> {
            ContainersController.showNotification("Woof!", "Container " + containerId.substring(0,5) + ".. removed.", 3);
        });
    }

    /**
     * Renames a Docker container with the given ID and new name.
     * This method uses a CompletableFuture to perform the rename operation in the background,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the rename operation are logged.
     *
     * @param containerId the ID of the Docker container to rename
     * @param newName the new name for the Docker container
     */
    public void renameContainer(String containerId, String newName) {
        // Use a CompletableFuture to run the container rename operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's renameContainer method to rename the Docker container
                ExecutorThread.renameContainer(containerId, newName);
            } catch (ContainerNotFoundException | ContainerNameConflictException e) {
                // Log any exceptions that occur during the container rename operation
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Pauses a Docker container with the given ID.
     * This method uses a CompletableFuture to perform the pause operation in the background,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the pause operation are logged.
     *
     * @param containerId the ID of the Docker container to pause
     */
    public void pauseContainer(String containerId) {
        // Use a CompletableFuture to run the container pause operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's pauseContainer method to pause the Docker container
                ExecutorThread.pauseContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container pause operation
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Unpauses a Docker container with the given ID.
     * This method uses a CompletableFuture to perform the unpause operation in the background,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the unpause operation are logged.
     *
     * @param containerId the ID of the Docker container to unpause
     */
    public void unpauseContainer(String containerId) {
        // Use a CompletableFuture to run the container unpause operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's unpauseContainer method to unpause the Docker container
                ExecutorThread.unpauseContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container unpause operation
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Restarts a Docker container with the given ID.
     * This method first stops the container and then starts it again.
     * Both operations are performed asynchronously using a CompletableFuture,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the stop or start operations are logged.
     *
     * @param containerId the ID of the Docker container to restart
     */
    public void restartContainer(String containerId) {
        // Use a CompletableFuture to run the container restart operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's stopContainer method to stop the Docker container
                ExecutorThread.stopContainer(containerId);
                // Call the ExecutorThread's startContainer method to start the Docker container
                ExecutorThread.startContainer(containerId);
            } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container restart operation
                logger.error(e.getMessage());
            }
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
     * Creates a Docker container with the given image name.
     * This method uses a CompletableFuture to perform the container creation operation in the background,
     * ensuring that the main thread is not blocked. Also, calls the ImagesController's
     * showLoading method passing true to display the loading animation to the user while the creation is in progress.
     * In the end, calls the ImagesController's showLoading method passing false to hide the loading animation
     * and displays a notification to the user that the container has been created.
     * Any exceptions that occur during the container creation operation are logged.
     *
     * @param imageName the name of the Docker image to use for creating the container
     */
    public void createContainer(String imageName) {
        // Create a CompletableFuture to run the container creation operation in the background
        CompletableFuture<Void> createContainerCompletableFuture = CompletableFuture.runAsync(() -> {
            try {
                // Call the ImagesController's showLoading method passing true to display the loading animation
                ImagesController.showLoading(true);
                // Call the ExecutorThread's runContainer method to create the Docker container
                ExecutorThread.runContainer(imageName);
            } catch (ContainerCreationException | ImageNotFoundException | ContainerNotModifiedException e) {
                // Log any exceptions that occur during the container creation operation
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user and hide the loading animation
        createContainerCompletableFuture.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!", "Container created from " + imageName, 3);
            // Call the ImagesController's showLoading method passing false, so to hide the loading animation
            ImagesController.showLoading(false);
        });
    }

    /**
     * Starts all Docker containers associated with the given image name.
     * This method retrieves all containers associated with the image from the database,
     * and then iterates over them to start each one that is currently stopped.
     * The container start operation is performed asynchronously using a CompletableFuture,
     * ensuring that the main thread is not blocked. Also, calls the ImagesController's
     * showLoading method to display a loading animation to the user while the start operation is
     * in progress. In the end, calls the ImagesController's showLoading method
     * passing false so to hide the loading animation and displays a notification to the user
     * that all containers have been started.
     *
     * @param imageName the name of the Docker image whose containers to start
     */
    public void startAllContainers(String imageName) {
        // Get all containers associated with the image from the database
        List<Instance> containers = instancesRepository.findAllByImageName(imageName);

        // Create a list to hold all the CompletableFuture objects
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Iterate over the containers
        for (Instance container : containers) {
            // If the container is stopped, start it
            if (container.getStatus().equals("exited")) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Call the ImagesController's showLoading method passing true to display the loading animation
                        ImagesController.showLoading(true);
                        // Call the ExecutorThread's startContainer method to start the Docker container
                        ExecutorThread.startContainer(container.getId());
                    } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                        // Log any exceptions that occur during the container start operation
                        logger.error(e.getMessage());
                    }
                });

                // Add the CompletableFuture to the list
                futures.add(future);
            }
        }

        // Create a CompletableFuture that completes when all the futures in the list complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Once all the futures are complete, display a notification to the user and hide the loading animation
        allFutures.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!", "All containers for " + imageName + " started", 3);
            // Call the ImagesController's showLoading method passing false to hide the loading animation
            ImagesController.showLoading(false);
        });
    }

    /**
     * Stops all Docker containers associated with the given image name.
     * This method retrieves all containers associated with the image from the database,
     * and then iterates over them to stop each one that is currently running.
     * The container stop operation is performed asynchronously using a CompletableFuture,
     * ensuring that the main thread is not blocked. Also, calls the ImagesController's
     * showLoading method to display a loading animation to the user while the stop operation is
     * in progress. In the end, calls the ImagesController's showLoading method
     * passing false so to hide the loading animation and displays a notification to the user
     * that all containers have been stopped.
     *
     * @param imageName the name of the Docker image whose containers to stop
     */
    public void stopAllContainers(String imageName) {
        // Get all containers associated with the image from the database
        List<Instance> containers = instancesRepository.findAllByImageName(imageName);

        // Create a list to hold all the CompletableFuture objects
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Iterate over the containers
        for (Instance container : containers) {
            // If the container is running, stop it
            if (container.getStatus().equals("running")) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Call the ImagesController's showLoading method to display the loading animation
                        ImagesController.showLoading(true);
                        // Call the ExecutorThread's stopContainer method to stop the Docker container
                        ExecutorThread.stopContainer(container.getId());
                    } catch (ContainerNotFoundException | ContainerNotModifiedException e) {
                        // Log any exceptions that occur during the container stop operation
                        logger.error(e.getMessage());
                    }
                });

                // Add the CompletableFuture to the list
                futures.add(future);
            }
        }

        // Create a CompletableFuture that completes when all the futures in the list complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Once all the futures are complete, display a notification to the user
        allFutures.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!", "All containers for " + imageName + " stopped", 3);
            // Call the ImagesController's showLoading method passing false so to hide the loading animation
            ImagesController.showLoading(false);
        });
    }

    /**
     * Pulls a Docker image with the given name.
     * This method uses a CompletableFuture to perform the image pulling operation in the background,
     * ensuring that the main thread is not blocked. Also, calls the ImagesController's
     * showLoading method to display a loading animation to the user while the image pulling is
     * in progress. In the end, calls the ImagesController's showLoading method passing
     * false so to hide the loading animation. Once the image pulling operation is complete,
     * a notification is displayed to the user.
     *
     * @param imageName the name of the Docker image to pull
     */
    public void pullImage(String imageName) {
        // Create a CompletableFuture to run the image pulling operation in the background
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // Call the ImagesController's showLoading method to display the loading animation
                ImagesController.showLoading(true);
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
            ImagesController.showNotification("Woof!", imageName + " pulled", 3);
            // Call the ImagesController's showLoading method passing false so to hide the loading animation
            ImagesController.showLoading(false);
        });
    }

    /**
     * Asynchronously removes a Docker image with the given name.
     * This method uses a CompletableFuture to perform the image removal operation in the background,
     * ensuring that the main thread is not blocked. Also, calls the ImagesController's
     * showLoading method to display a loading animation to the user while the image removal is
     * in progress. In the end, calls the ImagesController's showLoading method
     * passing false to hide the loading animation. Once the image removal operation is complete,
     * a notification is displayed to the user.
     * Any exceptions that occur during the image removal operation are logged.
     *
     * @param imageName the name of the Docker image to remove
     */
    public void removeImage(String imageName) {
        // Create a CompletableFuture to run the image removal operation in the background.
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // Call the ImagesController's showLoading method to display the loading animation
                ImagesController.showLoading(true);
                // Call the ExecutorThread's removeImage method to remove the Docker image
                ExecutorThread.removeImage(imageName);
            } catch (DockerException | ImageNotFoundException e) {
                // Log any exceptions that occur during the image removal operation.
                logger.error(e.getMessage());
            }
        });

        // Once the CompletableFuture is complete, display a notification to the user
        future.thenRun(() -> {
            // Call the ImagesController's showNotification method to display the notification
            ImagesController.showNotification("Woof!",imageName + " removed", 3);
            // Call the ImagesController's showLoading method passing false so to hide the loading animation
            ImagesController.showLoading(false);
        });
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
     * This method uses a CompletableFuture to perform the volume removal operation in the background,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the volume removal operation are logged.
     *
     * @param volumeName the name of the Docker volume to remove
     */
    public void removeVolume(String volumeName) {
        // Create a CompletableFuture to run the volume removal operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's removeVolume method to remove the Docker volume
                ExecutorThread.removeVolume(volumeName);
            } catch (Exception e) {
                // Log any exceptions that occur during the volume removal operation
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Creates a Docker volume with the given name.
     * This method uses a CompletableFuture to perform the volume creation operation in the background,
     * ensuring that the main thread is not blocked.
     * Any exceptions that occur during the volume creation operation are logged.
     *
     * @param volumeName the name of the Docker volume to create
     */
    public void createVolume(String volumeName) {
        // Create a CompletableFuture to run the volume creation operation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // Call the ExecutorThread's createVolume method to create the Docker volume
                ExecutorThread.createVolume(volumeName);
            } catch (Exception e) {
                // Log any exceptions that occur during the volume creation operation
                logger.error(e.getMessage());
            }
        });
    }

    /**
     * Retrieves the last metric ID so to understand
     * which is the present state of the containers. In database,
     * there inserts of the same container but with different metric IDs.
     * The last metric ID is the one that represents the present state of the containers,
     * since every time a container changes, metricId is incremented by one for ALL containers.
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
     * which display the number of total containers, running containers, stopped containers
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



    private static final String DOCKER_HUB_LOGIN_URL = "https://hub.docker.com/v2/users/login/";
    public static String authenticateDockerHub(String username, String password) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }");
        Request request = new Request.Builder()
                .url(DOCKER_HUB_LOGIN_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Parse the response body to get the token
            String responseBody = Objects.requireNonNull(response.body()).string();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getString("token");
        }
    }

    private static final String DOCKER_HUB_SEARCH_URL = "https://hub.docker.com/v2/search/repositories/?query=";

    public static JSONArray searchImages(String token, String searchTerm) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(DOCKER_HUB_SEARCH_URL + searchTerm)
                .get()
                .addHeader("Authorization", "JWT " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Parse the response body to get the results
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getJSONArray("results");
        }
    }

}
