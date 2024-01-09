package gr.aueb.dmst.dockerWatchdog.Threads;

import java.sql.*;
import java.util.Date;

import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;

public class DatabaseThread implements Runnable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";
    //other password : dockerW4tchd0g$

    private static boolean firstTime = true;

    @Override
    public void run() {
       createAllTables();
       updateLiveMetrics();
    }

    public static void createAllTables(){
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            if (firstTime) {

                String dropInstancesTable = "DROP TABLE IF EXISTS Instances";
                PreparedStatement dropInstancesStmt = conn.prepareStatement(dropInstancesTable);
                dropInstancesStmt.execute();

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

            String createMetricsTable = "CREATE TABLE IF NOT EXISTS Metrics (" +
                    "id INT AUTO_INCREMENT, " +
                    "datetime TIMESTAMP, " +
                    "PRIMARY KEY(id))";
            PreparedStatement createMetricsStmt = conn.prepareStatement(createMetricsTable);
            createMetricsStmt.execute();

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
                    "subnet VARCHAR(255)," +
                    "gateway VARCHAR(255)," +
                    "prefixlen INT," +
                    "metricid INT, " +
                    "FOREIGN KEY(metricid) REFERENCES Metrics(id), " +
                    "PRIMARY KEY(id,metricid))";
            PreparedStatement createInstancesStmt = conn.prepareStatement(createInstancesTable);
            createInstancesStmt.execute();

            keepTrackOfInstances();

            String createImagesTable = "CREATE TABLE IF NOT EXISTS Images (" +
                    "id VARCHAR(255), " +
                    "name VARCHAR(255), " +
                    "status VARCHAR(255), " +
                    "size LONG, " +
                    "PRIMARY KEY(id))";
            PreparedStatement createImagesStmt = conn.prepareStatement(createImagesTable);
            createImagesStmt.execute();

            keepTrackOfImages();

            String createVolumesTable = "CREATE TABLE IF NOT EXISTS Volumes (" +
                    "name VARCHAR(255), " +
                    "driver VARCHAR(255), " +
                    "mountpoint VARCHAR(255), " +
                    "containerNamesUsing VARCHAR(750), " +
                    "PRIMARY KEY(name))";
            PreparedStatement createVolumesStmt = conn.prepareStatement(createVolumesTable);
            createVolumesStmt.execute();

            keepTrackOfVolumes();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void keepTrackOfInstances() throws SQLException {
        try{
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String insertMetric = "INSERT INTO Metrics (datetime) VALUES (?)";
            PreparedStatement insertMetricStmt = conn.prepareStatement(insertMetric, PreparedStatement.RETURN_GENERATED_KEYS);
            insertMetricStmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            insertMetricStmt.executeUpdate();

            ResultSet rs = insertMetricStmt.getGeneratedKeys();
            int metricId = 0;
            if (rs.next()) {
                metricId = rs.getInt(1);
            }

            for (MyInstance instance : Main.myInstances) {
                String volumesUsing = "";
                for (String volumeName : instance.getVolumes()) {
                    volumesUsing += volumeName + ",";
                }

                String upsertInstance = "INSERT INTO Instances (id, name, image, status, memoryusage, pids, cpuusage, blockI, blockO, volumes, subnet, gateway, prefixlen, metricid) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), image = VALUES(image), status = VALUES(status), " +
                        "memoryusage = VALUES(memoryusage), pids = VALUES(pids), cpuUsage = VALUES(cpuusage), blockI = VALUES(blockI), " +
                        "blockO = VALUES(blockO), volumes = VALUES(volumes),subnet = VALUES(subnet),gateway = VALUES(gateway),prefixlen = VALUES(prefixlen), metricid = VALUES(metricid)";
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
                upsertInstanceStmt.setString(11, instance.getSubnet());
                upsertInstanceStmt.setString(12, instance.getGateway());
                upsertInstanceStmt.setInt(13, instance.getPrefixLen());
                upsertInstanceStmt.setInt(14, metricId);
                upsertInstanceStmt.executeUpdate();
            }

            conn.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void keepTrackOfImages() throws SQLException {
        try{
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            for (MyImage image : Main.myImages) {
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

    public static void deleteImage(MyImage image) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String deleteImage = "DELETE FROM Images WHERE name = ?";
            PreparedStatement deleteImageStmt = conn.prepareStatement(deleteImage);
            deleteImageStmt.setString(1, image.getName());
            deleteImageStmt.executeUpdate();

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void keepTrackOfVolumes(){
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            for (MyVolume volume : Main.myVolumes) {
                String containerNamesUsing = "";
                for (String containerName : volume.getContainerNamesUsing()) {
                    containerNamesUsing += containerName + ",";
                }
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

            conn.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteVolume(MyVolume volume) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String deleteVolume = "DELETE FROM Volumes WHERE name = ?";
            PreparedStatement deleteVolumeStmt = conn.prepareStatement(deleteVolume);
            deleteVolumeStmt.setString(1, volume.getName());
            deleteVolumeStmt.executeUpdate();

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateLiveMetrics() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            while (true) {
                String findLatestMetricIdQuery = "SELECT MAX(id) AS latestMetricId FROM Metrics";
                Statement findLatestMetricIdStmt = conn.createStatement();
                ResultSet latestMetricIdResult = findLatestMetricIdStmt.executeQuery(findLatestMetricIdQuery);

                int latestMetricId = 0;
                if (latestMetricIdResult.next()) {
                    latestMetricId = latestMetricIdResult.getInt("latestMetricId");
                }

                String updateInstancesQuery = "UPDATE Instances SET name = ?, image = ?, status = ?, " +
                        "memoryusage = ?, pids = ?, cpuusage = ?, blockI = ?, blockO = ?,volumes = ?,subnet = ?,gateway = ?,prefixlen = ? WHERE metricid = ? && id = ?";
                PreparedStatement updateInstancesStmt = conn.prepareStatement(updateInstancesQuery);

                for (MyInstance instance : Main.myInstances) {
                    String volumesUsing = "";
                    for (String volumeName : instance.getVolumes()) {
                        volumesUsing += volumeName + ",";
                    }
                    updateInstancesStmt.setString(1, instance.getName());
                    updateInstancesStmt.setString(2, instance.getImage());
                    updateInstancesStmt.setString(3, instance.getStatus());
                    updateInstancesStmt.setDouble(4, instance.getMemoryUsage());
                    updateInstancesStmt.setLong(5, instance.getPids());
                    updateInstancesStmt.setDouble(6, instance.getCpuUsage());
                    updateInstancesStmt.setDouble(7, instance.getBlockI());
                    updateInstancesStmt.setDouble(8, instance.getBlockO());
                    updateInstancesStmt.setString(9, volumesUsing);
                    updateInstancesStmt.setString(10, instance.getSubnet());
                    updateInstancesStmt.setString(11, instance.getGateway());
                    updateInstancesStmt.setInt(12, instance.getPrefixLen());
                    updateInstancesStmt.setInt(13, latestMetricId);
                    updateInstancesStmt.setString(14, instance.getId());

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
}
