package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitorThread implements Runnable {

    //Initiate running variable true
    private volatile boolean running = true;

    @Override
    public void run() {
        fillList();
        while (running) {

            // Calling monitorCluster so to show the current cluster situation
            startMonitoring();

            // Sleep for a specified interval (e.g., 20 sec) and then repeat if running
            try {
                Thread.sleep(1000); // 1 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillList() {

        ((Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        // Initiate containers list with existing containers.
        List < Container > containers;
        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container: containers) {
            Instance obj = new Instance(container.getId(), container.getNames()[0], container.getImage(), container.getStatus());
            Main.instancesList.add(obj);
        }

        List < Image > images;
        images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();

        for (Image image: images) {
            String isUsed = "false";
            for (Container container: containers) {
                if (container.getImageId().equals(image.getId())) {
                    isUsed = "true";
                }
            }
            Img obj = new Img(image.getId(), image.getRepoTags()[0], isUsed, image.getSize());
            Main.imagesList.add(obj);
        }

    }


    //Declaring
    int totalContainers = Main.instancesList.size();
    int totalImages = Main.imagesList.size();


    private void startMonitoring() {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        List < Container > containers;
        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        totalContainers = containers.size();


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