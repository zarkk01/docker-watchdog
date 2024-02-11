package gr.aueb.dmst.dockerWatchdog.Services;

import java.util.Arrays;
import java.util.List;

import gr.aueb.dmst.dockerWatchdog.models.Instance;
import gr.aueb.dmst.dockerWatchdog.api.repositories.InstancesRepository;
import gr.aueb.dmst.dockerWatchdog.api.repositories.MetricsRepository;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ApiServiceTest {

    @InjectMocks
    ApiService apiService;
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

        List<Instance> result = apiService.getAllInstancesMaxId();

        assertEquals(1, result.size());
        verify(instancesRepository, times(1)).findAllByMaxMetricId();
    }

    @Test
    public void testGetInstanceInfo() {
        Instance instance = new Instance();
        when(instancesRepository.findByContainerId("1")).thenReturn(instance);

        Instance result = apiService.getInstanceInfo("1");

        assertEquals(instance, result);
        verify(instancesRepository, times(1)).findByContainerId("1");
    }

    @Test
    public void testGetLastMetricId() {
        when(metricsRepository.findLastMetricId()).thenReturn(1);

        Integer result = apiService.getLastMetricId();

        assertEquals(1, result);
        verify(metricsRepository, times(1)).findLastMetricId();
    }
}