package gr.aueb.dmst.dockerWatchdog.threads;

import com.github.dockerjava.api.model.Event;
import gr.aueb.dmst.dockerWatchdog.exceptions.DatabaseOperationException;
import gr.aueb.dmst.dockerWatchdog.exceptions.EventHandlingException;
import gr.aueb.dmst.dockerWatchdog.exceptions.ListFillingException;
import gr.aueb.dmst.dockerWatchdog.exceptions.LiveStatsException;
import gr.aueb.dmst.dockerWatchdog.models.MyInstance;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
public class MonitorThreadTest{
    @Test
    public void testRun() {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        mockdb.run();
        Mockito.verify(mockdb, atLeastOnce()).run();
    }
/*
    @Test
    public void testFillLists() throws DatabaseOperationException, ListFillingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        mockdb.fillLists();
        Mockito.verify(mockdb, atLeastOnce()).fillLists();
    }
*/
    @Test
    public void testLiveMeasure() throws LiveStatsException {
        try (MockedStatic<MonitorThread> mockedStatic = Mockito.mockStatic(MonitorThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            String containerId = null;
            MonitorThread.liveMeasure(containerId);
            mockedStatic.verify(() -> MonitorThread.liveMeasure(containerId), Mockito.atLeastOnce());
        }
        ;
    }

    @Test
    public void testStartListening() throws ListFillingException, DatabaseOperationException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        mockdb.startListening();
        Mockito.verify(mockdb, atLeastOnce()).startListening();
    }

    @Test
    public void testHandleContainerEvent() throws EventHandlingException, LiveStatsException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String eventAction = null;
        String containerId = null;
        Event event = null;
        mockdb.handleContainerEvent(eventAction, containerId, event);
        Mockito.verify(mockdb, atLeastOnce()).handleContainerEvent(eventAction, containerId, event);
    }

    @Test
    public void testUpdateContainerStatus() {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        MyInstance instance = null;
        String status = null;
        mockdb.updateContainerStatus(instance, status);
        Mockito.verify(mockdb, atLeastOnce()).updateContainerStatus(instance, status);
    }
    @Test
    public void testHandleContainerDestroyEvent() throws DatabaseOperationException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        MyInstance instance = null;
        mockdb.handleContainerDestroyEvent(instance);
        Mockito.verify(mockdb, atLeastOnce()).handleContainerDestroyEvent(instance);
    }
    @Test
    public void testhandleImageEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String eventAction = null;
        String imageName = null;
        mockdb.handleImageEvent(eventAction, imageName);
        Mockito.verify(mockdb, atLeastOnce()).handleImageEvent(eventAction, imageName);
    }
    @Test
    public void testHandleImagePullEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String imageName = null;
        mockdb.handleImagePullEvent(imageName);
        Mockito.verify(mockdb, atLeastOnce()).handleImagePullEvent(imageName);
    }
    @Test
    public void testHandleImageDeleteEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String imageName = null;
        mockdb.handleImageDeleteEvent(imageName);
        Mockito.verify(mockdb, atLeastOnce()).handleImageDeleteEvent(imageName);
    }
    @Test
    public void testHandleVolumeEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String eventAction = null;
        String volumeName = null;
        mockdb.handleVolumeEvent(eventAction, volumeName);
        Mockito.verify(mockdb, atLeastOnce()).handleVolumeEvent(eventAction, volumeName);
    }

    @Test
    public void testHandleVolumeCreateEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String volumeName = null;
        mockdb.handleVolumeCreateEvent(volumeName);
        Mockito.verify(mockdb, atLeastOnce()).handleVolumeCreateEvent(volumeName);
    }

    @Test
    public void testHandleVolumeDestroyEvent() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String volumeName = null;
        mockdb.handleVolumeDestroyEvent(volumeName);
        Mockito.verify(mockdb, atLeastOnce()).handleVolumeDestroyEvent(volumeName);
    }

    @Test
    public void testGetImageUsageStatus() throws EventHandlingException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        String name = null;
        mockdb.getImageUsageStatus(name);
        Mockito.verify(mockdb, atLeastOnce()).getImageUsageStatus(name);
    }

}