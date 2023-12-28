package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.Metric;
import gr.aueb.dmst.dockerWatchdog.Services.DockerService;
import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/containers")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PostMapping("/{containerId}/start")
    public ResponseEntity<String> startContainer(@PathVariable("containerId") String containerId) {
        dockerService.startContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " started");
    }

    @PostMapping("/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable("containerId") String containerId) {
        dockerService.stopContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " stopped");
    }

    @PostMapping("/{containerId}/delete")
    public ResponseEntity<String> deleteContainer(@PathVariable("containerId") String containerId){
        dockerService.deleteContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + " deleted"));
    }

    @PostMapping("/{containerId}/rename")
    public ResponseEntity<String> renameContainer(@PathVariable("containerId") String containerId,@RequestParam("newName") String newName){
        dockerService.renameContainer(containerId,newName);
        return ResponseEntity.ok(("Container " + containerId + " renamed"));
    }

    @PostMapping("/{containerId}/pause")
    public ResponseEntity<String> pauseContainer(@PathVariable("containerId") String containerId){
        dockerService.pauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "paused"));
    }

    @PostMapping("/{containerId}/unpause")
    public ResponseEntity<String> unpauseContainer(@PathVariable("containerId") String containerId){
        dockerService.unpauseContainer(containerId);
        return ResponseEntity.ok(("Container " + containerId + "unpaused"));
    }

    @GetMapping("/instances")
    public List<Instance> getAllInstances() {
        return dockerService.getAllInstancesMaxId();
    }

    @GetMapping("/metrics")
    public List<Long> getMetrics(@RequestParam("chosenDate") String chosenDateString) {
        Timestamp chosenDate = Timestamp.valueOf(chosenDateString);
        return dockerService.getMetrics(chosenDate);
    }
}
