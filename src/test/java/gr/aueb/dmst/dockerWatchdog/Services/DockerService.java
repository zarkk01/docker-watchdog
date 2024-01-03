package gr.aueb.dmst.dockerWatchdog.Services;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import gr.aueb.dmst.dockerWatchdog.Repositories.ImagesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.VolumesRepository;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DockerService {

    private final InstancesRepository instanceRepository;
    private final MetricsRepository metricsRepository;
    private final ImagesRepository imagesRepository;
    private final VolumesRepository volumesRepository;

    public DockerService(InstancesRepository instanceRepository,
                         MetricsRepository metricsRepository,
                         ImagesRepository imagesRepository,
                         VolumesRepository volumesRepository) {
        this.instanceRepository = instanceRepository;
        this.metricsRepository = metricsRepository;
        this.imagesRepository = imagesRepository;
        this.volumesRepository = volumesRepository;
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

    public Instance getInstanceInfo(String id) {
        return instanceRepository.findByContainerId(id);
    }
    public Integer getLastMetricId(){
        return metricsRepository.findLastMetricId();
    }
    public List<Long> getMetrics(Timestamp chosenDate){
        long manyMetric = metricsRepository.countByDatetimeBefore(chosenDate);
        Optional<Metric> metricOptional = metricsRepository.findFirstByDatetimeBeforeOrderByDatetimeDesc(chosenDate);
        int wantedId = 0;
        if(metricOptional.isPresent()){
            wantedId =  metricOptional.get().getId();
        }
        long runningContainers = instanceRepository.countByMetricIdAndStatusRunning(wantedId);
        long totalContainers = instanceRepository.findAllByMetricId(wantedId);
        return List.of(manyMetric, runningContainers, totalContainers, totalContainers - runningContainers);
    }

    public List<Image> getAllImages() {
        return imagesRepository.findAll();
    }
    public List<Volume> getAllVolumes() {
        return volumesRepository.findAll();
    }
    public void createContainer(String imageName) {
        try {
            ExecutorThread.runContainer(imageName);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void startAllContainers(String imageName) {
        // Get all containers of the given image
        List<Instance> containers = instanceRepository.findAllByImageName(imageName);

        // Start each container
        for (Instance container : containers) {
            ExecutorThread.startContainer(container.getId());
        }
    }

    public void stopAllContainers(String imageName) {
        // Get all containers of the given image
        List<Instance> containers = instanceRepository.findAllByImageName(imageName);

        // Stop each container
        for (Instance container : containers) {
            ExecutorThread.stopContainer(container.getId());
        }
    }
}
