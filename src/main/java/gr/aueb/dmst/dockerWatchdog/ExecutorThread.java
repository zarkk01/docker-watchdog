package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.List;
import java.util.Scanner;

// Definition of a Runnable class for running the ExecutorThread
public class ExecutorThread implements Runnable {

    // Scanner for user input
    Scanner scanner = new Scanner(System.in);

    // Implementation of the run method from the Runnable interface
    @Override
    public void run() {
        // Continuous loop to show the menu and perform actions based on user input
        while (true) {
            // Method to display the menu and interact with the user's choice
            showMenuWithInteractions();
            int choice = scanner.nextInt();
            doDependsOnChoice(choice);
        }
    }

    // start a Docker container based on user input
    public void startContainer() {
        // Display available containers to start
        System.out.println("\nAvailable containers to start : ");
        // Count variable for tracking available containers
        int c = 0;
        // Loop through each container in the list
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            // Check if the container is in "Exited" state
            if (curIns.getStatus().startsWith("Exited")) {
                // Display container details with an index
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Display container details with an indication that it is already running
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already running)");
            }
        }
        // If no containers are available to start, display a message and return
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

        // User input to select a container to start
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the number of the container to start: ");
        // Get user input for the container to start
        int containerNumber = scanner.nextInt() - 1;
        // Check if the entered container number is valid
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
            return;
        }
        // Get selected container based on the user's input
        Container container = getContainerByNumber(containerNumber);
        try {
            //Start the container
            System.out.println("Starting the container " + container.getNames()[0].substring(1) + "...");
            Main.dockerClient.startContainerCmd(container.getId()).exec();
            System.out.println("Container started successfully.");
        } catch (NotModifiedException e) {
            //handle the case when the container is already running
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already running please try again with another container" + "\033[0m");
        }

    }

    // Method to display available containers to stop, prompt the user to stop one, and initiate the container stop process
    public void stopContainer() {
        // Displaying available containers to stop
        System.out.println("\nAvailable containers to stop : ");
        // Count variable for tracking available containers
        int c = 0;
        // Looping through each container in the list
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            // Checking if the container is in "Up" (running) state
            if (curIns.getStatus().startsWith("Up")) {
                // Displaying container details with an index
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Displaying container details with an indication that it is already exited
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Already exited)");
            }
        }
        // If no containers are available to stop, display a message and return
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
        // User input to select a container to stop
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to stop: ");
        // Getting user input for the container to stop
        int containerNumber = scanner.nextInt() - 1;
        // Checking if the entered container number is valid
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number."+ "\033[0m");
            return;
        }
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        try {
            // Stop the specified container
            System.out.println("Stopping the container " + container.getNames()[0].substring(1) + "...");
            Main.dockerClient.stopContainerCmd(container.getId()).exec();
            System.out.println("Container stopped successfully.");
        } catch (NotModifiedException e){
            // Handling the case when the container is already stopped
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is already stopped." + "\033[0m");
        }
    }
    //method to remove a docker container based on user input
    public void removeContainer() {
        // Display available containers to remove
        System.out.println("\nAvailable containers to remove : ");
        // Count variable for tracking available containers
        int c = 0;
        // Loop through each container in the list
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);
            // Check if the container is in "Exited" state
            if (curIns.getStatus().startsWith("Exited")) {
                // Display container details with an index
                System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
                c++;
            } else {
                // Display container details with an indication that it is currently running
                System.out.println("\033[9m" + i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "..." + "\033[0m" + " (Running)");
            }
        }

        // If no containers are available to remove, display a message and return
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

        // User input to select a container to remove
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to remove: ");

        // Getting user input for the container to remove
        int containerNumber = scanner.nextInt() - 1;

        // Checking if the entered container number is valid
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number."+ "\033[0m");
            return;
        }
        // Getting the selected container based on the user's input
        Container container = getContainerByNumber(containerNumber);
        try {
            // Remove the specified container
            System.out.println("Removing the container " + container.getNames()[0].substring(1) + "...");
            Main.dockerClient.removeContainerCmd(container.getId()).exec();
            System.out.println("Container removed successfully.");
        } catch (ConflictException e) {
            // Handle the case when the container is currently running, and removal is not allowed
            System.out.println("\033[0;31m" + container.getNames()[0].substring(1) + " is currently running.. Try stoping it first" + "\033[0m");
        }

    }
    //method to rename a container based on user input
    public void renameContainer() {
        // Count variable for tracking available containers
        int c = 0;
        // Display available containers to rename
        System.out.println("\nAvailable containers to rename : ");
        // Loop through each container in the list
        for (int i = 1; i < MonitorThread.containers.size() + 1; i++) {
            Container curIns = MonitorThread.containers.get(i - 1);

            // Display container details with an index
            System.out.println(i + "." + " NAME = " + curIns.getNames()[0].substring(1) + " , ID = " + curIns.getId().substring(0, 8) + "...");
            c++;
        }
        // If no containers are available to rename, display a message and return
        if(c == 0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\nThere are no containers to rename");
            return;
        }

        // User input to select a container to rename
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the container to rename: ");
        // Getting user input for the container to rename

        int containerNumber = scanner.nextInt() - 1;

        // Consuming the newline character after the integer input
        scanner.nextLine();

        // Checking if the entered container number is valid
        if (containerNumber < 0 || containerNumber >= MonitorThread.containers.size()) {
            System.out.println("\033[0;31m" + "Invalid container number. Please enter a valid number." + "\033[0m");
            return;
        }
        // Retrieve the Container object corresponding to the containerName
        Container container = getContainerByNumber(containerNumber);
        // User input for the new name of the container
        System.out.print("Enter the new name for the container: ");
        String newName = scanner.nextLine();
        try {
            // Rename the specified container
            Main.dockerClient.renameContainerCmd(container.getId())
                    .withName(newName)
                    .exec();
            System.out.println("Container renamed successfully.");
        } catch (ConflictException e) {
            //handle the case when there is a naming conflict with another container
            System.out.println("\033[0;31m" + "You can't name this container this way because there is another container by this name" + "\033[0m");
        }
    }
    //run container method
    public void runContainer() {
        try {
            // Setting the log level to INFO
            ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                    (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
            // Creating a scanner object for user input
            Scanner scanner = new Scanner(System.in);
            // Prompting the user to enter the name and version of the image
            System.out.print("Enter the name and the version of the image (ex format: nginx:latest )." +
                    "\nDon't worry if you have not pulled it, I will do it for you :) : "); //print message that asks for input
            // Getting user input for the image name and version (container name and tag)
            String imageName = scanner.nextLine(); //give container name and tag input

            // Pulling the specified Docker image
            Main.dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion();

            // Create and start a container based on the pulled image
            CreateContainerResponse containerResponse = Main.dockerClient.createContainerCmd(imageName).exec();
            //start the created docker container using its ID
            Main.dockerClient.startContainerCmd(containerResponse.getId()).exec();
            //print message of successfull start and run of container , also print its id
            System.out.println("Container started and running successfully. Container ID: " + containerResponse.getId());
        } catch (InterruptedException e) { //in case of an interrupted exception
            // Handling interruption exceptions during container creation or start
            System.out.println("Container creation or start operation was interrupted.");
            e.printStackTrace();
        } catch (Exception e) { //in case of a general exception
            // Handling general exceptions, such as errors in pulling or running the Docker image
            System.out.println("Error pulling or running the image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //method that pulls image
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

    // Method to retrieve a Docker container based on its position in the list of containers
    private Container getContainerByNumber(int containerNumber) {
        // Retrieve a list of all Docker containers, including stopped ones
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        // Return the container at the specified position in the list
        return containers.get(containerNumber);
    }
    // Method to display Docker information including summary, container details, and image details
    public void showDockerInfo() {
        // Display a summary of Docker information
        showDockerSummary();
        // Setting the logging level of the ROOT_LOGGER to INFO
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
        // Display information about Docker containers
        System.out.println("\n----" + "\u001B[33m" + "Containers" + "\u001B[0m" + "----");
        // Print each container instance with yellow text
        for (MyInstance instance : Main.myInstancesList) {
            System.out.println("\u001B[33m" + instance + "\u001B[0m");
        }
        // Display information about Docker images
        System.out.println("\n----" + "\u001B[32m" + "Images" + "\u001B[0m" + "----");
        for (MyImage myImage : Main.myImagesList) {
            // Print each image instance with green text
            System.out.println("\u001B[32m" + myImage + "\u001B[0m");
        }
    }

    /* Method to display a summary of Docker information, including the total number of containers,
    running containers, total images, and images in use */

    public void showDockerSummary(){
        //calculate the total number of containers
        int totalContainers = Main.myInstancesList.size();
        //initialize the count of running containers
        int runningContainers =0 ;
        //calculate the total number of images
        int images = Main.myImagesList.size();
        //initialize the count of images in use
        int imagesInUse = 0;
        // loop through each container instance to count running containers
        for (MyInstance instance : Main.myInstancesList){
            if (instance.getStatus().startsWith("Up")){
                runningContainers++;
            }
        }
            //loop through each image instance to count images in use
            for (MyImage image : Main.myImagesList){
                if (image.getStatus().startsWith("In")){
                    imagesInUse++;
                }
        }
            //print a summary of docker information in magenta text
        System.out.println("----"+"\u001B[35m" + "Docker Summary" + "\u001B[0m" + "----");
        System.out.println("\u001B[35m"+"Total Containers: " + totalContainers);
        System.out.println("Running Containers: " + runningContainers);
        System.out.println("Total Images: " + images);
        System.out.println("Images In Use: " + imagesInUse+"\u001B[0m");
    }


    //print the menu that shows possible options the user has over the app
    private void showMenuWithInteractions(){
        System.out.println("\nChoose an option:");
        System.out.println("1. View Docker Info");
        System.out.println("2. Start a container");
        System.out.println("3. Stop a container");
        System.out.println("4. Run a container");
        System.out.println("5. Remove a container");
        System.out.println("6. Rename a container");
        System.out.println("7. Pull an image");
        System.out.println("8.Exit");
    }
    // Method to perform actions based on the user's choice
    private void doDependsOnChoice(int choice){
        // Using a switch statement to determine the action based on the user's choice
        switch (choice) {
            case 1:
                // Show Docker information
                showDockerInfo();
                break;
            case 2:
                // Start a Docker container
                startContainer();
                break;
            case 3:
                // Stop a Docker container
                stopContainer();
                break;
            case 4:
                // Run a Docker container
                runContainer();
                break;
            case 5:
                // Warn the user and remove a Docker container
                System.out.println("Please be careful, the container will be permanently removed!!");
                removeContainer();
                break;
            case 6:
                // Rename a Docker container
                renameContainer();
                break;
            case 7:
                // Pull a Docker image
                pullImage();
                break;
            case 8:
                // Close the scanner and exit the program
                scanner.close();
                System.exit(0);
            default:
                // Handle invalid choices
                System.out.println("Invalid choice. Please try again.");
        }
    }
}