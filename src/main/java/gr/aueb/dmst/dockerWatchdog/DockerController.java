package gr.aueb.dmst.dockerWatchdog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/containers")
public class DockerController {

    private final DockerService dockerService;

    @Autowired
    private InstansRepo instansRepo;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping()
    public ResponseEntity<List<Instans>> getAllInstances() {
        List<Instans> instances = instansRepo.findAll();
        return ResponseEntity.ok(instances);
    }
    @GetMapping("/info")
    public ResponseEntity<String> getDockerInfo() {
        String dockerInfo = dockerService.getDockerInfo();
        return ResponseEntity.ok(dockerInfo);
    }
    @PostMapping("/run")
    public ResponseEntity<String> runContainer(@RequestParam String imageName, @RequestParam String command, @RequestParam String containerName, @RequestParam int port) {
        dockerService.runContainer(imageName, command, containerName, port);
        return ResponseEntity.ok("Container started");
    }
    @PostMapping("/{containerId}/start")
    public ResponseEntity<String> startContainer(@PathVariable("containerId") String containerId) {
        dockerService.startContainer();
        return ResponseEntity.ok("Container " + containerId + " started");
    }

    @PostMapping("/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable("containerId") String containerId) {
        dockerService.stopContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " stopped");
    }
    // New endpoint to rename a container
    @PostMapping("/{oldContainerName}/rename")
    public ResponseEntity<String> renameContainer(@PathVariable String oldContainerName, @RequestParam String newContainerName) {
        dockerService.renameContainer(oldContainerName, newContainerName);
        return ResponseEntity.ok("Container renamed");
    }
    @PostMapping("/{containerId}/remove")
    public ResponseEntity<String> removeContainer(@PathVariable String containerId) {
        dockerService.removeContainer(containerId);
        return ResponseEntity.ok("Container removed");
    }
    @PostMapping("/{containerId}/pause")
    public ResponseEntity<String> pauseContainer(@PathVariable String containerId) {
        dockerService.pauseContainer(containerId);
        return ResponseEntity.ok("Container paused");
    }
    @PostMapping("/{containerId}/unpause")
    public ResponseEntity<String> unpauseContainer(@PathVariable String containerId) {
        dockerService.unpauseContainer(containerId);
        return ResponseEntity.ok("Container unpaused");
    }
    @PostMapping("/pull")
    public ResponseEntity<String> pullImage(@RequestParam String imageName) {
        dockerService.pullImage(imageName);
        return ResponseEntity.ok("Image pulled successfully");
    }






}
