package gr.aueb.dmst.dockerWatchdog.Threads;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.PullImageResultCallback;
import gr.aueb.dmst.dockerWatchdog.Main;

public class ExecutorThread implements Runnable {
    @Override
    public void run() {}

    // Method to start a container
    public static void startContainer(String containerId) {
        try {
            // Start the container with the provided ID
            System.out.println("Starting the container with ID " + containerId + "...");
            Main.dockerClient.startContainerCmd(containerId).exec();
            System.out.println("Container started successfully.");
        } catch (NotFoundException e) {
            // If the container is not found
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " not found." + "\033[0m");
        } catch (NotModifiedException e) {
            // If the container is already running
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " is already running." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to stop a container
    public static void stopContainer(String containerId) {
        try {
            // Stop the container with the provided ID
            System.out.println("Stopping the container with ID " + containerId + "...");
            Main.dockerClient.stopContainerCmd(containerId).exec();
            System.out.println("Container stopped successfully.");
        } catch (NotFoundException e) {
            // If the container is not found
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " not found." + "\033[0m");
        } catch (NotModifiedException e) {
            // If the container is already stopped
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " is already stopped." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to remove a container
    public static void removeContainer(String containerId) {
        try {
            // Remove the container with the provided ID
            System.out.println("Removing the container with ID " + containerId + "...");
            Main.dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("Container removed successfully.");
        } catch (NotFoundException e) {
            // If the container is not found
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " not found." + "\033[0m");
        } catch (ConflictException e) {
            // If the container is already running
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " is currently running. Try stopping it first." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to pause a container
    public static void pauseContainer(String containerId) {
        try {
            // Pause the specified container
            System.out.println("Pausing the container with ID " + containerId + "...");
            Main.dockerClient.pauseContainerCmd(containerId).exec();
            System.out.println("Container paused successfully.");
        } catch (NotFoundException e) {
            // If the container is not found
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " not found." + "\033[0m");
        } catch (ConflictException e) {
            // If the container is already paused or exited
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " is already paused or exited." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to unpause a container
    public static void unpauseContainer(String containerId) {
        try {
            // Unpause the specified container
            System.out.println("Unpausing the container with ID " + containerId + "...");
            Main.dockerClient.unpauseContainerCmd(containerId).exec();
            System.out.println("Container unpaused successfully.");
        } catch (NotFoundException e) {
            // If the container is not found
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " not found." + "\033[0m");
        } catch (ConflictException e) {
            // If the container is not paused
            System.out.println("\033[0;31m" + "Container with ID " + containerId + " is not paused." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to rename a container
    public static void renameContainer(String containerId, String newName) {
        try {
            // Rename the specified container
            Main.dockerClient.renameContainerCmd(containerId)
                    .withName(newName)
                    .exec();
            System.out.println("Container renamed successfully.");
        } catch (ConflictException e) {
            // If there is a container with the same name
            System.out.println("\033[0;31m" + "You can't name this container this way because there is another container by this name" + "\033[0m");
        }
    }

    // Method to run a container
    public static void runContainer(String imageName) throws InterruptedException {

        Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

        CreateContainerResponse container = Main.dockerClient.createContainerCmd(imageName)
                .withCmd("sleep", "infinity")
                .exec();

        // Create and start a container based on the pulled image
        Main.dockerClient.startContainerCmd(container.getId()).exec();

        // Print the container ID
        System.out.println("Container started and running successfully. Container ID: " + container.getId());
    }

    // Method to pull an image from Docker Hub
    public static void pullImage(String imageName) {
        try {
            // Pull the image with the provided name
            System.out.println("Pulling the image " + imageName + "...");
            Main.dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
            System.out.println("Image pulled successfully.");
        } catch (NotFoundException e) {
            // If the image is not found
            System.out.println("\033[0;31m" + "Image " + imageName + " not found on Docker Hub." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    // Method to remove an image from Docker
    public static void removeImage(String imageName) {
        try {
            // Remove the image with the provided name
            System.out.println("Removing the image " + imageName + "...");
            Main.dockerClient.removeImageCmd(imageName).exec();
            System.out.println("Image removed successfully.");
        } catch (NotFoundException e) {
            // If the image is not found
            System.out.println("\033[0;31m" + "Image " + imageName + " not found." + "\033[0m");
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }
}