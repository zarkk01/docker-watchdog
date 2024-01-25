package gr.aueb.dmst.dockerWatchdog.Controllers;

import java.util.ArrayList;
import java.util.List;

import gr.aueb.dmst.dockerWatchdog.Models.Image;
import gr.aueb.dmst.dockerWatchdog.Models.Volume;
import gr.aueb.dmst.dockerWatchdog.Services.DockerService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DockerControllerTest {

    @Test
    public void testGetAllImages() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        List<Image> mockImages = new ArrayList<>();
        Mockito.when(dockerServiceMock.getAllImages()).thenReturn(mockImages);

        DockerController dockerController = new DockerController(dockerServiceMock);
        List<Image> result = dockerController.getAllImages();

        assertNotNull(result);
        assertEquals(mockImages, result);
    }

    @Test
    public void testGetAllVolumes() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        List<Volume> mockVolumes = new ArrayList<>();
        Mockito.when(dockerServiceMock.getAllVolumes()).thenReturn(mockVolumes);

        DockerController dockerController = new DockerController(dockerServiceMock);
        List<Volume> result = dockerController.getAllVolumes();

        assertNotNull(result);
        assertEquals(mockVolumes, result);
    }

    @Test
    public void testStartContainer() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        String containerId = "containerId";

        DockerController dockerController = new DockerController(dockerServiceMock);
        ResponseEntity<String> responseEntity = dockerController.startContainer(containerId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Container " + containerId + " started", responseEntity.getBody());
        Mockito.verify(dockerServiceMock).startContainer(containerId);
    }

    @Test
    public void testStopContainer() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        String containerId = "containerId";

        DockerController dockerController = new DockerController(dockerServiceMock);
        ResponseEntity<String> responseEntity = dockerController.stopContainer(containerId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Container " + containerId + " stopped", responseEntity.getBody());
        Mockito.verify(dockerServiceMock).stopContainer(containerId);
    }

    @Test
    public void testStartAllContainers() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        String imageName = "imageName";

        DockerController dockerController = new DockerController(dockerServiceMock);
        ResponseEntity<String> responseEntity = dockerController.startAllContainers(imageName);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("All containers started", responseEntity.getBody());
        Mockito.verify(dockerServiceMock).startAllContainers(imageName);
    }

    @Test
    public void testStopAllContainers() {
        DockerService dockerServiceMock = Mockito.mock(DockerService.class);
        String imageName = "imageName";

        DockerController dockerController = new DockerController(dockerServiceMock);
        ResponseEntity<String> responseEntity = dockerController.stopAllContainers(imageName);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("All containers stopped", responseEntity.getBody());
        Mockito.verify(dockerServiceMock).stopAllContainers(imageName);
    }
}
