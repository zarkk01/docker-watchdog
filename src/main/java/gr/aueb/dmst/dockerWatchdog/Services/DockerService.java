package gr.aueb.dmst.dockerWatchdog.Services;

import com.github.dockerjava.api.DockerClient;
import gr.aueb.dmst.dockerWatchdog.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstanceRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    private final InstanceRepository instanceRepository;
    private final MetricsRepository metricsRepository;

    public DockerService(InstanceRepository instanceRepository, MetricsRepository metricsRepository) {
        this.dockerClient = Main.dockerClient;
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
