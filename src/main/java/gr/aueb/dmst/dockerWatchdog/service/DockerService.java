package gr.aueb.dmst.dockerWatchdog.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import gr.aueb.dmst.dockerWatchdog.model.Instance;
import gr.aueb.dmst.dockerWatchdog.repositories.InstanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    private final InstanceRepository instanceRepository;

    public DockerService(InstanceRepository instanceRepository) {
        this.dockerClient = DockerClientBuilder.getInstance().build();
        this.instanceRepository = instanceRepository;
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId);
    }

    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public List<Instance> getAllInstancesMaxId() {
        return instanceRepository.findAllByMaxMetricId();
    }
}
