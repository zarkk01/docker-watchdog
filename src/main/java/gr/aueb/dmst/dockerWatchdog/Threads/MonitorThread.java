package gr.aueb.dmst.dockerWatchdog.Threads;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import gr.aueb.dmst.dockerWatchdog.Exceptions.LiveStatsException;


/**
 * This Thread is communicating with the Docker API and provide the state of the Docker Cluster to the app.
 * Starts by filling the lists with Containers, Images and Volumes and then sends LiveStatsCallback so
 * to maintain the state of the cluster in real time. After that, it opens a listener to the Docker API
 * and hears for events happening in the cluster. When an event occurs, it recognises it and updates the lists
 * accordingly.
 */
public class MonitorThread implements Runnable {

    // Logger instance used mainly for errors
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
            // Filling the lists with our custom Docker components
            fillLists();
            // Start the live monitoring of the Docker Cluster with null parameter
            // so to get the statistics of ALL containers.
            liveMeasure(null);
            // Start listening for events happening in the Docker Cluster
            startListening();
        } catch (ListFillingException | LiveStatsException | DatabaseOperationException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * This method fills the lists with our custom My... models after getting info from original Docker Components.
     * In this way we can handle effectively the Docker Cluster and maintain control. Then, since custom lists are filled,
     * it calls the method to set up the database so to store the custom lists.
     *
     * @throws ListFillingException with the appropriate message for the error.
     * @throws DatabaseOperationException if an error occurs when createAllTables() is called.
     */
    public void fillLists() throws ListFillingException, DatabaseOperationException {
        // Images list is filled with MyImage objects.
        try {
            // Getting the list of images from the Docker API
            List<Image> images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();
            // Iterate through the images and get the info of each one
            for (Image image : images) {
                InspectImageResponse imageInfo = Main.dockerClient.inspectImageCmd(image.getId()).exec();
                // Creating our custom MyImage model
                MyImage newImage = new MyImage(
                        imageInfo.getRepoTags().get(0),
                        imageInfo.getId(),
                        imageInfo.getSize(),
                        getImageUsageStatus(imageInfo.getRepoTags().get(0))
                );
                // Adding it to our list of MyImage objects
                Main.myImages.add(newImage);
            }
        } catch (Exception e) {
            // If an error occurs, throw an exception specifying that Images list is not filled
            throw new ListFillingException("Images");
        }

        // Containers list is filled with MyInstance objects
        try {
            // Getting the list of containers from the Docker API
            List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
            // Iterate through the containers and get the info of each one
            for (Container container : containers) {
                InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();
                // Creating our custom MyInstance model
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

                // Checking for volumes and adding them to the list
                if (containerInfo.getMounts() != null) {
                    for (InspectContainerResponse.Mount volumeName : containerInfo.getMounts()) {
                        newInstance.addVolume(volumeName.getName());
                    }
                }
                // Adding it to our list of MyInstance objects
                Main.myInstances.add(newInstance);
            }
        } catch (Exception e) {
            // If an error occurs, throw an exception specifying that Instances list is not filled
            throw new ListFillingException("Instances");
        }

        // Volumes list is filled with MyVolume objects
        try {
            // Getting the list of volumes from the Docker API
            List<InspectVolumeResponse> volumes = Main.dockerClient.listVolumesCmd().exec().getVolumes();
            // Iterate through the volumes and get the info of each one
            for (InspectVolumeResponse volume : volumes) {
                // Creating our custom MyVolume model.
                MyVolume newVolume = new MyVolume(
                        volume.getName(),
                        volume.getDriver(),
                        volume.getMountpoint(),
                        new ArrayList<String>()
                );
                // Checking for containers using this volume and adding them to the list
                for (Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()) {
                    // For each container, check if it uses this volume and add it to the list
                    for (ContainerMount volumeName : container.getMounts()) {
                        if (volumeName.getName() == null) {continue; }
                        if (volumeName.getName().equals(volume.getName())) {
                            newVolume.addContainerNameUsing(container.getNames()[0]);
                        }
                    }
                }
                // Adding it to our list of MyVolume objects
                Main.myVolumes.add(newVolume);
            }
        } catch (Exception e) {
            // If an error occurs, throw an exception specifying that Volumes list is not filled
            throw new ListFillingException("Volumes");
        }

        // After filling the lists, we are ready to set up our database
        // since it will store the contents of these lists.
        DatabaseThread.setUpDatabase();
    }

    /**
     * This method is used to get the live statistics of the Docker Cluster.
     * It uses LiveStatsCallback to get the statistics of each container
     * and then updates the lists accordingly. liveMeasure() is responsible for
     * maintaining the state of every container's CPU, Memory, Block I/O and PIDs.
     *
     * @param containerId the id of the container we want to get the statistics.
     * @throws LiveStatsException if an error occurs when calling the exec() method.
     */
    public static void liveMeasure(String containerId) throws LiveStatsException {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Iterate through the containers and get the statistics of each one
        for (Container container : containers) {
            // If the containerId is null, get the statistics of all containers
            if (containerId == null || container.getId().equals(containerId)) {
                AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd = Main.dockerClient.statsCmd(container.getId());
                try {
                    // Use LiveStatsCallback to get the statistics
                    asyncStatsCmd.exec(new LiveStatsCallback(container.getId(), asyncStatsCmd));
                } catch (Exception e) {
                    throw new LiveStatsException();
                }
            }
        }
    }

    /**
     * This method is used to listen for events happening in the Docker Cluster.
     * It uses the Docker API to get the events, and then it recognises the type
     * of the event and calls the appropriate method to handle it. It makes use of the
     * EventResultCallback to get the events and then update the lists accordingly.
     */
    public void startListening() {
        // Get the events happening in the Docker Cluster
        Main.dockerClient.eventsCmd().exec(new EventsResultCallback() {
            @Override
            public void onNext(Event event) {

                // Get the info of the event
                EventType eventType = event.getType();
                String eventAction = event.getAction();
                String id = event.getActor().getId();

                // Recognise the type of the event and call the appropriate method to handle it
                switch (eventType) {
                    case CONTAINER:
                        try {
                            // If it is a container event, call the method to handle it
                            handleContainerEvent(eventAction, id, event);
                        } catch (EventHandlingException  e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case IMAGE:
                        try {
                            // If it is an image event, call the method to handle it
                            handleImageEvent(eventAction, id);
                        } catch (EventHandlingException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case VOLUME:
                        try {
                            // If it is a volume event, call the method to handle it
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
     * @throws EventHandlingException if an error occurs
     */
    public void handleContainerEvent(String eventAction, String containerId, Event event) throws EventHandlingException {
        // Given the ID, we get the custom MyInstance object
        MyInstance instance = MyInstance.getInstanceByID(containerId);
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
                    // To get the name of the container, we use event.getActor(), since we are provided only with containerId
                    instance.setName(event.getActor().getAttributes().get("name"));
                    break;
                case "destroy":
                    handleContainerDestroyEvent(instance);
                    break;
                case "create":
                    handleContainerCreateEvent(containerId);
                    break;
            }
            // Update the instances table in the database
            DatabaseThread.keepTrackOfInstances();
            // Also, update the images table in the database because the status of an image may change
            DatabaseThread.keepTrackOfImages();
        } catch (Exception e) {
            throw new EventHandlingException("container");
        }
    }

    /**
     * Helper method that updates the status of a given instance.
     *
     * @param instance The instance whose status is to be updated.
     * @param status The new status to be set for the instance.
     */
    public void updateContainerStatus(MyInstance instance, String status) {
        instance.setStatus(status);
    }

    /**
     * Helper method that handles the destroy event of a container.
     * This method removes the instance from the list of instances, checks if the image used by this instance
     * is used by any other instance. If not, it sets the image status to "Unused". It also checks if any volume
     * is affected by this destroy event and removes the instance from the list of instances using the volume.
     * Finally, it updates the volumes table in the database.
     *
     * @param instance The instance of the container that is destroyed.
     * @throws DatabaseOperationException if an error occurs when updating the volumes table in the database.
     */
    public void handleContainerDestroyEvent(MyInstance instance) throws DatabaseOperationException {
        Main.myInstances.remove(instance);
        // Checking if this event, converted an image from in use to unused
        boolean found = false;
        for (MyInstance checkingInstance : Main.myInstances) {
            // If another container is using this image, we don't change its status
            if (checkingInstance.getImage().equals(instance.getImage())) {
                found = true;
                break;
            }
        }
        // If no other container is using this image, we change its status to unused
        if (!found) {
            // Iterate through images
            for (MyImage image : Main.myImages) {
                if (image.getName().equals(instance.getImage())) {
                    // Since no other container is using this image, we change its status to unused
                    image.setStatus("Unused");
                }
            }
        }

        // Checking if any volume are effected by this destroy event
        for(String volumeName : instance.getVolumes()){
            // Get the volume by its name
            MyVolume vol = MyVolume.getVolumeByName(volumeName);
            // Remove the container from the list of containers using this particular volume
            if (vol != null) {vol.removeContainerNameUsing(instance.getName()); }
        }
        // Update the volumes table in the database
        DatabaseThread.keepTrackOfVolumes();
    }

    /**
     * Helper method that handles the creation event of a container.
     * This method retrieves the information of the newly created container, creates a new instance of MyInstance
     * for that container, and adds it to the list of MyInstance objects. It also checks if the container uses any volumes
     * and adds them to the list of volumes used by this container. If the image used by this container was previously unused,
     * it changes its status to "In use". Finally, it starts the live monitoring of the new container.
     *
     * @param containerId The ID of the container that is created.
     * @throws LiveStatsException if an error occurs when starting the live monitoring of the new container.
     * @throws DatabaseOperationException if an error occurs when updating the volumes table in the database.
     */
    public void handleContainerCreateEvent(String containerId) throws LiveStatsException, DatabaseOperationException {
        // Get the info of the newborn container
        InspectContainerResponse container = Main.dockerClient.inspectContainerCmd(containerId).exec();

        // Creating our custom MyInstance model for that container
        MyInstance newInstance = new MyInstance(
                container.getId(),
                container.getName(),
                MyImage.getImageByID(container.getImageId()).getName(),
                container.getState().getStatus(),
                0, 0, 0, 0, 0,
                new ArrayList<String>(),
                container.getNetworkSettings().getIpAddress(), container.getNetworkSettings().getGateway(),
                container.getNetworkSettings().getIpPrefixLen()
        );

        // Adding to our custom MyInstance the volumes that are used, if any
        if (container.getMounts() != null) {
            // For each volume, add it to the list of volumes used by this container
            for (InspectContainerResponse.Mount volumeName : container.getMounts()) {
                if (volumeName.getName() == null) {continue;}
                // Here we add the volume to the list of volumes used by this container
                newInstance.addVolume(volumeName.getName());

                // Get the volume by its name and add the container to the list of containers using this volume
                MyVolume vol = MyVolume.getVolumeByName(volumeName.getName());
                if (vol != null) {vol.addContainerNameUsing(newInstance.getName());}

                // Update the volumes table in the database
                DatabaseThread.keepTrackOfVolumes();
            }
        }

        // Checking if this creating, converted an image from unused to in use.
        for (MyImage image : Main.myImages) {
            if (newInstance.getImage().equals(image.getName())) {
                image.setStatus("In use");
            }
        }

        // Adding it to our list of MyInstance objects
        Main.myInstances.add(newInstance);
        // Start the live monitoring(CPU, PIDs..) of the newborn container
        liveMeasure(newInstance.getId());
    }

    /**
     * This method is used to handle the image events.
     * It recognises the type of the event (such us pull or delete) from eventAction and
     * calls the appropriate method to handle it.
     *
     * @param eventAction the action of the event.
     * @param imageId the ID of the image the event happened.
     *
     * @throws EventHandlingException if an error occurs when calling the handleImagePullEvent() method.
     */
    public void handleImageEvent(String eventAction, String imageId) throws EventHandlingException {
        try {
            // Recognise the type of the event and call the appropriate method to handle it
            switch (eventAction) {
                case "pull":
                    // If it is a pull event, call the method to handle it
                    handleImagePullEvent(imageId);
                    break;
                case "delete":
                case "untag":
                    // If it is a delete event, call the method to handle it
                    handleImageDeleteEvent(imageId);
                    break;
            }
        } catch (EventHandlingException e) {
            throw new EventHandlingException("image");
        }
    }

    /**
     * Helper method that handles the pull event of an image.
     * This method retrieves the information of the pulled image, creates a new instance of MyImage
     * for that image, and adds it to the list of MyImage objects. It also updates the images table in the database.
     *
     * @param imageId The ID of the image that is pulled.
     * @throws EventHandlingException if an error occurs when retrieving the image information or updating the database.
     */
    public void handleImagePullEvent(String imageId) throws EventHandlingException {
        try {
            // Get the info of the pulled image given its name
            InspectImageResponse image = Main.dockerClient.inspectImageCmd(imageId).exec();
            // Using the original Image object, we create our custom MyImage model
            MyImage newImage = new MyImage(
                    image.getRepoTags().get(0),
                    image.getId(),
                    image.getSize(),
                    getImageUsageStatus(image.getRepoTags().get(0))
            );

            // We add it to our list and update the database
            Main.myImages.add(newImage);
            // Update the images table in the database
            DatabaseThread.keepTrackOfImages();
        } catch (Exception e) {
            throw new EventHandlingException("image");
        }
    }

    /**
     * Helper method that checks and returns the usage status of a given image.
     * This method iterates through all the containers and checks if the image is used by at least one container.
     * If the image is in use, it returns "In use". If not, it returns "Unused".
     *
     * @param name The name of the image whose usage status is to be checked.
     * @return The usage status of the image. It can be either "In use" or "Unused".
     */
    public String getImageUsageStatus(String name){
        // Iterate through the containers and check if the image is used by at least one.
        for (Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()) {
            // If we find at least one container using this image, we return "In use"
            if (container.getImage().equals(name)) {
                return "In use";
            }
        }
        // If the image is not used by any container, return "Unused"
        return "Unused";
    }

    /**
     * Helper method that handles the delete event of an image.
     * This method retrieves the image to be deleted using its ID, removes it from the list of MyImage objects,
     * and updates the images table in the database. If the image does not exist, it does nothing.
     *
     * @param imageId The name of the image that is deleted.
     * @throws EventHandlingException if an error occurs when retrieving the image information or updating the database.
     */
    public void handleImageDeleteEvent(String imageId) throws EventHandlingException {
        try {
            // Make use of our custom's MyImage method getImageByID() to get the image given its ID
            MyImage imageToRemove = MyImage.getImageByID(imageId);
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
            // Recognise the type of the event and call the appropriate method to handle it
            switch (eventAction) {
                case "create":
                    // If it is a create event, call the method to handle it
                    handleVolumeCreateEvent(volumeName);
                    break;
                case "destroy":
                    // If it is a destroy event, call the method to handle it
                    handleVolumeDestroyEvent(volumeName);
                    break;
            }
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    /**
     * Helper method that handles the creation event of a volume.
     * This method retrieves the information of the newly created volume, creates a new instance of MyVolume
     * for that volume, and adds it to the list of MyVolume objects. It also checks if any container uses this volume
     * and adds them to the list of containers using this volume. Finally, it updates the volumes table in the database.
     *
     * @param volumeName The name of the volume that is created.
     * @throws EventHandlingException if an error occurs when retrieving the volume information or updating the database.
     */
    public void handleVolumeCreateEvent(String volumeName) throws EventHandlingException {
        try {
            // Get the info of the created volume
            InspectVolumeResponse volume = Main.dockerClient.inspectVolumeCmd(volumeName).exec();
            // Creating our custom MyVolume model
            MyVolume newVolume = new MyVolume(
                    volume.getName(),
                    volume.getDriver(),
                    volume.getMountpoint(),
                    new ArrayList<String>()
            );

            // Checking if any container is using this volume, so to add it in containerNamesUsing
            for (Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()) {
                // For each container, check if it uses this volume and add it to the list
                for (ContainerMount volumeMount : container.getMounts()) {
                    if (volumeMount.getName() != null && volumeMount.getName().equals(volume.getName())) {
                        // Here we add the container to the list of containers using this volume
                        newVolume.addContainerNameUsing(container.getNames()[0]);
                    }
                }
            }

            // Adding it to our list and update the database
            Main.myVolumes.add(newVolume);
            // Update the volumes table in the database
            DatabaseThread.keepTrackOfVolumes();
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    /**
     * Helper method that handles the destroy event of a volume.
     * This method retrieves the volume to be deleted using its name, removes it from the list of MyVolume objects,
     * and updates the volumes table in the database. If the volume does not exist, it throws an EventHandlingException.
     *
     * @param volumeName The name of the volume that is deleted.
     * @throws EventHandlingException if the volume does not exist or an error occurs when updating the database.
     */
    public void handleVolumeDestroyEvent(String volumeName) throws EventHandlingException {
        try {
            // Make use of our custom's MyVolume method getVolumeByName() to get the volume given its name
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

    /**
     * LiveStatsCallback extends ResultCallbackTemplate class and is responsible for
     * getting the statistics of a Docker container and updating the lists accordingly. Every new or existing
     * container sends its own LiveStatsCallback to maintain its state in real time.
     * It includes methods for getting CPU usage, memory usage, number of PIDs, and block I/O statistics.
     */
    private static class LiveStatsCallback extends ResultCallbackTemplate<LiveStatsCallback, Statistics > {

        // The ID of the container we want to get the statistics
        public String containerId;

        // The AsyncDockerCmd object we use to get the statistics
        private AsyncDockerCmd <StatsCmd, Statistics> asyncStatsCmd;

        /**
         * Constructor for the LiveStatsCallback class.
         *
         * @param id The ID of the Docker container.
         * @param asyncStatsCmd The asynchronous command to get the statistics of the Docker container.
         */
        public LiveStatsCallback(String id, AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd) {
            this.containerId = id;
            this.asyncStatsCmd = asyncStatsCmd;
        }

        /**
         * This method is called when the next item is available meaning that the statistics are updated.
         * It updates the CPU usage, memory usage, number of PIDs, and block I/O statistics of the Docker container.
         *
         * @param stats The statistics of the Docker container.
         */
        @Override
        public void onNext(Statistics stats) {
            // Get the custom MyInstance object given the containerId
            MyInstance instance = MyInstance.getInstanceByID(containerId);

            // Get and update the CPU usage of the Docker container
            long cpuUsage = getCpuUsageInNanos(stats);
            if (instance != null) {
                if (cpuUsage != 0) {
                   instance.setCpuUsage((double) cpuUsage / 1_000_000_000);
                } else {
                    instance.setCpuUsage(0.0);
                }
            }

            // Get and update the memory usage of the Docker container.
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            if (instance != null) {
                instance.setMemoryUsage(memoryUsage);
            }

            // Get and update the number of PIDs of the Docker container.
            Long pids = stats.getPidsStats().getCurrent();
            if (instance != null) {
                if (pids != null) {
                    instance.setPids(pids);
                } else {
                    instance.setPids(0);
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
            if (instance != null) {
               instance.setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
                instance.setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            }
        }

        // Helper method to get the CPU usage of a Docker container
        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();
            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        // Helper method to get the block I/O statistics of a Docker container
        private Long getIoServiceBytesValue(List < BlkioStatEntry > ioServiceBytes, String type) {
            for (BlkioStatEntry entry: ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        // This method is called when an error occurs
        @Override
        public void onError(Throwable throwable) {
            logger.error(throwable.getMessage());
        }

        // This method is called when the stream is closed and the statistics are complete
        @Override
        public void onComplete() {
            asyncStatsCmd.close();
        }

        // This method is called when the stream is started and the statistics are available
        @Override
        public void onStart(Closeable closeable) {}
    }
}
