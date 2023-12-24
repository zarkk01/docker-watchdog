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

    private final BlockingQueue<Integer> commandQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (true) {
            try {
                // Wait for a command from the GUI
                int choice = commandQueue.take();
                doDependsOnChoice(choice);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

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
    /*
    public void startContainer() {

        System.out.println("\nAvailable containers to start : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Exited") || curIns.getStatus().startsWith("Created")) {
                // Available containers to start
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Already running containers
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already running)");
            }
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If there are no containers to start
            if (Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() == 0) {
                System.out.println("\nThere are no containers to run");
            } else {
                System.out.println("\nAll containers are running.");
            }
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to start: ");
            int containerNumber = scanner.nextInt() - 1;

            // Check if the number is valid
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);
            try {
                //Start the container
                System.out.println("Starting the container " + container.getNames()[0].substring(1) + "...");
                Main.dockerClient.startContainerCmd(container.getId()).exec();
                System.out.println("Container started successfully.");

            } catch (NotModifiedException e) {
                // If the container is already running
                System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already running please try again with another container" + "\033[0m");
            }
        }catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }
    }*/

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
    /*
    public void stopContainer() {
        System.out.println("\nAvailable containers to stop : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Up")) {
                // Available containers to stop
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else if (curIns.getStatus().startsWith("Created")){
                // Already exited containers
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Hasn't started yet)");
            } else {
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already exited)");
            }
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If there are no containers to stop
            if (Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() == 0) {
                System.out.println("\nThere are no containers to stop");
            } else {
                System.out.println("\nAll containers are stopped.");
            }
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to stop: ");
            int containerNumber = scanner.nextInt() - 1;
            // Check if the number is valid
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);
            try {
                // Stop the specified container
                System.out.println("Stopping the container " + container.getNames()[0].substring(1) + "...");
                Main.dockerClient.stopContainerCmd(container.getId()).exec();
                System.out.println("Container stopped successfully.");
            } catch (NotModifiedException e) {
                System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already stopped." + "\033[0m");
            }
        }catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }
    }*/

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

    /*
    public void removeContainer() {

        // Show the available containers to remove
        System.out.println("\nAvailable containers to remove : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Exited") || curIns.getStatus().startsWith("Created")) {
                // Available containers to remove
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Already running containers
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Running)");
            }
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() == 0) {
                // If there are no containers to remove
                System.out.println("\nThere are no containers to remove");
            } else {
                System.out.println("\nAll containers are running so you can't remove them.");
            }
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to remove: ");
            int containerNumber = scanner.nextInt() - 1;
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);
            try {
                // Remove the specified container
                System.out.println("Removing the container " + container.getNames()[0].substring(1) + "...");
                Main.dockerClient.removeContainerCmd(container.getId()).exec();
                System.out.println("Container removed successfully.");
            } catch (ConflictException e) {
                // If the container is already running
                System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is currently running.. Try stoping it first" + "\033[0m");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }

    }*/

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

    /*

    public void pauseContainer() {
        System.out.println("\nAvailable containers to pause : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Up") && !curIns.getStatus().contains("Paused")) {
                // Available containers to pause
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Already exited containers
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Exited or Paused)");
            }
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If there are no containers to pause
            if (Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() == 0) {
                System.out.println("\nThere are no containers to pause");
            } else {
                System.out.println("\nAll containers are paused or exited.");
            }
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to pause: ");
            int containerNumber = scanner.nextInt() - 1;
            // Check if the number is valid
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);
            try {
                // Pause the specified container
                System.out.println("Pausing the container " + container.getNames()[0].substring(1) + "...");
                Main.dockerClient.pauseContainerCmd(container.getId()).exec();
                System.out.println("Container paused successfully.");
            } catch (ConflictException e) {
                System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already paused or Exited." + "\033[0m");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }
    }*/
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
/*
    public void unpauseContainer() {
        System.out.println("\nAvailable containers to unpause : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().contains("Paused")) {
                // Available containers to unpause
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Containers that cannot be unpaused
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Not Paused)");
            }
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If there are no containers to unpause
            if (Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() == 0) {
                System.out.println("\nThere are no containers to unpause");
            } else {
                System.out.println("\nAll containers are in a not paused state");
            }
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to unpause: ");
            int containerNumber = scanner.nextInt() - 1;
            // Check if the number is valid
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);
            try {
                // Unpause the specified container
                System.out.println("Unpausing the container " + container.getNames()[0].substring(1) + "...");
                Main.dockerClient.unpauseContainerCmd(container.getId()).exec();
                System.out.println("Container unpaused successfully.");
            } catch (InternalServerErrorException e) {
                System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is not paused." + "\033[0m");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }
    }*/

    // Method to rename a container
   /* public void renameContainer() {

        int c = 0;

        // Show the available containers to rename
        System.out.println("\nAvailable containers to rename : ");
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
            c++;
        }

        if (c == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If there are no containers to rename
            System.out.println("\nThere are no containers to rename");
            return;
        }
        try {
            // Get the container number from the user
            System.out.print("\nEnter the number of the container to rename: ");
            int containerNumber = scanner.nextInt() - 1;
            scanner.nextLine();
            // Check if the number is valid
            if (containerNumber < 0 || containerNumber >= Main.dockerClient.listContainersCmd().withShowAll(true).exec().size()) {
                System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
                return;
            }

            // Retrieve the Container object corresponding to the containerName
            Container container = getContainerByNumber(containerNumber);

            System.out.print("Enter the new name for the container: ");
            String newName = scanner.nextLine();
            try {
                // Rename the specified container
                Main.dockerClient.renameContainerCmd(container.getId())
                        .withName(newName)
                        .exec();
                System.out.println("Container renamed successfully.");
            } catch (ConflictException e) {
                // If the container is already running
                System.out.println("\033[0;31m" + "You can't name this container this way because there is another container by this name" + "\033[0m");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid integer.");
            scanner.next();
        }
    }
*/
    // Method to run a container
    /*
    public void runContainer(String imageName, int sourcePort, int targetPort) {
        try {
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

            // Calling liveMeasureForNewContainer for this container to start a callback for CPU, Memory, etc.
            MonitorThread.liveMeasureForNewContainer(container.getId());

            // Print the container ID
            System.out.println("Container started and running successfully. Container ID: " + container.getId() + " on port: " + sourcePort + ":" + targetPort);
        } catch (InterruptedException | NoPortException | NotFoundException | InternalServerErrorException e1) {
            handleRunContainerException(e1);
        } catch (Exception e2) {
            handleRunContainerException(e2);
        }
    }

    private void handleRunContainerException(Exception e) {
        System.err.println("Error running the container: " + e.getMessage());
        e.printStackTrace();
    }


    */
    public void runContainer() {
        String imageName = null;
        try {

            // Get the image name from the user
            System.out.print("Enter the name and the version of the image (ex format: nginx:latest )." +
                    "\nDon't worry if you have not pulled it, I will do it for you :) : ");
            scanner.nextLine();
            imageName = scanner.nextLine();

            // Get the source port from the user
            Integer sourcePort = null;
            while (sourcePort == null || sourcePort < 1 || sourcePort > 65535) {
                System.out.print("Enter the source port number (1-65535) or 0 if you want no specific ports: ");
                try {
                    sourcePort = scanner.nextInt();
                    if (sourcePort == 0) {
                        throw new NoPortException();
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid integer.");
                    scanner.next();
                }
            }

            // Get the target port from the user
            Integer targetPort = null;
            while (targetPort == null || targetPort < 0 || targetPort > 65535) {
                System.out.print("Enter the target port number (1-65535): ");
                try {
                    targetPort = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid integer.");
                    scanner.next();
                }
            }

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
        } catch (NoPortException e) {
            try {
                CreateContainerResponse container = Main.dockerClient.createContainerCmd(imageName).exec();

                // Pull the specified Docker image
                Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

                // Create and start a container based on the pulled image
                Main.dockerClient.startContainerCmd(container.getId()).exec();

                // Print the container ID
                System.out.println("Container started and running successfully. Container ID: " + container.getId());
            } catch (InterruptedException a) {
                System.out.println("Image pull operation was interrupted.");
                e.printStackTrace();
            } catch (NotFoundException a) {
                System.out.println("The image you are trying to pull does not exist");
            } catch (Exception a) {
                System.err.println("Error pulling the image: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (InterruptedException a) {
            System.out.println("Image pull operation was interrupted.");
            a.printStackTrace();
        } catch (NotFoundException a) {
            System.err.println("The image you are trying to pull does not exist");
        } catch (InternalServerErrorException a) {
            System.err.println("Port already in use try using another source port");
        } catch (Exception a) {
            System.err.println("Error pulling the image: " + a.getMessage());
            a.printStackTrace();
        }

    }

    // Method to pull an image
    public void pullImage() {

        // Get the image name from the user
        System.out.print("Enter the name and the version of the image to pull (ex format: nginx:latest ): ");
        scanner.nextLine();
        String imageName = scanner.nextLine();

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

    // Method to get a Container object based on the container list number
    private Container getContainerByNumber(int containerNumber) {
        List < Container > containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        return containers.get(containerNumber);
    }
    private Container getContainerById(String containerId) {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Iterate through the list of containers and find the one with the matching ID
        for (Container container : containers) {
            if (container.getId().equals(containerId)) {
                return container;
            }
        }
        return null;
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
                runContainer();
                break;

            case 7:
                // Pull Image
                pullImage();
                break;

            default:
                // Handle invalid choice
                System.out.println("Invalid choice. Please try again.");
        }
    }
    // Method to receive a command from the GUI
    public void receiveCommand(int command) {
        try {
            // Put the received command into the queue
            commandQueue.put(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

