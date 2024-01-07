package gr.aueb.dmst.dockerWatchdog.Threads;

import org.junit.jupiter.api.Test;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;

import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseThreadTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";

    @Test
    void testDatabaseThreadRun() {
        // Create an instance of DatabaseThread
        DatabaseThread databaseThread = new DatabaseThread();

        // Run the thread
        Thread thread = new Thread(databaseThread);
        thread.start();

        // Wait for the thread to finish
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add assertions to check whether the database was populated correctly
        assertTrue(checkIfTableExists("Images"));
        assertTrue(checkIfTableExists("Volumes"));
        assertTrue(checkIfTableExists("Instances"));
        assertTrue(checkIfTableExists("Metrics"));
    }

//    @Test
//    void testUpdateLiveMetrics() {
//        // Insert initial data for testing
//        TestDataProvider.insertTestMetrics();
//        TestDataProvider.insertTestInstances();
//
//        // Create a thread for updating live metrics
//        Thread updateMetricsThread = new Thread(DatabaseThread::updateLiveMetrics);
//
//        // Start the thread
//        updateMetricsThread.start();
//
//        // Wait for the thread to finish
//        try {
//            updateMetricsThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        // Check if metrics have been updated in the database
//        assertTrue(checkIfMetricsUpdated());
//    }
//
//    private boolean checkIfMetricsUpdated() {
//        try {
//            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
//
//            // Check if the metrics have been updated by comparing the timestamp
//            String checkMetricsUpdatedQuery = "SELECT * FROM Instances ORDER BY datetime DESC LIMIT 1";
//            PreparedStatement checkMetricsUpdatedStmt = conn.prepareStatement(checkMetricsUpdatedQuery);
//            ResultSet resultSet = checkMetricsUpdatedStmt.executeQuery();
//
//            if (resultSet.next()) {
//                // Compare the timestamp with the initial timestamp
//                Timestamp initialTimestamp = TestDataProvider.getCurrentTimestamp();
//                Timestamp updatedTimestamp = resultSet.getTimestamp("datetime");
//                return updatedTimestamp.after(initialTimestamp);
//            }
//
//            return false;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    // Helper method to check if a table exists in the database
    public boolean checkIfTableExists(String tablename) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the table exists
            String checkTableExistsQuery = "SHOW TABLES LIKE ?";
            PreparedStatement checkTableExistsStmt = conn.prepareStatement(checkTableExistsQuery);
            checkTableExistsStmt.setString(1, tablename);
            ResultSet resultSet = checkTableExistsStmt.executeQuery();

            // Return true if the table exists, false otherwise
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Test
    void testAddImage() {
        // Arrange
        MyImage testImage = new MyImage("nginx", "a6bd71f48f6839d9faae1f29d3babef831e76bc213107682c5cc80f0cbb30866", 1024L, "active");

        // Act
        TestDataProvider.addImage(testImage);

        // Assert
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Images WHERE id = 'a6bd71f48f6839d9faae1f29d3babef831e76bc213107682c5cc80f0cbb30866'");
            assertTrue(resultSet.next());

            // Check if the data was correctly inserted or updated
            assertEquals("a6bd71f48f6839d9faae1f29d3babef831e76bc213107682c5cc80f0cbb30866", resultSet.getString("id"));
            assertEquals("nginx", resultSet.getString("name"));
            assertEquals(1024L, resultSet.getLong("size"));
            assertEquals("active", resultSet.getString("status"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}