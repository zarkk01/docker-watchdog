package gr.aueb.dmst.dockerWatchdog.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import gr.aueb.dmst.dockerWatchdog.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.model.Instance;
import gr.aueb.dmst.dockerWatchdog.model.Metric;
import gr.aueb.dmst.dockerWatchdog.repositories.InstanceRepository;
import gr.aueb.dmst.dockerWatchdog.repositories.MetricsRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    private final InstanceRepository instanceRepository;
    private final MetricsRepository metricsRepository;

    public DockerService(InstanceRepository instanceRepository, MetricsRepository metricsRepository) {
        this.dockerClient = DockerClientBuilder.getInstance().build();
        this.instanceRepository = instanceRepository;
        this.metricsRepository = metricsRepository;
    }

    public void startContainer(String containerId) {
        ExecutorThread.startContainer(containerId);
    }

    public void stopContainer(String containerId) {
        ExecutorThread.stopContainer(containerId);
    }

    public List<Instance> getAllInstancesMaxId() {
        return instanceRepository.findAllByMaxMetricId();
    }

    public List<Metric> getMetrics(Timestamp startDate, Timestamp endDate){
        return metricsRepository.findAllByDatetimeBetween(startDate, endDate);
    }
}
