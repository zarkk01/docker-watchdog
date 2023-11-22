package gr.aueb.dmst.dockerWatchdog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MonitorThread implements Runnable {

    //Initiate running variable true
    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {

            // Calling monitorCluster so to show the current cluster situation
            monitor();

            // Sleep for a specified interval (e.g., 20 sec) and then repeat if running
            try {
                Thread.sleep(6000); // 6 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void monitor() {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        List < Container > containers;
        containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        String[][] instancesArray = new String[100][2];
        int counterInstance = -1;
        String[][] containersArray = new String[100][2];
        int counterContainer = -1;

        for (Container container: containers) {
            counterContainer++;
            containersArray[counterContainer][0] = container.getId();

            for ( Instance instance: Main.instancesList) {
                if (container.getId().equals(instance.getId())) {
                    containersArray[counterContainer][1] = "match";
                    break;
                }
            }
        }

        for (Instance instance : Main.instancesList) {
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
                for (Instance instance : Main.instancesList) {
                    if (instance.getId().equals(containersArray[i][0])){
                        instance.setStatus(Main.dockerClient.inspectContainerCmd
                                (containersArray[i][0]).exec().getState().getStatus());
                        break;
                    }
                }
            } else {
                Instance addOne = new Instance(containersArray[i][0],
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getName() ,
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getImageId() ,
                        Main.dockerClient.inspectContainerCmd(containersArray[i][0]).exec().getState().getStatus());

                Main.instancesList.add(addOne);
            }
        }

        for (int i = 0 ; i < counterInstance +1 ; i++) {
            Iterator<Instance> iterator = Main.instancesList.iterator();
            while (iterator.hasNext()) {
                Instance instance = iterator.next();

                if (instance.getId().equals(instancesArray[i][0]) && instancesArray[i][1] == null) {
                    iterator.remove();
                }
            }
        }

        int runningContainers = 0;
        for (Instance instance :Main.instancesList) {
            if (instance.getStatus().equals("running")){
                runningContainers++;
            }
        }


        System.out.println("We have " + Main.instancesList.size() + " containers. " +
                runningContainers + " of them are running, while ");

        //+ stoppedContainers + " stopped." +
        //" The total images count is " + Main.imagesList.size() + " and from them " + Img.getUsedImages() + " used.");




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