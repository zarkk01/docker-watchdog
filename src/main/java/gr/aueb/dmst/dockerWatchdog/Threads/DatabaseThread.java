package gr.aueb.dmst.dockerWatchdog.Threads;

import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.MyImage;
import gr.aueb.dmst.dockerWatchdog.MyInstance;
import gr.aueb.dmst.dockerWatchdog.MyVolume;

import java.sql.*;
import java.util.Date;

public class DatabaseThread implements Runnable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";
    //other password : dockerW4tchd0g$

    private static boolean firstTime = true;

    @Override
    public void run() {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            if (firstTime) {

                // Drop the Instances table if it exists
                String dropInstancesTable = "DROP TABLE IF EXISTS Instances";
                PreparedStatement dropInstancesStmt = conn.prepareStatement(dropInstancesTable);
                dropInstancesStmt.execute();

                // Drop the Metrics table if it exists
                String dropMetricsTable = "DROP TABLE IF EXISTS Metrics";
                PreparedStatement dropMetricsStmt = conn.prepareStatement(dropMetricsTable);
                dropMetricsStmt.execute();

                String dropImagesTable = "DROP TABLE IF EXISTS Images";
                PreparedStatement dropImagesStmt = conn.prepareStatement(dropImagesTable);
                dropImagesStmt.execute();

                String dropVolumesTable = "DROP TABLE IF EXISTS Volumes";
                PreparedStatement dropVolumesStmt = conn.prepareStatement(dropVolumesTable);
                dropVolumesStmt.execute();

                firstTime = false;
            }

            // Create the metrics table if it doesn't exist
            String createMetricsTable = "CREATE TABLE IF NOT EXISTS Metrics (" +
                    "id INT AUTO_INCREMENT, " +
                    "datetime TIMESTAMP, " +
                    "PRIMARY KEY(id))";
            PreparedStatement createMetricsStmt = conn.prepareStatement(createMetricsTable);
            createMetricsStmt.execute();

            // Insert a new metric into the Metrics table
            String insertMetric = "INSERT INTO Metrics (datetime) VALUES (?)";
            PreparedStatement insertMetricStmt = conn.prepareStatement(insertMetric, PreparedStatement.RETURN_GENERATED_KEYS);
            insertMetricStmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            insertMetricStmt.executeUpdate();

            // Get the ID of the inserted metric
            ResultSet rs = insertMetricStmt.getGeneratedKeys();
            int metricId = 0;
            if (rs.next()) {
                metricId = rs.getInt(1);
            }

            // Create the Instances table if it doesn't exist
            String createInstancesTable = "CREATE TABLE IF NOT EXISTS Instances (" +
                    "id VARCHAR(255), " +
                    "name VARCHAR(255), " +
                    "image VARCHAR(255), " +
                    "status VARCHAR(255), " +
                    "memoryusage BIGINT, " +
                    "pids BIGINT, " +
                    "cpuusage DOUBLE, " +
                    "blockI DOUBLE, " +
                    "blockO DOUBLE, " +
                    "volumes VARCHAR(750)," +
                    "metricid INT, " +
                    "FOREIGN KEY(metricid) REFERENCES Metrics(id), " +
                    "PRIMARY KEY(id,metricid))";
            PreparedStatement createInstancesStmt = conn.prepareStatement(createInstancesTable);
            createInstancesStmt.execute();

            // Iterate over the instances
            for (MyInstance instance : Main.myInstancesList) {
                String volumesUsing = "";
                for (String volumeName : instance.getVolumes()) {
                    volumesUsing += volumeName + ",";
                }
                // Insert or update the instance in the Instances table
                String upsertInstance = "INSERT INTO Instances (id, name, image, status, memoryusage, pids, cpuusage, blockI, blockO, volumes, metricid) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), image = VALUES(image), status = VALUES(status), " +
                        "memoryusage = VALUES(memoryusage), pids = VALUES(pids), cpuUsage = VALUES(cpuusage), blockI = VALUES(blockI), " +
                        "blockO = VALUES(blockO), volumes = VALUES(volumes), metricid = VALUES(metricid)";
                PreparedStatement upsertInstanceStmt = conn.prepareStatement(upsertInstance);
                upsertInstanceStmt.setString(1, instance.getId());
                upsertInstanceStmt.setString(2, instance.getName());
                upsertInstanceStmt.setString(3, instance.getImage());
                upsertInstanceStmt.setString(4, instance.getStatus());
                upsertInstanceStmt.setLong(5, instance.getMemoryUsage());
                upsertInstanceStmt.setLong(6, instance.getPids());
                upsertInstanceStmt.setDouble(7, instance.getCpuUsage());
                upsertInstanceStmt.setDouble(8, instance.getBlockI());
                upsertInstanceStmt.setDouble(9, instance.getBlockO());
                upsertInstanceStmt.setString(10, volumesUsing);
                upsertInstanceStmt.setInt(11, metricId);
                upsertInstanceStmt.executeUpdate();
            }
            // Create the Images table if it doesn't exist
            String createImagesTable = "CREATE TABLE IF NOT EXISTS Images (" +
                    "id VARCHAR(255), " +
                    "name VARCHAR(255), " +
                    "status VARCHAR(255), " +
                    "size LONG, " +
                    "PRIMARY KEY(id))";
            PreparedStatement createImagesStmt = conn.prepareStatement(createImagesTable);
            createImagesStmt.execute();

            // Iterate over the images
            for (MyImage image : Main.myImagesList) {
                // Insert or update the images in the images table
                String upsertImage = "INSERT INTO Images (id, name, size, status) " +
                        "VALUES (?, ?, ?,?) " +
                        "ON DUPLICATE KEY UPDATE id = VALUES(id),name = VALUES(name), size = VALUES(size), " +
                        "status = VALUES(status)";
                PreparedStatement upsertImageStmt = conn.prepareStatement(upsertImage);
                upsertImageStmt.setString(1, image.getId());
                upsertImageStmt.setString(2, image.getName());
                upsertImageStmt.setLong(3, image.getSize());
                upsertImageStmt.setString(4, image.getStatus());
                upsertImageStmt.executeUpdate();
            }

            // Create the Volumes table if it doesn't exist
            String createVolumesTable = "CREATE TABLE IF NOT EXISTS Volumes (" +
                    "name VARCHAR(255), " +
                    "driver VARCHAR(255), " +
                    "mountpoint VARCHAR(255), " +
                    "containerNamesUsing VARCHAR(750), " +
                    "PRIMARY KEY(name))";
            PreparedStatement createVolumesStmt = conn.prepareStatement(createVolumesTable);
            createVolumesStmt.execute();
            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateLiveMetcrics() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            while (true) {
                // Find the largest metricId from the Metrics table
                String findLatestMetricIdQuery = "SELECT MAX(id) AS latestMetricId FROM Metrics";
                Statement findLatestMetricIdStmt = conn.createStatement();
                ResultSet latestMetricIdResult = findLatestMetricIdStmt.executeQuery(findLatestMetricIdQuery);

                int latestMetricId = 0;

                if (latestMetricIdResult.next()) {
                    latestMetricId = latestMetricIdResult.getInt("latestMetricId");
                }

                // Update instances with the latest metricId
                String updateInstancesQuery = "UPDATE Instances SET name = ?, image = ?, status = ?, " +
                        "memoryusage = ?, pids = ?, cpuusage = ?, blockI = ?, blockO = ?,volumes = ? WHERE metricid = ? && id = ?";
                PreparedStatement updateInstancesStmt = conn.prepareStatement(updateInstancesQuery);

                for (MyInstance instance : Main.myInstancesList) {
                    String volumesUsing = "";
                    for (String volumeName : instance.getVolumes()) {
                        volumesUsing += volumeName + ",";
                    }
                    // Set parameters for the update query
                    updateInstancesStmt.setString(1, instance.getName());
                    updateInstancesStmt.setString(2, instance.getImage());
                    updateInstancesStmt.setString(3, instance.getStatus());
                    updateInstancesStmt.setDouble(4, instance.getMemoryUsage());
                    updateInstancesStmt.setLong(5, instance.getPids());
                    updateInstancesStmt.setDouble(6, instance.getCpuUsage());
                    updateInstancesStmt.setDouble(7, instance.getBlockI());
                    updateInstancesStmt.setDouble(8, instance.getBlockO());
                    updateInstancesStmt.setString(9, volumesUsing);
                    updateInstancesStmt.setInt(10, latestMetricId);
                    updateInstancesStmt.setString(11, instance.getId());

                    // Execute the update query
                    updateInstancesStmt.executeUpdate();
                }
                Thread.sleep(2000);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public static void keepTrackOfImages() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        try{
            for (MyImage image : Main.myImagesList) {
                // Insert or update the images in the images table
                String upsertImage = "INSERT INTO Images (id, name, size, status) " +
                        "VALUES (?, ?, ?,?) " +
                        "ON DUPLICATE KEY UPDATE id = VALUES(id),name = VALUES(name), size = VALUES(size), " +
                        "status = VALUES(status)";
                PreparedStatement upsertImageStmt = conn.prepareStatement(upsertImage);
                upsertImageStmt.setString(1, image.getId());
                upsertImageStmt.setString(2, image.getName());
                upsertImageStmt.setLong(3, image.getSize());
                upsertImageStmt.setString(4, image.getStatus());
                upsertImageStmt.executeUpdate();
            }
            conn.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void addImage(MyImage image) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert or update the image in the Images table
            String upsertImage = "INSERT INTO Images (id, name, size, status) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name), size = VALUES(size), " +
                    "status = VALUES(status)";
            PreparedStatement upsertImageStmt = conn.prepareStatement(upsertImage);
            upsertImageStmt.setString(1, image.getId());
            upsertImageStmt.setString(2, image.getName());
            upsertImageStmt.setLong(3, image.getSize());
            upsertImageStmt.setString(4, image.getStatus());
            upsertImageStmt.executeUpdate();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteImage(MyImage image) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Delete the image from the Images table
            String deleteImage = "DELETE FROM Images WHERE name = ?";
            PreparedStatement deleteImageStmt = conn.prepareStatement(deleteImage);
            deleteImageStmt.setString(1, image.getName());
            deleteImageStmt.executeUpdate();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addVolume(MyVolume volume) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String containerNamesUsing = "";
            for (String containerName : volume.getContainerNamesUsing()) {
                containerNamesUsing += containerName + ",";
            }

            System.out.println("containerNamesUsing: " + containerNamesUsing);

            // Insert or update the volume in the Volumes table
            String upsertVolume = "INSERT INTO Volumes (name, driver, mountpoint, containerNamesUsing) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE driver = VALUES(driver), mountpoint = VALUES(mountpoint), " +
                    "containerNamesUsing = VALUES(containerNamesUsing)";
            PreparedStatement upsertVolumeStmt = conn.prepareStatement(upsertVolume);
            upsertVolumeStmt.setString(1, volume.getName());
            upsertVolumeStmt.setString(2, volume.getDriver());
            upsertVolumeStmt.setString(3, volume.getMountpoint());
            upsertVolumeStmt.setString(4, containerNamesUsing);
            upsertVolumeStmt.executeUpdate();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteVolume(MyVolume volume) {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Delete the volume from the Volumes table
            String deleteVolume = "DELETE FROM Volumes WHERE name = ?";
            PreparedStatement deleteVolumeStmt = conn.prepareStatement(deleteVolume);
            deleteVolumeStmt.setString(1, volume.getName());
            deleteVolumeStmt.executeUpdate();

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void keepTrackOfVolumes(){
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Iterate over the volumes
            for (MyVolume volume : Main.myVolumesList) {

                String containerNamesUsing = "";
                for (String containerName : volume.getContainerNamesUsing()) {
                    containerNamesUsing += containerName + ",";
                }

                // Insert or update the images in the images table
                String upsertVolume = "INSERT INTO Volumes (name, driver, mountpoint, containerNamesUsing) " +
                        "VALUES (?, ?, ?,?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name),driver = VALUES(driver), mountpoint = VALUES(mountpoint), " +
                        "containerNamesUsing = VALUES(containerNamesUsing)";
                PreparedStatement upsertVolumeStmt = conn.prepareStatement(upsertVolume);
                upsertVolumeStmt.setString(1, volume.getName());
                upsertVolumeStmt.setString(2, volume.getDriver());
                upsertVolumeStmt.setString(3, volume.getMountpoint());
                upsertVolumeStmt.setString(4, containerNamesUsing);
                upsertVolumeStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
