package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


public class MonitorThread implements Runnable {

    //Initiate running variable true
    private volatile boolean running = true;
    @Override
    public void run() {
        while (running) {
            // Set the root logger level to ERROR to suppress all messages
            ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.ERROR);
            // Set the level for the specific logger to ERROR
            ((Logger) LoggerFactory.getLogger("org.apache.http.impl.execchain.RetryExec")).setLevel(Level.ERROR);

            // Calling monitoring so to show the current cluster situation
            try {
                monitoring();
            } catch ( Exception e) {
                System.out.println("Error trying to connect to the Docker Daemon" +
                        ". Try restarting your docker desktop and running the program again..");
                System.exit(0);
            }

            // Sleep for a specified interval (e.g., 20 sec) and then repeat if running
            try {
                Thread.sleep(500); // 1 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    static List< Container > containers;
    static List < Image > images;
    private void monitoring() throws InternalServerErrorException, SocketException {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();

        List<String[]> containersList = new ArrayList<>();
        List<String[]> instancesList = new ArrayList<>();

        int counterContainer = -1;
        int counterInstance = -1;

        for (Container container : containers) {
            counterContainer++;
            String[] containerInfo = new String[2];
            containerInfo[0] = container.getId();

            for (MyInstance instance : Main.myInstancesList) {
                if (container.getId().equals(instance.getId())) {
                    containerInfo[1] = "match";
                    break;
                }
            }
            containersList.add(containerInfo);
        }

        for (MyInstance instance : Main.myInstancesList) {
            counterInstance++;
            String[] instanceInfo = new String[2];
            instanceInfo[0] = instance.getId();

            for (Container container : containers) {
                if (instance.getId().equals(container.getId())) {
                    instanceInfo[1] = "match";
                    break;
                }
            }
            instancesList.add(instanceInfo);
        }

        for (int i = 0; i < counterContainer + 1; i++) {
            if ("match".equals(containersList.get(i)[1])) {
                for (MyInstance instance : Main.myInstancesList) {
                    if (instance.getId().equals(containersList.get(i)[0])) {
                        instance.setStatus(Main.dockerClient.inspectContainerCmd(containersList.get(i)[0]).exec().getState().getStatus());
                        instance.setName(Main.dockerClient.inspectContainerCmd(containersList.get(i)[0]).exec().getName());
                        break;
                    }
                }
            } else {
                MyInstance addOne = new MyInstance(containersList.get(i)[0],
                        Main.dockerClient.inspectContainerCmd(containersList.get(i)[0]).exec().getName(),
                        Main.dockerClient.inspectContainerCmd(containersList.get(i)[0]).exec().getImageId(),
                        Main.dockerClient.inspectContainerCmd(containersList.get(i)[0]).exec().getState().getStatus());

                Main.myInstancesList.add(addOne);
            }
        }

        for (int i = 0; i < counterInstance + 1; i++) {
            Iterator<MyInstance> iterator = Main.myInstancesList.iterator();
            while (iterator.hasNext()) {
                MyInstance instance = iterator.next();

                if (instance.getId().equals(instancesList.get(i)[0]) && instancesList.get(i)[1] == null) {
                    iterator.remove();
                }
            }
        }

        List<String[]> imagesList = new ArrayList<>();
        List<String[]> myImagesList = new ArrayList<>();
        int counterImages = -1;
        int counterMyImages = -1;

        // Populate imagesList with matching information
        for (Image image : images) {
            counterImages++;
            String[] imageInfo = new String[2];
            imageInfo[0] = image.getId();

            for (MyImage img : Main.myImagesList) {
                if (image.getId().equals(img.getId())) {
                    imageInfo[1] = "match";
                    break;
                }
            }
            imagesList.add(imageInfo);
        }

        // Populate myImagesList with matching information
        for (MyImage img : Main.myImagesList) {
            counterMyImages++;
            String[] myImageInfo = new String[2];
            myImageInfo[0] = img.getId();

            for (Image image : images) {
                if (img.getId().equals(image.getId())) {
                    myImageInfo[1] = "match";
                    break;
                }
            }
            myImagesList.add(myImageInfo);
        }

        // Update Main.myImagesList based on imagesList
        for (int i = 0; i < counterImages + 1; i++) {
            if ("match".equals(imagesList.get(i)[1])) {
                for (MyImage image : Main.myImagesList) {
                    if (image.getId().equals(imagesList.get(i)[0])) {
                        image.setStatus(getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                                .inspectImageCmd(imagesList.get(i)[0]).exec().getRepoTags()).get(0)));
                        break;
                    }
                }
            } else {
                MyImage addOne = new MyImage(Objects.requireNonNull(Main.dockerClient
                        .inspectImageCmd(imagesList.get(i)[0]).exec().getRepoTags()).get(0), imagesList.get(i)[0],
                        Main.dockerClient.inspectImageCmd(imagesList.get(i)[0]).exec().getSize(),
                        getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                                .inspectImageCmd(imagesList.get(i)[0]).exec().getRepoTags()).get(0)));

                Main.myImagesList.add(addOne);
            }
        }

        // Remove unmatched entries from Main.myImagesList based on myImagesList
        for (int i = 0; i < counterMyImages + 1; i++) {
            Iterator<MyImage> iterator = Main.myImagesList.iterator();
            while (iterator.hasNext()) {
                MyImage myImage = iterator.next();
                if (myImage.getId().equals(myImagesList.get(i)[0]) && myImagesList.get(i)[1] == null) {
                    iterator.remove();
                }
            }
        }

    }

    private String getImageUsageStatus(String imageName) {
        // Get a list of all containers
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Check if any container is using the specified image
        for (Container container : containers) {
            InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();

            // Check if the container uses the specified image
            if (imageName.equals(containerInfo.getConfig().getImage())) {
                return "In use";
            }
        }

        // If no container is using the image, consider it as "Unused"
        return "Unused";
    }

    public void stopMonitoring() {

        // Thread is about to stop, so while loop in run method should stop
        running = false;
        // Closing dockerClient to prevent resource leaks
        try {
            Main.dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}