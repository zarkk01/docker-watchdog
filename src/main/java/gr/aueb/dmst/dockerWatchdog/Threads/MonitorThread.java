package gr.aueb.dmst.dockerWatchdog.Threads;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.EventsResultCallback;

import gr.aueb.dmst.dockerWatchdog.Exceptions.DatabaseOperationException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.EventHandlingException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ListFillingException;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gr.aueb.dmst.dockerWatchdog.Exceptions.LiveStatsException;

/**
 * This Thread is communicating with the Docker API and provide the state of the Docker Cluster to the app.
 * Starts by filling the lists with Containers, Images and Volumes and then uses a custom ResultCallback so
 * to maintain the state of the cluster in real time. After that, it opens a listener to the Docker API
 * and hears for events happening in the cluster. When an event occurs, it recognises it and updates the lists
 * accordingly.
 */
public class MonitorThread implements Runnable {

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(MonitorThread.class);

    /**
     * This method is calling in order the 3 basic methods of the class.
     * It fills the lists with the Docker components, then it starts the
     * live monitoring of the cluster, and finally it starts listening for
     * events.
     */
    @Override
    public void run() {
        try {
            fillLists();
            liveMeasure(null);
        } catch (ListFillingException | LiveStatsException | DatabaseOperationException e) {
            logger.error(e.getMessage());
        }
        startListening();
    }

    /** This method fills the lists with our custom My... models after getting info from original Docker Components.
     * In this way we can handle effectively in our way the Docker Cluster and maintain control.
     *
     * @throws ListFillingException with the appropriate message for the error.
     * @throws DatabaseOperationException if an error occurs when createAllTables() is called.
     */
    public void fillLists() throws ListFillingException, DatabaseOperationException {
        try {
            // Images list is filled with MyImage objects.
            List<Image> images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();
            for (Image image : images) {
                InspectImageResponse imageInfo = Main.dockerClient.inspectImageCmd(image.getId()).exec();
                // Creating our custom MyImage model.
                MyImage newImage = new MyImage(
                        imageInfo.getRepoTags().get(0),
                        imageInfo.getId(),
                        imageInfo.getSize(),
                        getImageUsageStatus(imageInfo.getRepoTags().get(0))
                );
                Main.myImages.add(newImage);
            }
        } catch (Exception e) {
            throw new ListFillingException("Images");
        }

        try {
            // Containers list is filled with MyInstance objects.
            List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
            for (Container container : containers) {
                InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();
                // Creating our custom MyInstance model.
                MyInstance newInstance = new MyInstance(
                        containerInfo.getId(),
                        containerInfo.getName(),
                        MyImage.getImageByID(containerInfo.getImageId()).getName(),
                        containerInfo.getState().getStatus(),
                        0, 0, 0, 0, 0,
                        new ArrayList<String>()
                        ,containerInfo.getNetworkSettings().getIpAddress(),
                        containerInfo.getNetworkSettings().getGateway(),
                        containerInfo.getNetworkSettings().getIpPrefixLen()
                );

                // Checking for volumes and adding them to the list.
                if(containerInfo.getMounts() != null){
                    for(InspectContainerResponse.Mount volumeName : containerInfo.getMounts()){
                        newInstance.addVolume(volumeName.getName());
                    }
                }
                Main.myInstances.add(newInstance);
            }
        } catch (Exception e) {
            throw new ListFillingException("Containers");
        }

        try {
            // Volumes list is filled with MyVolume objects.
            List<InspectVolumeResponse> volumes = Main.dockerClient.listVolumesCmd().exec().getVolumes();
            for (InspectVolumeResponse volume : volumes) {
                // Creating our custom MyVolume model.
                MyVolume newVolume = new MyVolume(
                        volume.getName(),
                        volume.getDriver(),
                        volume.getMountpoint(),
                        new ArrayList<String>()
                );
                for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
                    for(ContainerMount volumeName : container.getMounts()){
                        if(volumeName.getName() == null){continue;}
                        if(volumeName.getName().equals(volume.getName())){
                            newVolume.addContainerNameUsing(container.getNames()[0]);
                        }
                    }
                }
                Main.myVolumes.add(newVolume);
            }
        } catch (Exception e) {
            throw new ListFillingException("Volumes");
        }

