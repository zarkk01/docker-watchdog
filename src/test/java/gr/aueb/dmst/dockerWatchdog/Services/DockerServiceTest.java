package gr.aueb.dmst.dockerWatchdog.Services;

import java.util.Arrays;
import java.util.List;

import gr.aueb.dmst.dockerWatchdog.Models.Instance;
import gr.aueb.dmst.dockerWatchdog.Repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.Repositories.MetricsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DockerServiceTest {

    @InjectMocks
    DockerService dockerService;
    @Mock
    InstancesRepository instancesRepository;
    @Mock
    MetricsRepository metricsRepository;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllInstancesMaxId() {
        Instance instance = new Instance();
        when(instancesRepository.findAllByMaxMetricId()).thenReturn(Arrays.asList(instance));

        List<Instance> result = dockerService.getAllInstancesMaxId();

        assertEquals(1, result.size());
        verify(instancesRepository, times(1)).findAllByMaxMetricId();
    }

    @Test
    public void testGetInstanceInfo() {
        Instance instance = new Instance();
        when(instancesRepository.findByContainerId("1")).thenReturn(instance);

        Instance result = dockerService.getInstanceInfo("1");

        assertEquals(instance, result);
        verify(instancesRepository, times(1)).findByContainerId("1");
    }

    @Test
    public void testGetLastMetricId() {
        when(metricsRepository.findLastMetricId()).thenReturn(1);

        Integer result = dockerService.getLastMetricId();

        assertEquals(1, result);
        verify(metricsRepository, times(1)).findLastMetricId();
    }
}