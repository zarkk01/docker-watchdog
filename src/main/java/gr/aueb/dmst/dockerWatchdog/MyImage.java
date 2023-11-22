package gr.aueb.dmst.dockerWatchdog;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

public class MyImage {

    private final String id;
    private final Long size;

    public MyImage(String id, Long size) {
        this.id = id;
        this.size = size;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for size
    public Long getSize() {
        return size;
    }

    public static int countUsedImages(DockerClient dockerClient) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        List<String> usedImageIds = containers.stream().map(Container::getImageId).distinct().toList();

        return usedImageIds.size();
    }

    @Override
    public String toString() {
        return "id = "+ id +  ", size = " + size;
    }

}