        // In the end, call the method to create the tables in the database as we are sure that our lists are filled.
        DatabaseThread.createAllTables();
    }

    /**
     * This method is used to get the live statistics of the Docker Cluster.
     * It uses a custom ResultCallback to get the statistics of each container
     * and then updates the lists accordingly. liveMeasure() is responsible for
     * maintaining the state of every container's CPU, Memory, Block I/O and PIDs.
     *
     * @param containerId the id of the container we want to get the statistics.
     * @throws LiveStatsException if an error occurs when calling the exec() method.
     */
    public static void liveMeasure(String containerId) throws LiveStatsException {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Iterate through the containers and get the statistics of each one.
        for (Container container : containers) {
            // If the containerId is null, get the statistics of all containers.
            if (containerId == null || container.getId().equals(containerId)) {
                AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd = Main.dockerClient.statsCmd(container.getId());
                try {
                    // Use the custom ResultCallback to get the statistics.
                    asyncStatsCmd.exec(new CustomResultCallback(container.getId(), asyncStatsCmd));
                } catch (Exception e) {
                    throw new LiveStatsException();
                }
            }
        }
    }


    /**
     * This method is used to listen for events happening in the Docker Cluster.
     * It uses the Docker API to get the events, and then it recognises the type
     * of the event and calls the appropriate method to handle it.
     */
    public void startListening() {
        Main.dockerClient.eventsCmd().exec(new EventsResultCallback() {
            @Override
            public void onNext(Event event) {

                // Get the info of the event.
                EventType eventType = event.getType();
                String eventAction = event.getAction();
                String id = event.getActor().getId();

                switch (eventType) {
                    case CONTAINER:
                        try {
                            // If it is a container event, call the method to handle it.
                            handleContainerEvent(eventAction, id,event);

                            // Update the lists in the database.
                            DatabaseThread.keepTrackOfInstances();
                            DatabaseThread.keepTrackOfImages();
                        } catch (LiveStatsException | EventHandlingException | DatabaseOperationException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case IMAGE:
                        try {
                            // If it is an image event, call the method to handle it.
                            handleImageEvent(eventAction, id);
                        } catch (EventHandlingException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case VOLUME:
                        try {
                            // If it is a volume event, call the method to handle it.
                            handleVolumeEvent(eventAction, id);
                        } catch (EventHandlingException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                }
            }
        });
    }

    /**
     * This method is used to handle the container events.
     * It recognises the type of the event from eventAction and
     * calls the appropriate method to handle it.
     *
     * @param eventAction the action of the event.
     * @param containerId the id of the container the event happened.
     * @param event the event.
     *
     * @throws LiveStatsException if an error occurs when calling the liveMeasure() method.
     * @throws EventHandlingException if an error occurs when calling the handleContainerDestroyEvent() method.
     */
    public void handleContainerEvent(String eventAction, String containerId, Event event) throws LiveStatsException, EventHandlingException {
        // Given the ID, we get the custom MyInstance object.
        MyInstance instance = MyInstance.getInstanceByid(containerId);
        try {
            switch (eventAction) {
                case "start":
                case "unpause":
                    updateContainerStatus(instance, "running");
                    break;
                case "stop":
                case "die":
                    updateContainerStatus(instance, "exited");
                    break;
                case "pause":
                    updateContainerStatus(instance, "paused");
                    break;
                case "rename":
                    // To get the name of the container, we use event.getActor(), since we are provided only with containerId.
                    instance.setName(event.getActor().getAttributes().get("name"));
                    break;
                case "destroy":
                    handleContainerDestroyEvent(instance);
                    break;
                case "create":
                    handleContainerCreateEvent(containerId);
                    break;
            }
        } catch (Exception e) {
            throw new EventHandlingException("container");
        }
    }

    // Helper method to update the status of a container.
    private void updateContainerStatus(MyInstance instance, String status) {
        instance.setStatus(status);
    }

    // Helper method to handle the destroy event of a container.
    private void handleContainerDestroyEvent(MyInstance instance) throws DatabaseOperationException {
        Main.myInstances.remove(instance);
        for(MyInstance checkingInstance : Main.myInstances){
            if(checkingInstance.getImage().equals(instance.getImage())){
                MyImage imageToSetUnused = MyImage.getImageByName(instance.getImage());
                imageToSetUnused.setStatus("Unused");
            }
        }

        // Checking if any volume are effected by this destroy event.
        for(String volumeName : instance.getVolumes()){
            MyVolume vol = MyVolume.getVolumeByName(volumeName);
            if(vol != null){vol.removeContainerNameUsing(instance.getName());}
        }
        DatabaseThread.keepTrackOfVolumes();
    }

    // Helper method to handle the create event of a container.
    private void handleContainerCreateEvent(String containerId) throws LiveStatsException, DatabaseOperationException {
        InspectContainerResponse container = Main.dockerClient.inspectContainerCmd(containerId).exec();

        // Creating our custom MyInstance model.
        MyInstance newInstance = new MyInstance(
                container.getId(),
                container.getName(),
                MyImage.getImageByID(container.getImageId()).getName(),
                container.getState().getStatus(),
                0, 0, 0, 0, 0,
                new ArrayList<String>(),
                container.getNetworkSettings().getIpAddress(),container.getNetworkSettings().getGateway(),
                container.getNetworkSettings().getIpPrefixLen()
        );

        // Adding to it the volumes that are used, if any.
        if(container.getMounts() != null){
            for(InspectContainerResponse.Mount volumeName : container.getMounts()){
                if(volumeName.getName() == null){continue;}
                newInstance.addVolume(volumeName.getName());
                MyVolume vol = MyVolume.getVolumeByName(volumeName.getName());
                assert vol != null;
                vol.addContainerNameUsing(newInstance.getName());
                DatabaseThread.keepTrackOfVolumes();
            }
        }

        // Checking if this creating, converted an image from unused to in use.
        for(MyImage image : Main.myImages) {
            if(newInstance.getImage().equals(image.getName())){
                image.setStatus("In use");
            }
        }

        // Adding it to our list and start monitoring it.
        Main.myInstances.add(newInstance);
        liveMeasure(newInstance.getId());
    }

    /**
     * This method is used to handle the image events.
     * It recognises the type of the event (such us pull or delete) from eventAction and
     * calls the appropriate method to handle it.
     *
     * @param eventAction the action of the event.
     * @param imageName the name of the image the event happened.
     *
     * @throws EventHandlingException if an error occurs when calling the handleImagePullEvent() method.
     */
    private void handleImageEvent(String eventAction, String imageName) throws EventHandlingException {
        try {
            switch (eventAction) {
                case "pull":
                    handleImagePullEvent(imageName);
                    break;
                case "delete":
                case "untag":
                    handleImageDeleteEvent(imageName);
                    break;
            }
        } catch (EventHandlingException e) {
            throw new EventHandlingException("image");
        }
    }

    // Helper method to handle the pull event of an image.
    private void handleImagePullEvent(String imageName) throws EventHandlingException {
        try {
            InspectImageResponse image = Main.dockerClient.inspectImageCmd(imageName).exec();

            // Using the original Image object, we create our custom MyImage model.
            MyImage newImage = new MyImage(
                    image.getRepoTags().get(0),
                    image.getId(),
                    image.getSize(),
                    getImageUsageStatus(image.getRepoTags().get(0))
            );

            // We add it to our list and update the database.
            Main.myImages.add(newImage);
            DatabaseThread.keepTrackOfImages();
        } catch (Exception e) {
            throw new EventHandlingException("image");
        }
    }

    // Helper method to handle the delete event of an image.
    private void handleImageDeleteEvent(String imageName) throws EventHandlingException {
        try {
            MyImage imageToRemove = MyImage.getImageByID(imageName);
            if (imageToRemove != null) {
                // Say goodbye to image.
                DatabaseThread.deleteImage(imageToRemove);
                Main.myImages.remove(imageToRemove);
            }
        } catch (Exception e) {
            throw new EventHandlingException("image");
        }
    }

    /**
     * This method is used to handle the volume events.
     * It recognises the type of the event (such us create or destroy) from eventAction and
     * calls the appropriate method to handle it.
     *
     * @param eventAction the action of the event.
     * @param volumeName the name of the volume the event happened.
     *
     * @throws EventHandlingException if an error occurs when calling the handleVolumeCreateEvent() method.
     */
    public void handleVolumeEvent(String eventAction, String volumeName) throws EventHandlingException {
        try {
            switch (eventAction) {
                case "create":
                    handleVolumeCreateEvent(volumeName);
                    break;
                case "destroy":
                    handleVolumeDestroyEvent(volumeName);
                    break;
            }
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    // Helper method to handle the create event of a volume.
    private void handleVolumeCreateEvent(String volumeName) throws EventHandlingException {
        try {
            InspectVolumeResponse volume = Main.dockerClient.inspectVolumeCmd(volumeName).exec();
            // Creating our custom MyVolume model.
            MyVolume newVolume = new MyVolume(
                    volume.getName(),
                    volume.getDriver(),
                    volume.getMountpoint(),
                    new ArrayList<String>()
            );

            // Checking if any container is using this volume, so to add it in containerNamesUsing.
            for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
                for(ContainerMount volumeMount : container.getMounts()){
                    if(volumeMount.getName() != null && volumeMount.getName().equals(volume.getName())){
                        newVolume.addContainerNameUsing(container.getNames()[0]);
                    }
                }
            }

            // Adding it to our list and update the database.
            Main.myVolumes.add(newVolume);
            DatabaseThread.keepTrackOfVolumes();
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    // Helper method to handle the destroy event of a volume.
    private void handleVolumeDestroyEvent(String volumeName) throws EventHandlingException {
        try {
            MyVolume volumeToRemove = MyVolume.getVolumeByName(volumeName);
            if (volumeToRemove != null) {
                // Sad to see you go, volume.
                DatabaseThread.deleteVolume(volumeToRemove);
                Main.myVolumes.remove(volumeToRemove);
            } else {
                throw new EventHandlingException("null volume");
            }
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    // Helper method to get the status of an image.
    public String getImageUsageStatus(String name){
        // Iterate through the containers and check if the image is used by at least one.
        for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
            if(container.getImage().equals(name)){
                return "In use";
            }
        }
        return "Unused";
    }

    /**
     * CustomResultCallback is a custom implementation of the ResultCallbackTemplate class.
     * It is used to handle the statistics of a Docker container.
     * It includes methods for getting CPU usage, memory usage, number of PIDs, and block I/O statistics.
     */
    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics > {

        // The ID of the container we want to get the statistics.
        public String id;

        // The AsyncDockerCmd object we use to get the statistics.
        private AsyncDockerCmd < StatsCmd,Statistics > asyncStatsCmd;

        /**
         * Constructor for the CustomResultCallback class.
         * @param id The ID of the Docker container.
         * @param asyncStatsCmd The asynchronous command to get the statistics of the Docker container.
         */
        public CustomResultCallback(String id, AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd) {
            this.id = id;
            this.asyncStatsCmd = asyncStatsCmd;

        }

        /**
         * This method is called when the next item is available.
         * It updates the CPU usage, memory usage, number of PIDs, and block I/O statistics of the Docker container.
         * @param stats The statistics of the Docker container.
         */
        @Override
        public void onNext(Statistics stats) {

            // Get and update the CPU usage of the Docker container.
            long cpuUsage = getCpuUsageInNanos(stats);
            if (MyInstance.getInstanceByid(id) != null) {
                if (cpuUsage != 0) {
                    MyInstance.getInstanceByid(id).setCpuUsage((double) cpuUsage / 1_000_000_000);
                } else {
                    MyInstance.getInstanceByid(id).setCpuUsage(0.0);
                }
            }

            // Get and update the memory usage of the Docker container.
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setMemoryUsage(memoryUsage);
            }

            // Get and update the number of PIDs of the Docker container.
            Long pids = stats.getPidsStats().getCurrent();
            if (MyInstance.getInstanceByid(id) != null) {
                if (pids != null) {
                    MyInstance.getInstanceByid(id).setPids(pids);
                } else {
                    MyInstance.getInstanceByid(id).setPids(0);
                }
            }

            // Get and update the block I/O statistics of the Docker container.
            List < BlkioStatEntry > ioServiceBytes = stats.getBlkioStats().getIoServiceBytesRecursive();
            Long readBytes = null;
            if (ioServiceBytes != null) {
                readBytes = getIoServiceBytesValue(ioServiceBytes, "Read");
            }
            Long writeBytes = null;
            if (ioServiceBytes != null) {
                writeBytes = getIoServiceBytesValue(ioServiceBytes, "Write");
            }
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
                MyInstance.getInstanceByid(id).setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            }
        }

        // Helper method to get the CPU usage of a Docker container.
        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();
            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        // Helper method to get the block I/O statistics of a Docker container.
        private Long getIoServiceBytesValue(List < BlkioStatEntry > ioServiceBytes, String type) {
            for (BlkioStatEntry entry: ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        // This method is called when an error occurs.
        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        // This method is called when the stream is closed.
        @Override
        public void onComplete() {
            asyncStatsCmd.close();
        }

        // This method is called when the stream is started.
        @Override
        public void onStart(Closeable closeable) {}
    }
}
