package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.springframework.stereotype.Service;

@Service
public class DockerService {
    ExecutorThread e = new ExecutorThread();
    private final DockerClient dockerClient;

    public DockerService() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    public String getDockerInfo() {
        // Execute the Docker info command using Docker Java API
        // Return the Docker information as a string
        return dockerClient.infoCmd().exec().toString();
    }

    public void startContainer() {
        e.startContainer();
    }


    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void renameContainer(String oldContainerName, String newContainerName) {
        RenameContainerCmd renameCmd = dockerClient.renameContainerCmd(oldContainerName)
                .withName(newContainerName);
        renameCmd.exec();


    }

    public void runContainer(String imageName, String command, String containerName, int port) {
        // Pull the Docker image (if not already present)
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitSuccess();

        // Create a new container
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withCmd(command)
                .withName(containerName)
                .withHostConfig(
                        HostConfig.newHostConfig()
                                .withPortBindings(PortBinding.parse(port + ":" + port))
                                .withBinds(Bind.parse("/hostPath:/containerPath")) // Optional volume bind
                )
                .exec();

        // Start the container
        dockerClient.startContainerCmd(container.getId()).exec();
    }
    public void removeContainer(String containerId) {
        RemoveContainerCmd removeCmd = dockerClient.removeContainerCmd(containerId);
        removeCmd.exec();
    }
    public void pauseContainer(String containerId) {
        PauseContainerCmd pauseCmd = dockerClient.pauseContainerCmd(containerId);
        pauseCmd.exec();
    }
    public void unpauseContainer(String containerId) {
        UnpauseContainerCmd unpauseCmd = dockerClient.unpauseContainerCmd(containerId);
        unpauseCmd.exec();
    }
    public void pullImage(String imageName) {
        PullImageCmd pullCmd = dockerClient.pullImageCmd(imageName);
        pullCmd.exec(new PullImageResultCallback()).awaitSuccess();
    }



}
