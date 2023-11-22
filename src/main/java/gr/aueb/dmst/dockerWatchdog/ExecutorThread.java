package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.List;
import java.util.Scanner;

public class ExecutorThread implements Runnable {

    Scanner scanner = new Scanner(System.in);
    @Override
    public void run(
    ) {
        while (true) {
            showMenuWithInteractions();
            int choice = scanner.nextInt();
            doDependsOnChoice(choice);
        }
    };

    private Container getContainerByName(String containerName) {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + containerName)) {
                    return container;
                }
            }
        }
        return null; // Container with the specified name not found
    }

    public void startContainer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the container to start: ");
        String containerName = scanner.nextLine();

        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByName(containerName);

        if (container != null && container.getStatus().startsWith("Exited")) {

            //Start the container
            System.out.println("Starting the container " + containerName + "...");
            Main.dockerClient.startContainerCmd(containerName).exec();
            System.out.println("Container started successfully.");
        } else {
            System.out.println("The container you are trying to start is either null or not in an exited state!");
        }
    }

    public void stopContainer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the container to stop: ");
        String containerName = scanner.nextLine();

        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByName(containerName);

        if (container != null && container.getStatus().startsWith("Up")) {
            // Stop the specified container
            System.out.println("Stopping the container " + containerName + "...");
            Main.dockerClient.stopContainerCmd(containerName).exec();
            System.out.println("Container stopped successfully.");
        } else {
            System.out.println("The container you are trying to stop is either null or not in a running state!");
        }
    }

    public void removeContainer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the container to remove: ");
        String containerName = scanner.nextLine();

        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByName(containerName);

        if (container != null && container.getStatus().startsWith("Exited")) {
            // Remove the specified container
            System.out.println("Removing the container " + containerName + "...");
            Main.dockerClient.removeContainerCmd(containerName).exec();
            System.out.println("Container removed successfully.");
        } else {
            System.out.println("The container you are trying to remove is either null or not in a stopped state!");
        }
    }

    public void renameContainer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the current name of the container: ");
        String currentName = scanner.nextLine();

        // Retrieve the Container object corresponding to the currentName
        Container container = getContainerByName(currentName);

        if (container != null) {
            System.out.print("Enter the new name for the container: ");
            String newName = scanner.nextLine();

            // Rename the specified container
            Main.dockerClient.renameContainerCmd(container.getId())
                    .withName(newName)
                    .exec();

            System.out.println("Container renamed successfully from " + currentName + " to " + newName + ".");
        } else {
            System.out.println("The container with the name " + currentName + " does not exist!");
        }
    }
    public void runContainer() {
        try {
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                    (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the name of the image: ");
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
        System.out.print("Enter the name of the image to pull: ");
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

    public void showDockerInfo() {

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Instance instance : Main.instancesList) {
            System.out.println(instance);
        }


    }

    private void showMenuWithInteractions(){
        System.out.println("Choose an option:");
        System.out.println("1. View Docker containers");
        System.out.println("2. Exit");
        System.out.println("3. Start a container");
        System.out.println("4. Stop a container");
        System.out.println("5. Run a container");
        System.out.println("6. Remove a container");
        System.out.println("7. Rename a container");
        System.out.println("8. Pull an image");
    }

    private void doDependsOnChoice(int choice){
        switch (choice) {
            case 1:
                showDockerInfo();
                break;
            case 2:
                ;
                System.exit(0);
                break;
            case 3:
                startContainer();
                break;
            case 4:
                stopContainer();
                break;
            case 5:
                runContainer();
                break;
            case 6:
                System.out.println("Please be careful, the container will be permanently removed!!");
                removeContainer();
                break;
            case 7:
                renameContainer();
                break;
            case 8:
                pullImage();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
