//package gr.aueb.dmst.dockerWatchdog.threads;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.command.InspectContainerCmd;
//import com.github.dockerjava.api.command.InspectContainerResponse;
//import com.github.dockerjava.api.exception.NotFoundException;
//import gr.aueb.dmst.dockerWatchdog.exceptions.ContainerNotFoundException;
//import gr.aueb.dmst.dockerWatchdog.exceptions.ContainerNotModifiedException;
//import gr.aueb.dmst.dockerWatchdog.Main;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import org.junit.jupiter.api.extension.ExtendWith;
//
//public class ExecutorThreadTest {
//
//    @Mock
//    private DockerClient dockerClient;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testStartContainerNotExisting() throws ContainerNotFoundException {
//        String containerId = "conta1nerThatdoesnotexist";
//        InspectContainerResponse response = mock(InspectContainerResponse.class);
//        InspectContainerResponse.ContainerState state = mock(InspectContainerResponse.ContainerState.class);
//
//        when(dockerClient.inspectContainerCmd(containerId)).thenReturn(mock(InspectContainerCmd.class));
//        when(dockerClient.inspectContainerCmd(containerId).exec()).thenReturn(response);
//        when(response.getState()).thenReturn(state);
//        when(state.getRunning()).thenReturn(true);
//
//        assertThrows(ContainerNotFoundException.class, () -> ExecutorThread.startContainer(containerId));
//    }
///*
//    @Test
//    public void testStartContainerAlreadyRunning() throws ContainerNotModifiedException {
//        String containerId = "runningcontainer";
//        InspectContainerResponse response = mock(InspectContainerResponse.class);
//        InspectContainerResponse.ContainerState state = mock(InspectContainerResponse.ContainerState.class);
//
//        when(dockerClient.inspectContainerCmd(containerId)).thenReturn(mock(InspectContainerCmd.class));
//        when(dockerClient.inspectContainerCmd(containerId).exec()).thenReturn(response);
//        when(response.getState()).thenReturn(state);
//        when(state.getRunning()).thenReturn(true);
//
//        assertThrows(ContainerNotModifiedException.class, () -> ExecutorThread.startContainer(containerId));
//    }
//*/
//
//}
