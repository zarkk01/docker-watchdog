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