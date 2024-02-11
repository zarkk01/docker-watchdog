package gr.aueb.dmst.dockerWatchdog.api.controllers;

import java.util.ArrayList;
import java.util.List;

import gr.aueb.dmst.dockerWatchdog.api.entities.Image;
import gr.aueb.dmst.dockerWatchdog.api.entities.Volume;
import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;

import gr.aueb.dmst.dockerWatchdog.api.controllers.ApiController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiControllerTest {

    @Test
    public void testGetAllImages() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        List<Image> mockImages = new ArrayList<>();
        Mockito.when(apiServiceMock.getAllImages()).thenReturn(mockImages);

        ApiController dockerController = new ApiController(apiServiceMock);
        List<Image> result = dockerController.getAllImages();

        assertNotNull(result);
        assertEquals(mockImages, result);
    }

    @Test
    public void testGetAllVolumes() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        List<Volume> mockVolumes = new ArrayList<>();
        Mockito.when(apiServiceMock.getAllVolumes()).thenReturn(mockVolumes);

        ApiController dockerController = new ApiController(apiServiceMock);
        List<Volume> result = dockerController.getAllVolumes();

        assertNotNull(result);
        assertEquals(mockVolumes, result);
    }

    @Test
    public void testStartContainer() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        String containerId = "containerId";

        ApiController dockerController = new ApiController(apiServiceMock);
        ResponseEntity<String> responseEntity = dockerController.startContainer(containerId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Container " + containerId + " started", responseEntity.getBody());
        Mockito.verify(apiServiceMock).startContainer(containerId);
    }

    @Test
    public void testStopContainer() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        String containerId = "containerId";

        ApiController dockerController = new ApiController(apiServiceMock);
        ResponseEntity<String> responseEntity = dockerController.stopContainer(containerId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Container " + containerId + " stopped", responseEntity.getBody());
        Mockito.verify(apiServiceMock).stopContainer(containerId);
    }

    @Test
    public void testStartAllContainers() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        String imageName = "imageName";

        ApiController dockerController = new ApiController(apiServiceMock);
        ResponseEntity<String> responseEntity = dockerController.startAllContainers(imageName);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("All containers started", responseEntity.getBody());
        Mockito.verify(apiServiceMock).startAllContainers(imageName);
    }

    @Test
    public void testStopAllContainers() {
        ApiService apiServiceMock = Mockito.mock(ApiService.class);
        String imageName = "imageName";

        ApiController dockerController = new ApiController(apiServiceMock);
        ResponseEntity<String> responseEntity = dockerController.stopAllContainers(imageName);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("All containers stopped", responseEntity.getBody());
        Mockito.verify(apiServiceMock).stopAllContainers(imageName);
    }
}
