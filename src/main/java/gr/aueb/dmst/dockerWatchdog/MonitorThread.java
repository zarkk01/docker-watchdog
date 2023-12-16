package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.EventsResultCallback;

import java.util.List;

public class MonitorThread implements Runnable {

    @Override
    public void run() {
        fillLists();
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
                        container.getConfig().getLabels(),
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
                    containerInfo.getConfig().getLabels(),
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
}
