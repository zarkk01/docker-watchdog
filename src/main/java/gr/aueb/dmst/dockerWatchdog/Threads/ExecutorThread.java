package gr.aueb.dmst.dockerWatchdog.Threads;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.NoPortException;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExecutorThread implements Runnable {
    // Initiate and create scanner
    Scanner scanner = new Scanner(System.in);

//    private final BlockingQueue<Integer> commandQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
//        while (true) {
//            try {
//                // Wait for a command from the GUI
//                int choice = commandQueue.take();
//                doDependsOnChoice(choice);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }

    }

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
    public void removeContainer(String containerId) {
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
    public void pauseContainer(String containerId) {
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
    public void unpauseContainer(String containerId) {
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
   public void renameContainer(String containerId, String newName) {
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
    public void runContainer(String imageName,Integer sourcePort,Integer targetPort) throws InterruptedException {

            Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

            ExposedPort tcp22 = ExposedPort.tcp(sourcePort);
            Ports portBindings = new Ports();
            portBindings.bind(tcp22, Ports.Binding.bindPort(targetPort));

            CreateContainerResponse container = Main.dockerClient.createContainerCmd(imageName)
                    .withCmd("sleep", "infinity")
                    .withExposedPorts(tcp22)
                    .withPortBindings(portBindings)
                    .exec();

            // Create and start a container based on the pulled image
            Main.dockerClient.startContainerCmd(container.getId()).exec();

            // Calling liveMeasureForNewContainer for this container so start a callback for CPU,Memory etc
            // for this container too
            MonitorThread.liveMeasureForNewContainer(container.getId());

            // Print the container ID
            System.out.println("Container started and running successfully. Container ID: " + container.getId() + "on port: " + sourcePort + ":" + targetPort);
    }

    // Method to pull an image
    public void pullImage(String imageName) {

        // Pull the specified Docker image
        try {
            Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();
            System.out.println("Image pulled successfully.");
        } catch (InterruptedException e) {
            System.out.println("Image pull operation was interrupted.");
            e.printStackTrace();
        } catch (NotFoundException e) {
            System.err.println("The image you are trying to pull does not exist");
        } catch (Exception e) {
            System.err.println("Error pulling the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to execute actions based on the choice received from the GUI
    private void doDependsOnChoice(int choice) {
        switch (choice) {
            case 1:
                // Start Container
                // You can replace "containerId" with the actual container ID you want to start
                startContainer("containerId");
                break;

            case 2:
                // Stop Container
                // You can replace "containerId" with the actual container ID you want to stop
                stopContainer("containerId");
                break;

            case 3:
                // Remove Container
                // You can replace "containerId" with the actual container ID you want to remove
                removeContainer("containerId");
                break;

            case 4:
                // Pause Container
                // You can replace "containerId" with the actual container ID you want to pause
                pauseContainer("containerId");
                break;

            case 5:
                // Unpause Container
                // You can replace "containerId" with the actual container ID you want to unpause
                unpauseContainer("containerId");
                break;

            case 6:
                // Run Container
                try {
                    runContainer("imageName", 22, 22);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 7:
                // Pull Image
                pullImage("imageName");
                break;

            default:
                // Handle invalid choice
                System.out.println("Invalid choice. Please try again.");
        }
    }

//    // Method to receive a command from the GUI
//    public void receiveCommand(int command) {
//        try {
//            // Put the received command into the queue
//            commandQueue.put(command);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
}

