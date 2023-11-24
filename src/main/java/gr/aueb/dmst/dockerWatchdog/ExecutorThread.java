package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.bouncycastle.cert.jcajce.JcaAttributeCertificateIssuer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    ;

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
            System.out.println("All containers are running.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the number of the container to start: ");
        int containerNumber = scanner.nextInt() - 1;

        Container container = getContainerByNumber(containerNumber);

        //Start the container
        System.out.println("Starting the container " + container.getNames()[0].substring(1) + "...");
        Main.dockerClient.startContainerCmd(container.getId()).exec();
        System.out.println("Container started successfully.");
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
            System.out.println("All containers are exited.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to stop: ");
        int containerNumber = scanner.nextInt() - 1;

        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        // Stop the specified container
        System.out.println("Stopping the container " + container.getNames()[0].substring(1) + "...");
        Main.dockerClient.stopContainerCmd(container.getId()).exec();
        System.out.println("Container stopped successfully.");
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
            System.out.println("All containers are running.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the number of the container to remove: ");
        int containerNumber = scanner.nextInt() - 1;

        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        // Remove the specified container
        System.out.println("Removing the container " + container.getNames()[0].substring(1) + "...");
        Main.dockerClient.removeContainerCmd(container.getId()).exec();
        System.out.println("Container removed successfully.");

    }

    public void renameContainer() {

        int c =0;
        System.out.println("\nAvailable containers to rename : ");
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            if (curIns.getStatus().equals("Exited")) {
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
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
            System.out.println("All containers are running.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to rename: ");
        int containerNumber = scanner.nextInt() - 1;
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);

        System.out.print("Enter the new name for the container: ");
        String newName = scanner.nextLine();

        // Rename the specified container
        Main.dockerClient.renameContainerCmd(container.getId())
                .withName(newName)
                .exec();

        System.out.println("Container renamed successfully.");

    }

    public void runContainer() {
        try {
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                    (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the name and the version of the image (ex format: nginx:latest )." +
                    "Don't worry if you have not pulled it, I will do it for you :) : ");
            String imageName = scanner.nextLine();

            Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

            // Create and start a container based on the pulled image
            CreateContainerResponse containerResponse = Main.dockerClient.createContainerCmd(imageName).exec();
            Main.dockerClient.startContainerCmd(containerResponse.getId()).exec();

            System.out.println("Container started and running successfully. Container ID: " + containerResponse.getId());
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

    private Container getContainerByNumber(int containerNumber) {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        return containers.get(containerNumber);
    }

    public void showDockerInfo() {

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        System.out.println("\n----Containers----");
        for (MyInstance instance : Main.myInstancesList) {
            System.out.println(instance);
        }

        System.out.println("\n----Images----");
        for (MyImage myImage : Main.myImagesList) {
            System.out.println(myImage);
        }
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
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
