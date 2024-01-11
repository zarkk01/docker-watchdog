package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import gr.aueb.dmst.dockerWatchdog.Services.DockerService;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/images")
    public List<Image> getAllImages() {
        return dockerService.getAllImages();
    }

    @GetMapping("/volumes")
    public List<Volume> getAllVolumes() {
        return dockerService.getAllVolumes();
    }

    @PostMapping("/images/create/{imageName}")
    public ResponseEntity<String> createContainer(@PathVariable("imageName") String imageName) {
        dockerService.createContainer(imageName);
        return ResponseEntity.ok("Container created");
    }

    @PostMapping("/containers/{containerId}/start")
    public ResponseEntity<String> startContainer(@PathVariable("containerId") String containerId) {
        dockerService.startContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " started");
    }

    @PostMapping("/containers/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable("containerId") String containerId) {
        dockerService.stopContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " stopped");
    }

    @PostMapping("/containers/{containerId}/restart")
    public ResponseEntity<String> restartContainer(@PathVariable("containerId") String containerId) {
        dockerService.restartContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " restarted");
    }

    @PostMapping("/containers/{containerId}/delete")
    public ResponseEntity<String> deleteContainer(@PathVariable("containerId") String containerId){
        dockerService.removeContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + " deleted"));
    }

    @PostMapping("/containers/{containerId}/rename")
    public ResponseEntity<String> renameContainer(@PathVariable("containerId") String containerId,@RequestParam("newName") String newName){
        dockerService.renameContainer(containerId,newName);
        return ResponseEntity.ok(("Container " + containerId + " renamed"));
    }

    @PostMapping("/containers/{containerId}/pause")
    public ResponseEntity<String> pauseContainer(@PathVariable("containerId") String containerId){
        dockerService.pauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "paused"));
    }

    @PostMapping("/containers/{containerId}/unpause")
    public ResponseEntity<String> unpauseContainer(@PathVariable("containerId") String containerId){
        dockerService.unpauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "unpaused"));
    }

    @GetMapping("/containers/instances")
    public List<Instance> getAllInstances() {
        return dockerService.getAllInstancesMaxId();
    }

    @GetMapping("/containers/metrics")
    public List<Long> getMetrics(@RequestParam("chosenDate") String chosenDateString) {
        Timestamp chosenDate = Timestamp.valueOf(chosenDateString);
        return dockerService.getMetrics(chosenDate);
    }

    @GetMapping("/containers/lastMetricId")
    public Integer getLastMetricId() {
        return dockerService.getLastMetricId();
    }

    @GetMapping("/containers/{containerId}/info")
    public ResponseEntity<?> getInstanceInfo(@PathVariable("containerId") String containerId) {
        try {
            Instance instance = dockerService.getInstanceInfo(containerId);
            if (instance == null) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.ok(instance);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error"); // 500 Internal Server Error
        }
    }

    @PostMapping("/containers/startAll/{imageName}")
    public ResponseEntity<String> startAllContainers(@PathVariable("imageName") String imageName) {
        dockerService.startAllContainers(imageName);
        return ResponseEntity.ok("All containers started");
    }

    @PostMapping("/containers/stopAll/{imageName}")
    public ResponseEntity<String> stopAllContainers(@PathVariable("imageName") String imageName) {
        dockerService.stopAllContainers(imageName);
        return ResponseEntity.ok("All containers stopped");
    }

    @PostMapping("/images/pull/{imageName}")
    public ResponseEntity<String> pullImage(@PathVariable("imageName") String imageName) {
        dockerService.pullImage(imageName);
        return ResponseEntity.ok("Image pulled " + imageName);
    }

    @PostMapping("/images/remove/{imageName}")
    public ResponseEntity<String> removeImage(@PathVariable("imageName") String imageName) {
        dockerService.removeImage(imageName);
        return ResponseEntity.ok("Image removed " + imageName);
    }
}
