package gr.aueb.dmst.dockerWatchdog.Threads;

import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.EventsResultCallback;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.MyImage;
import gr.aueb.dmst.dockerWatchdog.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;

import java.io.Closeable;
import java.util.List;

public class MonitorThread implements Runnable {

    @Override
    public void run() {
        try {
            fillLists();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in filllists");
        }
        liveMeasure();
        startListening();
    }

    public void startListening() {
        Main.dockerClient.eventsCmd().exec(new EventsResultCallback() {
            @Override
            public void onNext(Event event) {
                EventType eventType = event.getType();
                String eventAction = event.getAction();
                String id = event.getActor().getId();

                switch (eventType) {
                    case CONTAINER:
                        handleContainerEvent(eventAction, id,event);
                        break;
                    case IMAGE:
                        handleImageEvent(eventAction, id,event);
                        break;
                }
            }
        });
    }

    private void handleContainerEvent(String eventAction, String containerId,Event event) {
        switch (eventAction) {
            case "start":
            case "unpause":
                // Find the corresponding instance and set its status to "Up running"
                MyInstance instance = MyInstance.getInstanceByid(containerId);
                if (instance != null) {
                    instance.setStatus("running");
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
            case "stop":
            case "die":
                // Find the corresponding instance and set its status to "Exited"
                instance = MyInstance.getInstanceByid(containerId);
                if (instance != null) {
                    instance.setStatus("exited");
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
            case "pause":
                // Find the corresponding instance and set its status to "Paused"
                instance = MyInstance.getInstanceByid(containerId);
                if (instance != null) {
                    instance.setStatus("paused");
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
            case "rename":
                // Find the corresponding instance and update its name
                instance = MyInstance.getInstanceByid(containerId);
                if (instance != null) {
                    instance.setName(event.getActor().getAttributes().get("name"));
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
            case "destroy":
                // Remove the corresponding instance from the list
                instance = MyInstance.getInstanceByid(containerId);
                if (instance != null) {
                    Main.myInstancesList.remove(instance);
                }
                boolean isThere = false;
                for(MyInstance inst : Main.myInstancesList){
                    if(inst.getImage().equals(instance.getImage())){
                        isThere = true;
                    }
                }
                if(!isThere){
                    MyImage imageToSetUnused = MyImage.getImageByName(instance.getImage());
                    imageToSetUnused.setStatus("Unused");
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
            case "create":
                // Add the new instance to the list
                InspectContainerResponse container = Main.dockerClient.inspectContainerCmd(containerId).exec();
                MyInstance newInstance = new MyInstance(
                        container.getId(),
                        container.getName(),
                        MyImage.getImageByID(container.getImageId()).getName(),
                        container.getState().getStatus(),
                        0, 0, 0, 0, 0,
                        getContainerPorts(container.getId())
                );
                for(MyImage image : Main.myImagesList) {
                    if(newInstance.getImage().equals(image.getName())){
                        image.setStatus("In use");
                    }
                }
                Main.myInstancesList.add(newInstance);
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
        }
    }

    private void handleImageEvent(String eventAction, String imageName,Event event) {
        switch (eventAction) {
            case "pull":
                // Add the new image to the list
                InspectImageResponse image = Main.dockerClient.inspectImageCmd(imageName).exec();
                boolean isThere = false;
                for(MyImage ima : Main.myImagesList){
                    if(ima.getId().equals(image.getId())){
                        isThere = true;
                    }
                }
                if (!isThere) {
                    MyImage newImage = new MyImage(
                            image.getRepoTags().get(0),
                            image.getId(),
                            image.getSize(),
                            getImageUsageStatus(image.getRepoTags().get(0))
                    );
                    Main.myImagesList.add(newImage);
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }

                break;
            case "delete":
            case "untag":
                MyImage imageToRemove = MyImage.getImageByName(imageName);
                if (imageToRemove != null) {
                    Main.myImagesList.remove(imageToRemove);
                }
                if (!Main.dbThread.isAlive()) {
                    Main.dbThread = new Thread(new DatabaseThread());
                    Main.dbThread.start();
                }
                break;
        }
    }

    public void fillLists() {
        // Get all Docker containers
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Get all Docker images
        List<Image> images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();

        // Iterate over the images
        for (Image image : images) {
            // Inspect the image to get its details
            InspectImageResponse imageInfo = Main.dockerClient.inspectImageCmd(image.getId()).exec();

            // Create a new MyImage object for the image
            MyImage newImage = new MyImage(
                    imageInfo.getRepoTags().get(0),
                    imageInfo.getId(),
                    imageInfo.getSize(),
                    getImageUsageStatus(imageInfo.getRepoTags().get(0))
            );

            // Add the new image to the imagesList
            Main.myImagesList.add(newImage);
        }

        // Iterate over the containers
        for (Container container : containers) {
            // Inspect the container to get its details
            InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();

            // Create a new MyInstance object for the container
            MyInstance newInstance = new MyInstance(
                    containerInfo.getId(),
                    containerInfo.getName(),
                    MyImage.getImageByID(containerInfo.getImageId()).getName(),
                    containerInfo.getState().getStatus(),
                    0, 0, 0, 0, 0,
                    getContainerPorts(containerInfo.getId())
            );

            // Add the new instance to the instancesList
            Main.myInstancesList.add(newInstance);
        }
        if (!Main.dbThread.isAlive()) {
            Main.dbThread = new Thread(new DatabaseThread());
            Main.dbThread.start();
        }
    }
    private static String getContainerPorts(String containerId) {
        // Use the Docker Java API to inspect the container
        InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(containerId).exec();

        // Get the bindings map
        Ports ports = containerInfo.getNetworkSettings().getPorts();
        ExposedPort[] exposedPorts = ports.getBindings().keySet().toArray(new ExposedPort[0]);

        // Check if there are any exposed ports
        if (exposedPorts.length > 0) {
            ExposedPort exposedPort = exposedPorts[0];
            Ports.Binding[] bindings = ports.getBindings().get(exposedPort);

            // Check if there are any bindings for the exposed port
            if(bindings != null){
                if (bindings.length > 0) {
                    Ports.Binding binding = bindings[0];
                    // Format and return the ports string
                    return binding.getHostPortSpec() + ":" + exposedPort.getPort();
                }
            }
        }

        // Return a default value if no ports are found
        return "No ports found";
    }

    public String getImageUsageStatus(String name){
        for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
            if(container.getImage().equals(name)){
                return "In use";
            }
        }
        return "Unused";
    }

    public static void liveMeasure() {

        // Lists of all containers of docker desktop using dockerClient that is initiated in Main.java
        List < Container > containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // For every container in containers list
        for (Container container: containers) {
            // Get the id of the container
            String id = container.getId();
            // Get the stats of the container
            AsyncDockerCmd<StatsCmd, Statistics > asyncStatsCmd = Main.dockerClient.statsCmd(id);
            try {
                // Execute the statsCmd and call CustomResultCallback
                asyncStatsCmd.exec(new CustomResultCallback(id, asyncStatsCmd));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void liveMeasureForNewContainer(String id) {
        AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd = Main.dockerClient.statsCmd(id);
        try {
            // Execute the statsCmd and call CustomResultCallback
            asyncStatsCmd.exec(new CustomResultCallback(id, asyncStatsCmd));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CustomResultCallback that extends ResultCallbackTemplate and implements Statistics class
    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics > {

        // Initiate id and asyncStatsCmd
        public String id;
        private AsyncDockerCmd < StatsCmd,Statistics > asyncStatsCmd;

        // Constructor
        public CustomResultCallback(String id, AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd) {
            this.id = id;
            this.asyncStatsCmd = asyncStatsCmd;

        }

        // Override onNext method of ResultCallbackTemplate class that is called
        // when a new statistics is received from the docker daemon
        @Override
        public void onNext(Statistics stats) {

            // CPU stats
            long cpuUsage = getCpuUsageInNanos(stats);

            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                if (cpuUsage != 0) {
                    // If cpuUsage is not 0 then set the cpuUsage of the instance with the value of cpuUsage
                    MyInstance.getInstanceByid(id).setCpuUsage((double) cpuUsage / 1_000_000_000);
                } else {
                    // If cpuUsage is 0 then set the cpuUsage of the instance with 0
                    MyInstance.getInstanceByid(id).setCpuUsage(0.0);
                }
            }

            // Memory stats
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setMemoryUsage(memoryUsage);
            }

            // Process IDs (PIDs) stats
            Long pids = stats.getPidsStats().getCurrent();
            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                if (pids != null) {
                    MyInstance.getInstanceByid(id).setPids(pids);
                } else {
                    MyInstance.getInstanceByid(id).setPids(0);
                }
            }

            // BLCKIO (block I/O) stats
            List < BlkioStatEntry > ioServiceBytes = stats.getBlkioStats().getIoServiceBytesRecursive();
            Long readBytes = null;

            // If ioServiceBytes is not null then get the value of Read
            if (ioServiceBytes != null) {
                readBytes = getIoServiceBytesValue(ioServiceBytes, "Read");
            }
            Long writeBytes = null;
            // If ioServiceBytes is not null then get the value of Write
            if (ioServiceBytes != null) {
                writeBytes = getIoServiceBytesValue(ioServiceBytes, "Write");
            }

            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
                MyInstance.getInstanceByid(id).setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            }
        }

        // Method getCpuUsageInNanos that returns the CPU usage in nanoseconds
        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();
            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        // Method getIoServiceBytesValue that returns the value of ioServiceBytes
        private Long getIoServiceBytesValue(List < BlkioStatEntry > ioServiceBytes, String type) {
            for (BlkioStatEntry entry: ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        // Override onError method of ResultCallbackTemplate class
        // that is called when some error occurs
        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        // Override onComplete method of ResultCallbackTemplate
        @Override
        public void onComplete() {
            // Close the AsyncDockerCmd
            asyncStatsCmd.close();
        }

        // Override onStart method of ResultCallbackTemplate that do nothing
        @Override
        public void onStart(Closeable closeable) {}
    }
}
