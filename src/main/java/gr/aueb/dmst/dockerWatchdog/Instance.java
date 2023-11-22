package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.model.Container;
import java.util.List;

public class Instance {
    private final String id;
    private String name;
    private final String image;
    private String status;

    // constructor
    public Instance(String id ,String name ,String image ,String status){
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
    }

    @Override
    public String toString() {
        return "id = "+ id +", " +
                "name =" + name +" ,image = " + image
                + " ,status = " + status;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for image
    public String getImage() {
        return image;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    private Container getContainerByName(String containerName) {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + containerName)) {
                    return container;
                }
            }
        }
        return null; // Container with the specified name not found
    }
    // Setter for name
    public void setName(String currentName ,String newName) {
        this.name = name;
        // Retrieve the Container object corresponding to the currentName
        Container container = getContainerByName(currentName);

        if (container != null) {
            // Rename the specified container
            Main.dockerClient.renameContainerCmd(container.getId())
                    .withName(newName)
                    .exec();

            System.out.println("Container renamed successfully from " + currentName + " to " + newName + ".");
        } else {
            System.out.println("The container with the name " + currentName + " does not exist!");
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}