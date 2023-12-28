package gr.aueb.dmst.dockerWatchdog.Services;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Repositories.ImagesRepository;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class DockerService {

    private final InstancesRepository instanceRepository;
    private final MetricsRepository metricsRepository;
    private final ImagesRepository imagesRepository;

    public DockerService(InstancesRepository instanceRepository, MetricsRepository metricsRepository, ImagesRepository imagesRepository) {
        this.instanceRepository = instanceRepository;
        this.metricsRepository = metricsRepository;
        this.imagesRepository = imagesRepository;
    }

    public void startContainer(String containerId) {
        ExecutorThread.startContainer(containerId);
    }

    public void stopContainer(String containerId) {
        ExecutorThread.stopContainer(containerId);
    }

    public void deleteContainer(String containerId){
        ExecutorThread.removeContainer(containerId);
    }

    public void renameContainer(String containerId, String newName){
        ExecutorThread.renameContainer(containerId,newName);
    }

    public void pauseContainer(String containerId){
        ExecutorThread.pauseContainer(containerId);
    }

    public void unpauseContainer(String containerId){
        ExecutorThread.unpauseContainer(containerId);
    }

    public void restartContainer(String containerId){
        ExecutorThread.stopContainer(containerId);
        ExecutorThread.startContainer(containerId);
    }

    public List<Instance> getAllInstancesMaxId() {
        return instanceRepository.findAllByMaxMetricId();
    }

    public List<Long> getMetrics(Timestamp chosenDate){
        long manyMetric = metricsRepository.countByDatetimeBefore(chosenDate);
        Optional<Metric> metricOptional = metricsRepository.findFirstByDatetimeBeforeOrderByDatetimeDesc(chosenDate);
        int wantedId = 0;
        if(metricOptional.isPresent()){
            wantedId =  metricOptional.get().getId();
        }
        long runningContainers = instanceRepository.countByMetricIdAndStatusRunning(wantedId);
        return List.of(manyMetric, runningContainers);
    }

    public List<Image> getAllImages() {
        return imagesRepository.findAll();
    }
    public void createContainer(String imageName) {
        try {
            ExecutorThread.runContainer(imageName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
