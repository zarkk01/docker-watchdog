package gr.aueb.dmst.dockerWatchdog.Tests.Threads;

import java.sql.*;

public class TestDataProvider {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";

    public static void insertTestMetrics() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test data into the Metrics table
            String insertMetrics = "INSERT INTO Metrics (datetime) VALUES (?)";
            PreparedStatement insertMetricsStmt = conn.prepareStatement(insertMetrics);
            insertMetricsStmt.setTimestamp(1, getCurrentTimestamp());
            insertMetricsStmt.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTestInstances() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test data into the Instances table
            String insertInstance = "INSERT INTO Instances (id, name, image, status, memoryusage, pids, cpuusage, blockI, blockO, volumes, subnet, gateway, prefixlen, metricid) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertInstanceStmt = conn.prepareStatement(insertInstance);
            insertInstanceStmt.setString(1, "yourInstanceId");
            insertInstanceStmt.setString(2, "InstanceName");
            insertInstanceStmt.setString(3, "ImageName");
            // Set other values accordingly
            insertInstanceStmt.setLong(13, getLatestMetricId());
            insertInstanceStmt.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTestImages() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test data into the Images table
            String insertImage = "INSERT INTO Images (id, name, size, status) VALUES (?, ?, ?, ?)";
            PreparedStatement insertImageStmt = conn.prepareStatement(insertImage);
            insertImageStmt.setString(1, "yourImageId");
            insertImageStmt.setString(2, "ImageName");
            insertImageStmt.setLong(3, 1024); // Example size
            insertImageStmt.setString(4, "active");
            insertImageStmt.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTestVolumes() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test data into the Volumes table
            String insertVolume = "INSERT INTO Volumes (name, driver, mountpoint, containerNamesUsing) VALUES (?, ?, ?, ?)";
            PreparedStatement insertVolumeStmt = conn.prepareStatement(insertVolume);
            insertVolumeStmt.setString(1, "yourVolumeName");
            insertVolumeStmt.setString(2, "local");
            insertVolumeStmt.setString(3, "/mnt/data");
            insertVolumeStmt.setString(4, "Container1,Container2");
            insertVolumeStmt.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static java.sql.Timestamp getCurrentTimestamp() {
        // Implement logic to get the current timestamp
        // This depends on your specific requirements and database setup
        return new java.sql.Timestamp(System.currentTimeMillis());
    }

    private static int getLatestMetricId() {
        // Implement logic to get the latest metric ID from the Metrics table
        // This depends on your specific requirements and database setup
        return 1; // Replace with actual logic to retrieve the latest metric ID
    }
    // Method to insert test container data into the database
    public static void insertTestContainer() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test container data into the Containers table
            String insertContainerQuery = "INSERT INTO Containers (id, state, created_at) VALUES (?, ?, ?)";
            PreparedStatement insertContainerStmt = conn.prepareStatement(insertContainerQuery);
            insertContainerStmt.setString(1, "test_container_id");
            insertContainerStmt.setString(2, "created");
            insertContainerStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertContainerStmt.executeUpdate();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to get the ID of the test container from the database
    public static String getTestContainerId() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Query to get the ID of the test container
            String getContainerIdQuery = "SELECT id FROM Containers WHERE state = 'created' ORDER BY created_at DESC LIMIT 1";
            PreparedStatement getContainerIdStmt = conn.prepareStatement(getContainerIdQuery);
            var resultSet = getContainerIdStmt.executeQuery();

            // Get the container ID
            String containerId = null;
            if (resultSet.next()) {
                containerId = resultSet.getString("id");
            }

            // Close the connection
            conn.close();

            return containerId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getTestContainerName() {
        String containerName = null;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Query to get the test container name from the Instances table
            String getContainerNameQuery = "SELECT name FROM Instances WHERE id = ?";
            PreparedStatement getContainerNameStmt = conn.prepareStatement(getContainerNameQuery);
            getContainerNameStmt.setString(1, "testContainerId"); // Assuming the test container ID

            ResultSet resultSet = getContainerNameStmt.executeQuery();

            // Check if the result set has a row
            if (resultSet.next()) {
                containerName = resultSet.getString("name");
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return containerName;
    }
}
