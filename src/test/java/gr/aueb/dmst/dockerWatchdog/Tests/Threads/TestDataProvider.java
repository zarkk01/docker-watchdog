package gr.aueb.dmst.dockerWatchdog.Tests.Threads;

import gr.aueb.dmst.dockerWatchdog.Models.MyImage;

import java.sql.*;
import java.util.Date;
import java.util.Random;

public class TestDataProvider {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";

    public static void insertTestMetrics() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert test da/ta into the Metrics table
            String insertMetrics = "INSERT INTO Metrics (id ,datetime) VALUES (?,?)";
            PreparedStatement insertMetricsStmt = conn.prepareStatement(insertMetrics);
            insertMetricsStmt.setInt(1, new Random().nextInt(1000));
            insertMetricsStmt.setTimestamp(2,  new Timestamp(new Date().getTime()));
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
            insertInstanceStmt.setString(1, Math.random() + "");
            insertInstanceStmt.setString(2, "InstanceName");
            insertInstanceStmt.setString(3, "ImageName");
            insertInstanceStmt.setString(4, "running");
            insertInstanceStmt.setInt(5, 100);
            insertInstanceStmt.setInt(6, 9);
            insertInstanceStmt.setDouble(7, 1.1);
            insertInstanceStmt.setDouble(8, 3.3);
            insertInstanceStmt.setDouble(9, 2.2);
            insertInstanceStmt.setString(10, "Volume1,Volume2");
            insertInstanceStmt.setString(11, "subnet");
            insertInstanceStmt.setString(12, "gateway");
            insertInstanceStmt.setInt(13, 24);
            insertInstanceStmt.setInt(14, 3);
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
            String insertImage = "INSERT INTO Images (id, name, status, size) VALUES (?, ?, ?, ?)";
            PreparedStatement insertImageStmt = conn.prepareStatement(insertImage);
            insertImageStmt.setString(1, Math.random() + "");
            insertImageStmt.setString(2, "ImageName");
            insertImageStmt.setString(4, "In use");
            insertImageStmt.setLong(3, 1024);
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
            insertVolumeStmt.setString(1, Math.random() + "");
            insertVolumeStmt.setString(2, "local");
            insertVolumeStmt.setString(3, "/mnt/data");
            insertVolumeStmt.setString(4, "Container1,Container2");
            insertVolumeStmt.executeUpdate();

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
            String getContainerIdQuery = "SELECT id FROM Instances WHERE status = 'running'";
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

    public static void addImage(MyImage image) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the image with the given ID already exists
            String checkImageExistsQuery = "SELECT * FROM Images WHERE id = ?";
            PreparedStatement checkImageExistsStmt = conn.prepareStatement(checkImageExistsQuery);
            checkImageExistsStmt.setString(1, image.getId());
            ResultSet resultSet = checkImageExistsStmt.executeQuery();

            if (resultSet.next()) {
                // Update the existing image
                String updateImageQuery = "UPDATE Images SET name = ?, size = ?, status = ? WHERE id = ?";
                PreparedStatement updateImageStmt = conn.prepareStatement(updateImageQuery);
                updateImageStmt.setString(1, image.getName());
                updateImageStmt.setLong(2, image.getSize());
                updateImageStmt.setString(3, image.getStatus());
                updateImageStmt.setString(4, image.getId());
                updateImageStmt.executeUpdate();
            } else {
                // Insert a new image
                String insertImageQuery = "INSERT INTO Images (id, name, size, status) VALUES (?, ?, ?, ?)";
                PreparedStatement insertImageStmt = conn.prepareStatement(insertImageQuery);
                insertImageStmt.setString(1, image.getId());
                insertImageStmt.setString(2, image.getName());
                insertImageStmt.setLong(3, image.getSize());
                insertImageStmt.setString(4, image.getStatus());
                insertImageStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
