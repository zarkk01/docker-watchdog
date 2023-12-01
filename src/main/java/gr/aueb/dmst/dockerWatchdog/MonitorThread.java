package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


public class MonitorThread implements Runnable {

    //Initiate running variable true
    private static volatile boolean running = true;
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
                Thread.sleep(50); // 1 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    static List< Container > containers;
    static List < Image > images;
    private void monitoring() throws InternalServerErrorException {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();

        for (Container container : containers) {
            boolean match = false;

            for (MyInstance instance : Main.myInstancesList) {
                if (container.getId().equals(instance.getId())) {
                    instance.setStatus(container.getStatus());
                    instance.setName(container.getNames()[0]);
                    instance.setLabels(container.labels);
                    Long sizeRootFs = container.getSizeRootFs();
                    if (sizeRootFs != null) {
                        instance.setSize(sizeRootFs);
                    } else {
                        instance.setSize(0);
                    }
                    match = true;
                    break;
                }
            }
            if (!match) {
                Long sizeRootFs = container.getSizeRootFs();
                MyInstance addOne = new MyInstance(container.getId(),container.getNames()[0],
                        container.getImage(),container.getStatus() ,container.labels ,sizeRootFs != null ? sizeRootFs : 0);

                Main.myInstancesList.add(addOne);
            }
        }


        for (MyInstance instance : Main.myInstancesList) {
            boolean match = false;

            for (Container container : containers) {
                if (instance.getId().equals(container.getId())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                Main.myInstancesList.remove(instance);
                break;
            }
        }

        for (Image image : images) {
            boolean match = false;

            for (MyImage myImage : Main.myImagesList) {
                if (image.getId().equals(myImage.getId())) {
                    myImage.setStatus(getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                            .inspectImageCmd(image.getId()).exec().getRepoTags()).get(0)));
                    match = true;
                    break;
                }
            }

            if (!match) {
                MyImage addOne = new MyImage(Objects.requireNonNull(Main.dockerClient
                        .inspectImageCmd(image.getId()).exec().getRepoTags()).get(0), image.getId(),
                        Main.dockerClient.inspectImageCmd(image.getId()).exec().getSize(),
                        getImageUsageStatus(Objects.requireNonNull(Main.dockerClient
                                .inspectImageCmd(image.getId()).exec().getRepoTags()).get(0)));

                Main.myImagesList.add(addOne);
            }
        }

        Iterator<MyImage> iterator = Main.myImagesList.iterator();
        while (iterator.hasNext()) {
            MyImage myImage = iterator.next();

            boolean match = false;
            for (Image image : images) {
                if (myImage.getId().equals(image.getId())) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                iterator.remove();
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

    public static void stopMonitoring() {

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