package gr.aueb.dmst.dockerWatchdog.Tests.Threads;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseThreadTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";

    @BeforeEach
    void setUp() {
            try {
                // Establish a connection
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

                // Create the Metrics table
                String createMetricsTable = "CREATE TABLE IF NOT EXISTS Metrics (" +
                        "id INT AUTO_INCREMENT, " +
                        "datetime TIMESTAMP, " +
                        "PRIMARY KEY(id))";
                PreparedStatement createMetricsStmt = conn.prepareStatement(createMetricsTable);
                createMetricsStmt.execute();

                // Create the Instances table
                String createInstancesTable = "CREATE TABLE IF NOT EXISTS Instances (" +
                        // ... (columns and constraints)
                        "PRIMARY KEY(id, metricid))";
                PreparedStatement createInstancesStmt = conn.prepareStatement(createInstancesTable);
                createInstancesStmt.execute();

                // Create the Images table
                String createImagesTable = "CREATE TABLE IF NOT EXISTS Images (" +
                        // ... (columns and constraints)
                        "PRIMARY KEY(id))";
                PreparedStatement createImagesStmt = conn.prepareStatement(createImagesTable);
                createImagesStmt.execute();

                // Create the Volumes table
                String createVolumesTable = "CREATE TABLE IF NOT EXISTS Volumes (" +
                        // ... (columns and constraints)
                        "PRIMARY KEY(name))";
                PreparedStatement createVolumesStmt = conn.prepareStatement(createVolumesTable);
                createVolumesStmt.execute();

                // Close the connection
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    @AfterEach
    void tearDown() {
        //drop db
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Drop the database if it exists
            String dropDatabase = "DROP DATABASE IF EXISTS docker_database";
            PreparedStatement dropDatabaseStmt = conn.prepareStatement(dropDatabase);
            dropDatabaseStmt.execute();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @Test
    void testDatabaseThreadRun() {
        // Create an instance of DatabaseThread
        DatabaseThread databaseThread = new DatabaseThread();

        // Run the thread
        databaseThread.run();

        // Add assertions to check whether the database was populated correctly
        assertTrue(checkIfTableExists("Metrics"));
        assertTrue(checkIfTableExists("Instances"));
        assertTrue(checkIfTableExists("Images"));
        assertTrue(checkIfTableExists("Volumes"));


    }

    @Test
    void testUpdateLiveMetrics() {
        // Insert initial data for testing
        TestDataProvider.insertTestMetrics();
        TestDataProvider.insertTestInstances();

        // Create a thread for updating live metrics
        Thread updateMetricsThread = new Thread(DatabaseThread::updateLiveMetcrics);

        // Start the thread
        updateMetricsThread.start();

        // Allow the thread to run for a certain duration (e.g., 5 seconds)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt the thread to stop continuous updates
        updateMetricsThread.interrupt();

        // Check if metrics have been updated in the database
        assertTrue(checkIfMetricsUpdated());
    }

    private boolean checkIfMetricsUpdated() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the metrics have been updated by comparing the timestamp
            String checkMetricsUpdatedQuery = "SELECT * FROM Instances ORDER BY datetime DESC LIMIT 1";
            PreparedStatement checkMetricsUpdatedStmt = conn.prepareStatement(checkMetricsUpdatedQuery);
            ResultSet resultSet = checkMetricsUpdatedStmt.executeQuery();

            if (resultSet.next()) {
                // Compare the timestamp with the initial timestamp
                Timestamp initialTimestamp = TestDataProvider.getCurrentTimestamp();
                Timestamp updatedTimestamp = resultSet.getTimestamp("datetime");
                return updatedTimestamp.after(initialTimestamp);
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to check if a table exists in the database
    private boolean checkIfTableExists(String tableName) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the table exists
            String checkTableExistsQuery = "SHOW TABLES LIKE ?";
            PreparedStatement checkTableExistsStmt = conn.prepareStatement(checkTableExistsQuery);
            checkTableExistsStmt.setString(1, tableName);
            ResultSet resultSet = checkTableExistsStmt.executeQuery();

            // Return true if the table exists, false otherwise
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        }


}