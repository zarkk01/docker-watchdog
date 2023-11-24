package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MonitorThread implements Runnable {

    //Initiate running variable true
    private volatile boolean running = true;
    @Override
    public void run() {
        while (running) {

            // Calling monitoring so to show the current cluster situation
            monitoring();

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
    private void monitoring() {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();

        String[][] instancesArray = new String[100][2];
        int counterInstance = -1;
        String[][] containersArray = new String[100][2];
        int counterContainer = -1;

        for (Container container: containers) {
            counterContainer++;
            containersArray[counterContainer][0] = container.getId();

            for ( MyInstance instance: Main.myInstancesList) {
                if (container.getId().equals(instance.getId())) {
                    containersArray[counterContainer][1] = "match";
                    break;
                }
            }
        }

        for (MyInstance instance : Main.myInstancesList) {
            counterInstance++;
            instancesArray[counterInstance][0] = instance.getId();

            for (Container container: containers) {
                if (instance.getId().equals(container.getId())){
                    instancesArray[counterInstance][1] = "match";
                    break;
                }
            }
        }

        for (int i = 0 ; i < counterContainer + 1 ; i++) {

            if ("match".equals(containersArray[i][1])){
                for (MyInstance instance : Main.myInstancesList) {
                    if (instance.getId().equals(containersArray[i][0])){
                        instance.setStatus(Main.dockerClient.inspectContainerCmd
                                (containersArray[i][0]).exec().getState().getStatus());
                        instance.setName(Main.dockerClient.inspectContainerCmd
                                (containersArray[i][0]).exec().getName());
                        break;
                    }
                }
            } else {
                MyInstance addOne = new MyInstance(containersArray[i][0],
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getName() ,
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getImageId() ,
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getState().getStatus());

                Main.myInstancesList.add(addOne);
            }

        }

        for (int i = 0 ; i < counterInstance +1 ; i++) {
            Iterator<MyInstance> iterator = Main.myInstancesList.iterator();
            while (iterator.hasNext()) {
                MyInstance instance = iterator.next();

                if (instance.getId().equals(instancesArray[i][0]) && instancesArray[i][1] == null) {
                    iterator.remove();
                }
            }
        }

        String[][] myImagesArray = new String[100][2];
        int counterMyImages = -1;
        String[][] imagesArray = new String[100][2];
        int counterImages = -1;

        for (Image image: images) {

            counterImages++;
            imagesArray[counterImages][0] = image.getId();

            for ( MyImage img: Main.myImagesList) {
                if (image.getId().equals(img.getId())) {
                    imagesArray[counterImages][1] = "match";
                    break;
                }
            }
        }

        for (MyImage img : Main.myImagesList) {
            counterMyImages++;
            myImagesArray[counterMyImages][0] = img.getId();

            for (Image image: images) {
                if (img.getId().equals(image.getId())){
                    myImagesArray[counterMyImages][1] = "match";
                    break;
                }
            }
        }

        for (int i = 0 ; i < counterImages + 1 ; i++) {

            if ("match".equals(imagesArray[i][1])){
                for (MyImage image : Main.myImagesList) {
                    if (image.getId().equals(imagesArray[i][0])){
                        image.setStatus(getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                                .inspectImageCmd(imagesArray[i][0]).exec().getRepoTags()).get(0)));
                        break;
                    }
                }
            } else {
                MyImage addOne = new MyImage(Objects.requireNonNull(Main.dockerClient
                        .inspectImageCmd(imagesArray[i][0]).exec().getRepoTags()).get(0),imagesArray[i][0], Main.dockerClient
                        .inspectImageCmd(imagesArray[i][0]).exec().getSize(),getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                        .inspectImageCmd(imagesArray[i][0]).exec().getRepoTags()).get(0)));

                Main.myImagesList.add(addOne);
            }
        }

        for (int i = 0 ; i < counterMyImages +1 ; i++) {
            Iterator<MyImage> iterator = Main.myImagesList.iterator();
            while (iterator.hasNext()) {
                MyImage myImage = iterator.next();

                if (myImage.getId().equals(myImagesArray[i][0]) && myImagesArray[i][1] == null) {
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