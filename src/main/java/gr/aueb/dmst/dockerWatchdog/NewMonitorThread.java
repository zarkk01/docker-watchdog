package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.EventsResultCallback;

import java.util.List;

public class NewMonitorThread implements Runnable {

    @Override
    public void run() {
        fillList();
        startListening();
    }

    public void startListening() {
        Main.dockerClient.eventsCmd().exec(new EventsResultCallback() {
            @Override
            public void onNext(Event event) {
                EventType eventType = event.getType();
                String eventAction = event.getAction();
                String containerId = event.getActor().getId();

                if (eventType == EventType.CONTAINER) {
                    switch (eventAction) {
                        case "start":
                        case "unpause":
                            // Find the corresponding instance and set its status to "Up running"
                            MyInstance instance = MyInstance.getInstanceByid(containerId);
                            if (instance != null) {
                                instance.setStatus("running");
                            }
                            break;
                        case "stop":
                        case "die":
                            // Find the corresponding instance and set its status to "Exited"
                            instance = MyInstance.getInstanceByid(containerId);
                            if (instance != null) {
                                instance.setStatus("exited");
                            }
                            break;
                        case "pause":
                            // Find the corresponding instance and set its status to "Paused"
                            instance = MyInstance.getInstanceByid(containerId);
                            if (instance != null) {
                                instance.setStatus("paused");
                            }
                            break;
                        case "rename":
                            // Find the corresponding instance and update its name
                            instance = MyInstance.getInstanceByid(containerId);
                            if (instance != null) {
                                instance.setName(event.getActor().getAttributes().get("name"));
                            }
                            break;
                        case "destroy":
                            // Remove the corresponding instance from the list
                            instance = MyInstance.getInstanceByid(containerId);
                            if (instance != null) {
                                Main.myInstancesList.remove(instance);
                            }
                            break;
                        case "create":
                            // Add the new instance to the list
                            InspectContainerResponse container = Main.dockerClient.inspectContainerCmd(containerId).exec();
                            MyInstance newInstance = new MyInstance(
                                    container.getId(),
                                    container.getName(),
                                    container.getImageId(),
                                    container.getState().getStatus(),
                                    container.getConfig().getLabels(),
                                    container.getSizeRootFs(),
                                    0, 0, 0, 0, 0,
                                    getContainerPorts(container.getId())
                            );
                            Main.myInstancesList.add(newInstance);
                            break;
                    }
                }
            }
        });
    }

    public void fillList() {
        // Get all Docker containers
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // Iterate over the containers
        for (Container container : containers) {
            // Inspect the container to get its details
            InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();

            // Create a new MyInstance object for the container
            MyInstance instance = new MyInstance(
                    containerInfo.getId(),
                    containerInfo.getName(),
                    containerInfo.getImageId(),
                    containerInfo.getState().getStatus(),
                    containerInfo.getConfig().getLabels(),
                    0,
                    0, 0, 0, 0, 0,
                    getContainerPorts(containerInfo.getId())
            );

            // Add the new instance to the instancesList
            Main.myInstancesList.add(instance);
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
}
