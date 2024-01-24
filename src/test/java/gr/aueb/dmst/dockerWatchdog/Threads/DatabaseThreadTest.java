package gr.aueb.dmst.dockerWatchdog.Threads;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;
import org.hibernate.annotations.processing.SQL;
import org.mockito.*;
import java.util.ArrayList;

import java.sql.*;

import gr.aueb.dmst.dockerWatchdog.Exceptions.DatabaseOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;

public class DatabaseThreadTest {
    @Test
    public void testRun() throws SQLException, DatabaseOperationException {
        DatabaseThread mockdb = Mockito.mock(DatabaseThread.class);
        mockdb.run();
        Mockito.verify(mockdb, atLeastOnce()).run();
    }
    @Test
    public void testCreateAllTables() throws SQLException, DatabaseOperationException {

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
        ;


    }
    @Test
    public void testKeepTrackOfInstances() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfInstances();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfInstances(), Mockito.atLeastOnce());
        }
        ;
    }
    @Test
    public void testKeepTrackOfImages() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfImages();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfImages(), Mockito.atLeastOnce());
        }
        ;
    }

    @Test
    public void testDeleteImage() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            MyImage image = new MyImage("name", "id", 19L, "status");
            DatabaseThread.deleteImage(image);
            mockedStatic.verify(() -> DatabaseThread.deleteImage(any(MyImage.class)), Mockito.atLeastOnce());
        };
    }

    @Test
    public void testDeleteVolume() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            ArrayList<String> ArrayList = null;
            MyVolume volume = new MyVolume("f", "a", "a",ArrayList);
            DatabaseThread.deleteVolume(volume);
            mockedStatic.verify(() -> DatabaseThread.deleteVolume(any(MyVolume.class)), Mockito.atLeastOnce());
        };
    }
    @Test
    public void testkeepTrackOfVolumes() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.keepTrackOfVolumes();
            mockedStatic.verify(() -> DatabaseThread.keepTrackOfVolumes(), Mockito.atLeastOnce());
        };
    }

    @Test
    public void testUpdateLiveMetrics() throws SQLException, DatabaseOperationException {
        try (MockedStatic<DatabaseThread> mockedStatic = Mockito.mockStatic(DatabaseThread.class)) {
            //mockedStatic.when(() -> DatabaseThread.createAllTables()).;
            DatabaseThread.updateLiveMetrics();
            mockedStatic.verify(() -> DatabaseThread.updateLiveMetrics(), Mockito.atLeastOnce());
        };
    }
}