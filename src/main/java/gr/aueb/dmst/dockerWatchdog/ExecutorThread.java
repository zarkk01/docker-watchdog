package gr.aueb.dmst.dockerWatchdog;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.io.Closeable;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import static gr.aueb.dmst.dockerWatchdog.Main.dockerClient;

public class ExecutorThread implements Runnable {

    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        while (true) {
            showMenuWithInteractions();
            int choice = scanner.nextInt();
            doDependsOnChoice(choice);
        }
    }

    public void startContainer() {

        System.out.println("\nAvailable containers to start : ");
        int c = 0;
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            if (curIns.getStatus().startsWith("Exited")) {
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already running)");
            }
        }

        if(c == 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (MonitorThread.containers.size() == 0) {
                System.out.println("\nThere are no containers to run");
            } else{
                System.out.println("\nAll containers are running.");
            }
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the number of the container to start: ");
        int containerNumber = scanner.nextInt() - 1;
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
            return;
        }

        Container container = getContainerByNumber(containerNumber);
        try {
            //Start the container
            System.out.println("Starting the container " + container.getNames()[0].substring(1) + "...");
            dockerClient.startContainerCmd(container.getId()).exec();
            System.out.println("Container started successfully.");
        } catch (NotModifiedException e) {
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already running please try again with another container" + "\033[0m");
        }

    }

    public void stopContainer() {
        System.out.println("\nAvailable containers to stop : ");
        int c = 0;
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            if (curIns.getStatus().startsWith("Up")) {
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already exited)");
            }
        }

        if(c == 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (MonitorThread.containers.size() == 0) {
                System.out.println("\nThere are no containers to stop");
            } else{
                System.out.println("\nAll containers are stopped.");
            }
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to stop: ");
        int containerNumber = scanner.nextInt() - 1;
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number."+ "\033[0m");
            return;
        }
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        try {
            // Stop the specified container
            System.out.println("Stopping the container " + container.getNames()[0].substring(1) + "...");
            dockerClient.stopContainerCmd(container.getId()).exec();
            System.out.println("Container stopped successfully.");
        } catch (NotModifiedException e){
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already stopped." + "\033[0m");
        }
    }

    public void removeContainer() {

        System.out.println("\nAvailable containers to remove : ");
        int c = 0;
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            if (curIns.getStatus().startsWith("Exited")) {
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Running)");
            }
        }

        if(c == 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (MonitorThread.containers.size() == 0) {
                System.out.println("\nThere are no containers to remove");
            } else{
                System.out.println("\nAll containers are running so you can't remove them.");
            }
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the number of the container to remove: ");
        int containerNumber = scanner.nextInt() - 1;
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number."+ "\033[0m");
            return;
        }
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        try {
            // Remove the specified container
            System.out.println("Removing the container " + container.getNames()[0].substring(1) + "...");
            dockerClient.removeContainerCmd(container.getId()).exec();
            System.out.println("Container removed successfully.");
        } catch (ConflictException e) {
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is currently running.. Try stoping it first" + "\033[0m");
        }

    }

    public void renameContainer() {

        int c = 0;
        System.out.println("\nAvailable containers to rename : ");
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
        }

        if(c == 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\nThere are no containers to rename");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to rename: ");
        int containerNumber = scanner.nextInt() - 1;
        scanner.nextLine();
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
            return;
        }
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);

        System.out.print("Enter the new name for the container: ");
        String newName = scanner.nextLine();
        try {
            // Rename the specified container
            dockerClient.renameContainerCmd(container.getId())
                    .withName(newName)
                    .exec();
            System.out.println("Container renamed successfully.");
        } catch (ConflictException e) {
            System.out.println("\033[0;31m" + "You can't name this container this way because there is another container by this name" + "\033[0m");
        }
    }

    public void runContainer() {
        try {
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                    (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the name and the version of the image (ex format: nginx:latest )." +
                    "\nDon't worry if you have not pulled it, I will do it for you :) : ");
            String imageName = scanner.nextLine();

            Integer sourcePort = null;
            while (sourcePort == null || sourcePort < 1 || sourcePort > 65535) {
                System.out.print("Enter the source port number (1-65535): ");
                try {
                    sourcePort = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid integer.");
                    scanner.next(); // consume invalid input
                }
            }

    // Get the target port from the user
            Integer targetPort = null;
            while (targetPort == null || targetPort < 1 || targetPort > 65535) {
                System.out.print("Enter the target port number (1-65535): ");
                try {
                    targetPort = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid integer.");
                    scanner.next(); // consume invalid input
                }
            }

            dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();
            ExposedPort tcp22 = ExposedPort.tcp(sourcePort);

            Ports portBindings = new Ports();
            portBindings.bind(tcp22, Ports.Binding.bindPort(targetPort));

            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withCmd("sleep", "infinity")
                    .withExposedPorts(tcp22)
                    .withPortBindings(portBindings)
                    .exec();

            dockerClient.startContainerCmd(container.getId().toString()).exec();

            System.out.println("Container started and running successfully. Container ID: " + container.getId());
        } catch (InterruptedException e) {
            System.out.println("Container creation or start operation was interrupted.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error pulling or running the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void pullImage() {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name and the version of the image to pull (ex format: nginx:latest ): ");
        String imageName = scanner.nextLine();

        // Pull the specified Docker image
        try {
            dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();
            System.out.println("Image pulled successfully.");
        } catch (InterruptedException e) {
            System.out.println("Image pull operation was interrupted.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error pulling the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Container getContainerByNumber(int containerNumber) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        return containers.get(containerNumber);
    }

    public void showDockerInfo() {
            showDockerSummary();
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                    (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

            System.out.println("\n----" + "\u001B[33m" + "Containers" + "\u001B[0m" + "----");
            for (MyInstance instance : Main.myInstancesList) {
                System.out.println("\u001B[33m" + instance + "\u001B[0m");
            }

            System.out.println("\n----" + "\u001B[32m" + "Images" + "\u001B[0m" + "----");
            for (MyImage myImage : Main.myImagesList) {
                System.out.println("\u001B[32m"+ myImage+ "\u001B[0m");
            }
    }

    public void showDockerSummary(){
        int totalContainers = Main.myInstancesList.size();
        int runningContainers =0 ;
        int images = Main.myImagesList.size();
        int imagesInUse = 0;

        for (MyInstance instance : Main.myInstancesList){
            if (instance.getStatus().startsWith("Up")){
                runningContainers++;
            }
        }

        for (MyImage image : Main.myImagesList){
            if (image.getStatus().equals("In use")){
                imagesInUse++;
            }
        }

        System.out.println("----"+"\u001B[35m" + "Docker Summary" + "\u001B[0m" + "----");
        System.out.println("\u001B[35m"+"Total Containers: " + totalContainers);
        System.out.println("Running Containers: " + runningContainers);
        System.out.println("Total Images: " + images);
        System.out.println("Images In Use: " + imagesInUse+"\u001B[0m");
    }



    private void showMenuWithInteractions(){
        System.out.println("\nChoose an option:");
        System.out.println("1. View Docker Info");
        System.out.println("2. Start a container");
        System.out.println("3. Stop a container");
        System.out.println("4. Run a container");
        System.out.println("5. Remove a container");
        System.out.println("6. Rename a container");
        System.out.println("7. Pull an image");
        System.out.println("8. Exit");
    }

    private void doDependsOnChoice(int choice){
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
                MonitorThread.stopMonitoring();
                System.exit(0);
                scanner.close();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
