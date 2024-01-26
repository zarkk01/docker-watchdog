package gr.aueb.dmst.dockerWatchdog.Threads;

import gr.aueb.dmst.dockerWatchdog.Exceptions.DatabaseOperationException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ListFillingException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.LiveStatsException;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
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

    @Test
    public void testFillLists() throws ListFillingException, DatabaseOperationException {
        MonitorThread mockdb = Mockito.mock(MonitorThread.class);
        mockdb.fillLists();
        Mockito.verify(mockdb, atLeastOnce()).run();
    }

    @Test
    public void testLiveMeasure() throws DatabaseOperationException, ListFillingException, LiveStatsException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            String containerId = "123";
            MonitorThread.liveMeasure(containerId);
            mockedStatic.verify(() -> MonitorThread.liveMeasure(containerId), Mockito.atLeastOnce());
        }
        ;
    }
}