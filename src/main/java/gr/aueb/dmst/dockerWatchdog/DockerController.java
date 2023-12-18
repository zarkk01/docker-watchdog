package gr.aueb.dmst.dockerWatchdog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
