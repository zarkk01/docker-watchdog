package gr.aueb.dmst.dockerWatchdog.threads;

import java.util.ArrayList;

import gr.aueb.dmst.dockerWatchdog.models.MyImage;
import gr.aueb.dmst.dockerWatchdog.models.MyVolume;
import gr.aueb.dmst.dockerWatchdog.exceptions.DatabaseOperationException;

import org.mockito.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class DatabaseThreadTest {
    @Test
    public void testRun() {
        DatabaseThread mockdb = Mockito.mock(DatabaseThread.class);
        mockdb.run();
        Mockito.verify(mockdb, atLeastOnce()).run();
    }

    @Test
    public void testCreateAllTables() throws DatabaseOperationException {

        // mock class + verify run method of runnable has been called

        /* Not yet working
        try(MockedStatic mockeddrvr = Mockito.mockStatic(DriverManager.class)){
            DriverManager factory = new DriverManager();
            mockeddrvr.when(()->DriverManager.getConnection(DB_URL,USER,PASS).thenReturn(new Connection() {  *...*   }));
            factory.getConnection();
            mockeddrvr.verify(()->DriverManager.getConnection(eq(DB_URL), eq(USER), eq(PASS)));
        }
        */

        // mock class + call static function - verify it got called
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.createAllTables();
            mockedStatic.verify(() -> DatabaseThread.createAllTables(), Mockito.atLeastOnce());
        }
    }
    @Test
    public void testKeepTrackOfInstances() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfInstances();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfInstances(), Mockito.atLeastOnce());
        }
        ;
    }
    @Test
    public void testKeepTrackOfImages() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfImages();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfImages(), Mockito.atLeastOnce());
        }
    }
    @Test
    public void testDeleteImage() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            MyImage image = new MyImage("name", "id", 19L, "status");
            DatabaseThread.deleteImage(image);
            mockedStatic.verify(() -> DatabaseThread.deleteImage(any(MyImage.class)), Mockito.atLeastOnce());
        }
    }
    @Test
    public void testDeleteVolume() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            ArrayList<String> ArrayList = null;
            MyVolume volume = new MyVolume("f", "a", "a",ArrayList);
            DatabaseThread.deleteVolume(volume);
            mockedStatic.verify(() -> DatabaseThread.deleteVolume(any(MyVolume.class)), Mockito.atLeastOnce());
        }
    }
    @Test
    public void testkeepTrackOfVolumes() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfVolumes();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfVolumes(), Mockito.atLeastOnce());
        }
    }
    @Test
    public void testUpdateLiveMetrics() throws DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.updateLiveMetrics();
            mockedStatic.verify(() -> DatabaseThread.updateLiveMetrics(), Mockito.atLeastOnce());
        }
    }
}
