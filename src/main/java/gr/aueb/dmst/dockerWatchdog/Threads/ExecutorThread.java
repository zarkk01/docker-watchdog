package gr.aueb.dmst.dockerWatchdog.Threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.command.PullImageResultCallback;

import gr.aueb.dmst.dockerWatchdog.Exceptions.*;
import gr.aueb.dmst.dockerWatchdog.Main;

/**
 * ExecutorThread is a class that provides methods for managing Docker containers and images.
 * It is where the actual Docker actions are performed.
 * It uses the Docker Java API to interact with the Docker daemon and perform operations
 * such as starting, stopping, pausing, unpausing, renaming, running, pulling, and removing containers.
 * Each method corresponds to a specific Docker operation and throws custom exceptions to handle errors.
 */
public class ExecutorThread implements Runnable {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(ExecutorThread.class);

    // Run method is empty because it is used only for creating the thread
    @Override
    public void run() {
    }

    /**
     * Starts a Docker container with the given ID after checking if it is not already running, paused, or dead.
     *
     * @param containerId the ID of the container to start
     * @throws ContainerNotFoundException    if the container with the given ID is not found
     * @throws ContainerNotModifiedException if the container with the given ID is already running, paused, or dead
     */
    public static void startContainer(String containerId) throws ContainerNotFoundException, ContainerNotModifiedException {
        try {
            // Check if the container is already running, paused, or dead
            if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning()) {
                // If the container is already running, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is already running.");
            } else if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getPaused()) {
                // If the container is paused, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is paused.");
            } else if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getDead()) {
                // If the container is dead, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is dead.");
            }

            // This is where the container is actually started
            Main.dockerClient.startContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Stops a Docker container with the given ID after checking if it is not already stopped, paused, or dead.
     *
     * @param containerId the ID of the container to stop
     * @throws ContainerNotFoundException    if the container with the given ID is not found
     * @throws ContainerNotModifiedException if the container with the given ID is already stopped, paused, or dead
     */
    public static void stopContainer(String containerId) throws ContainerNotFoundException, ContainerNotModifiedException {
        try {
            // Check if the container is already stopped, paused, or dead
            if (!Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning()) {
                // If the container is already stopped, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is already stopped.");
            } else if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getPaused()) {
                // If the container is paused, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is paused.");
            } else if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getDead()) {
                // If the container is dead, throw a ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is dead.");
            }

            // This is where the container is actually stopped
            Main.dockerClient.stopContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Removes a Docker container with the given ID after checking if it is not currently running.
     *
     * @param containerId the ID of the container to remove
     * @throws ContainerNotFoundException if the container with the given ID is not found
     * @throws ContainerRunningException  if the container with the given ID is currently running
     */
    public static void removeContainer(String containerId) throws ContainerNotFoundException, ContainerRunningException {
        try {
            // Check if the container is running
            if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning()) {
                // If the container is running, throw a ContainerRunningException
                throw new ContainerRunningException(containerId);
            }

            // This is where the container is actually removed.
            Main.dockerClient.removeContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Pauses a Docker container with the given ID after checking if it is not already paused or not running.
     *
     * @param containerId the ID of the container to pause
     * @throws ContainerNotFoundException    if the container with the given ID is not found
     * @throws ContainerNotModifiedException if the container with the given ID is already paused
     *                                       or if it is not running
     */
    public static void pauseContainer(String containerId) throws ContainerNotFoundException, ContainerNotModifiedException {
        try {
            // Check if the container is already paused or not running
            if (Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getPaused()) {
                // If the container is already paused, throw an ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is already paused.");
            } else if (!Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning()) {
                // If the container is not running, throw an ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is not running.");
            }

            // This is where the container is actually paused
            Main.dockerClient.pauseContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Unpauses a Docker container with the given ID after checking if it is not already paused.
     *
     * @param containerId the ID of the container to unpause
     * @throws ContainerNotFoundException    if the container with the given ID is not found
     * @throws ContainerNotModifiedException if the container with the given ID is not paused
     */
    public static void unpauseContainer(String containerId) throws ContainerNotFoundException, ContainerNotModifiedException {
        try {
            // Check if the container is not paused
            if (!Main.dockerClient.inspectContainerCmd(containerId).exec().getState().getPaused()) {
                // If the container is not paused, throw an ContainerNotModifiedException
                throw new ContainerNotModifiedException(containerId, "Container is not paused.");
            }

            // This is where the container is actually unpaused
            Main.dockerClient.unpauseContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Renames a Docker container with the given ID.
     *
     * @param containerId the ID of the container to rename
     * @param newName     the new name for the container
     * @throws ContainerNotFoundException     if the container with the given ID is not found
     * @throws ContainerNameConflictException if the new name is already used by another container
     */
    public static void renameContainer(String containerId, String newName) throws ContainerNotFoundException, ContainerNameConflictException {
        try {
            // This is where the container is actually renamed
            Main.dockerClient.renameContainerCmd(containerId)
                    .withName(newName)
                    .exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException(containerId);
        } catch (ConflictException e) {
            throw new ContainerNameConflictException(containerId, newName);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    /**
     * Creates and starts a Docker container with the given image name.
     *
     * @param imageName the name of the Docker image to use for creating the container
     * @throws ImageNotFoundException        if the image with the given name is not found
     * @throws ContainerCreationException    if the container cannot be created
     * @throws ContainerNotModifiedException if the container cannot be started
     */
    public static void runContainer(String imageName) throws ImageNotFoundException, ContainerCreationException, ContainerNotModifiedException {
        CreateContainerResponse container;
        try {
            // This is where the container is actually being created
            container = Main.dockerClient.createContainerCmd(imageName)
                    .withCmd("sleep", "infinity")
                    .exec();
        } catch (NotFoundException e) {
            throw new ImageNotFoundException(imageName);
        } catch (DockerException e) {
            throw new ContainerCreationException("Failed to create container from image " + imageName);
        }

        try {
            // Here, the newborn container is doing its first steps meaning it is being started
            Main.dockerClient.startContainerCmd(container.getId()).exec();
        } catch (DockerException e) {
            throw new ContainerNotModifiedException(container.getId(), " Failed to start container.");
        }
    }

    /**
     * Pulls a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to pull
     * @throws ImageNotFoundException if the image with the given name is not found
     * @throws DockerException        if there's a general Docker error
     */
    public static void pullImage(String imageName) throws ImageNotFoundException, DockerException, InterruptedException {
        try {
            // This is where the image is actually pulled
            Main.dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
        } catch (NotFoundException e) {
            throw new ImageNotFoundException(imageName);
        } catch (DockerException e) {
            throw new DockerException("General Docker error.", e.getHttpStatus());
        }
    }

    /**
     * Removes a Docker image with the given name.
     *
     * @param imageName the name of the Docker image to remove
     * @throws ImageNotFoundException if the image with the given name is not found
     */
    public static void removeImage(String imageName) throws ImageNotFoundException {
        try {
            // This is where the image is actually removed
            Main.dockerClient.removeImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            throw new ImageNotFoundException(imageName);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    /**
     * Removes a Docker volume with the given name.
     *
     * @param volumeName the name of the Docker volume to remove
     * @throws VolumeNotModifiedException if the volume with the given name is not found
     */
    public static void removeVolume(String volumeName) throws VolumeNotModifiedException {
        try {
            // This is where the volume is actually removed.
            Main.dockerClient.removeVolumeCmd(volumeName).exec();
        } catch (Exception e) {
            throw new VolumeNotModifiedException(volumeName);
        }
    }

    /**
     * Creates a Docker volume with the given name.
     *
     * @param volumeName the name of the Docker volume to create
     * @throws VolumeNotModifiedException if the volume cannot be created
     */
    public static void createVolume(String volumeName) throws VolumeNotModifiedException {
        try {
            // This is where the volume is actually created
            Main.dockerClient.createVolumeCmd().withName(volumeName).exec();
        } catch (Exception e) {
            throw new VolumeNotModifiedException(volumeName);
        }
    }
}
