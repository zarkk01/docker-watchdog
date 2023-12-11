package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ExecutorThread implements Runnable {

    // Initiate and create scanner
    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {

        // Continuous loop showing menu and calling the appropriate method
        while (true) {
            showMenuWithInteractions();
            int choice = scanner.nextInt();
            doDependsOnChoice(choice);
        }

    }

    // Method to start a container
    public void startContainer() {

        System.out.println("\nAvailable containers to start : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Exited")) {
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
    }

    // Method to stop a container
    public void stopContainer() {
        System.out.println("\nAvailable containers to stop : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Up")) {
                // Available containers to stop
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Already exited containers
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
    }

    // Method to remove a container
    public void removeContainer() {

        // Show the available containers to remove
        System.out.println("\nAvailable containers to remove : ");
        int c = 0;
        for (int i = 1; i < Main.dockerClient.listContainersCmd().withShowAll(true).exec().size() + 1; i++) {
            Container curIns = Main.dockerClient.listContainersCmd().withShowAll(true).exec().get(i - 1);
            if (curIns.getStatus().startsWith("Exited")) {
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

    }

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
                System.out.println("\nAll containers are paused.");
            }
            return;
        }

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
        } catch (NotModifiedException e) {
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already paused." + "\033[0m");
        } catch (ConflictException e) {
        System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already paused or Exited." + "\033[0m");
    }
    }
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
        } catch (NotModifiedException e) {
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is not paused." + "\033[0m");
            return;
        } catch (InternalServerErrorException e) {
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is not paused." + "\033[0m");
            return;
        }
    }

    // Method to rename a container
    public void renameContainer() {

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
    }

    // Method to run a container
    public void runContainer() {
        String imageName = null;
        try {

            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

            // Get the image name from the user
            System.out.print("Enter the name and the version of the image (ex format: nginx:latest )." +
                    "\nDon't worry if you have not pulled it, I will do it for you :) : ");
            scanner.nextLine();
            imageName = scanner.nextLine();

            // Get the source port from the user
            Integer sourcePort = null;
            while (sourcePort == null || sourcePort < 1 || sourcePort > 65535) {
                System.out.print("Enter the source port number (1-65535): ");
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
            DockerLiveMetrics.liveMeasureForNewContainer(container.getId());

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
                System.out.println("Container creation or start operation was interrupted.");
                a.printStackTrace();
            } catch (Exception a) {
                System.out.println("Error pulling or running the image: " + a.getMessage());
                a.printStackTrace();
            }
        } catch (InterruptedException e) {
            System.out.println("Container creation or start operation was interrupted.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error pulling or running the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to pull an image
    public void pullImage() {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

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
        } catch (Exception e) {
            System.out.println("Error pulling the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get a Container object based on the container list number
    private Container getContainerByNumber(int containerNumber) {
        List < Container > containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        return containers.get(containerNumber);
    }

    // Method to show the Docker info
    public void showDockerInfo() {
        showDockerSummary();
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        // Show the Docker Instances
        System.out.println("\n----" + "\u001B[33m" + "Containers" + "\u001B[0m" + "----");
        for (MyInstance instance: Main.myInstancesList) {
            System.out.println("\u001B[33m" + instance + "\u001B[0m");
        }

        // Show the Docker Images
        System.out.println("\n----" + "\u001B[32m" + "Images" + "\u001B[0m" + "----");
        for (MyImage myImage: Main.myImagesList) {
            System.out.println("\u001B[32m" + myImage + "\u001B[0m");
        }
    }

    // Method to show the Docker summary
    public void showDockerSummary() {
        int totalContainers = Main.myInstancesList.size();
        int runningContainers = 0;
        int images = Main.myImagesList.size();
        int imagesInUse = 0;

        for (MyInstance instance: Main.myInstancesList) {
            if (instance.getStatus().startsWith("running")) {
                runningContainers++;
            }
        }

        for (MyImage image: Main.myImagesList) {
            if (image.getStatus().startsWith("In")) {
                imagesInUse++;
            }
        }
        System.out.println("----" + "\u001B[35m" + "Docker Summary" + "\u001B[0m" + "----");
        System.out.println("\u001B[35m" + "Total Containers: " + totalContainers);
        System.out.println("Running Containers: " + runningContainers);
        System.out.println("Total Images: " + images);
        System.out.println("Images In Use: " + imagesInUse + "\u001B[0m");
    }

    // Method to show the menu with the available interactions
    private void showMenuWithInteractions() {
        System.out.println("\nChoose an option:");
        System.out.println("1. View Docker Info");
        System.out.println("2. Start a container");
        System.out.println("3. Stop a container");
        System.out.println("4. Run a container");
        System.out.println("5. Remove a container");
        System.out.println("6. Rename a container");
        System.out.println("7. Pull an image");
        System.out.println("8. Pause a container");
        System.out.println("9. Unpause a container");
        System.out.println("10. Exit");
    }

    // Method to do the appropriate action based on the user's choice
    private void doDependsOnChoice(int choice) {
        switch (choice) {
            case 1:
                showDockerInfo();
                break;
            case 2:
                startContainer();
                break;
            case 3:
                stopContainer();
                break;
            case 4:
                runContainer();
                break;
            case 5:
                System.out.println("Please be careful, the container will be permanently removed!!");
                removeContainer();
                break;
            case 6:
                renameContainer();
                break;
            case 7:
                pullImage();
                break;
            case 8:
                pauseContainer();
                break;
            case 9:
                unpauseContainer();
                break;
            case 10:
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}