package gr.aueb.dmst.dockerWatchdog.Threads;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.*;

import gr.aueb.dmst.dockerWatchdog.Exceptions.DatabaseOperationException;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This thread is responsible for handling database operations.
 * It implements Runnable to allow database operations to be performed in a separate thread.
 * It includes methods for creating tables, keeping track of instances, images, and volumes,
 * deleting images and volumes, and updating live metrics.
 */
public class DatabaseThread implements Runnable {

    // Logger instance used mainly for errors.
    private static final Logger logger = LogManager.getLogger(DatabaseThread.class);

    // Database connection details as environment variables from user.
    private final static String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = System.getenv("WATCHDOG_MYSQL_USERNAME");
    private static final String PASS = System.getenv("WATCHDOG_MYSQL_PASSWORD");

    /**
     * This method is responsible for creating Instances, Images and Volumes
     * tables in the database. Also, create Metrics (Changes) table and starts the updateLiveMetrics
     * method which is responsible for updating every 2.5 seconds the live data
     * of the containers.
     */
    @Override
    public void run() {
        try {
            updateLiveMetrics();
        } catch (DatabaseOperationException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * This method is responsible for creating the watchdog_database in the MySQL server.
     * First, it drops the database if it exists, and then it creates it again fresh.
     * It is called from Monitor Thread's fillLists() method after the initialization
     * of the lists, and it is the first thing happening regarding database operations.
     *
     * @throws DatabaseOperationException if an error occurs while creating the database.
     */
    public static void createDatabase() throws DatabaseOperationException {
        try {
            // Configure the connection to the database, first without specifying the database name.
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASS);

            // Drop the database if it exists because we want no old data in our monitoring.
            try {
                String dropDatabase = "DROP DATABASE IF EXISTS watchdog_database";
                PreparedStatement dropDatabaseStmt = conn.prepareStatement(dropDatabase);
                // Goodbye old watchdog_database
                dropDatabaseStmt.execute();
            } catch (Exception e) {
                throw new DatabaseOperationException("dropping database", "watchdog_database");
            }

            // Create the database named watchdog_database which will be used for the application.
            try {
                String createDatabase = "CREATE DATABASE IF NOT EXISTS watchdog_database";
                PreparedStatement createDatabaseStmt = conn.prepareStatement(createDatabase);
                // Database is created right here
                createDatabaseStmt.execute();
            } catch (Exception e) {
                throw new DatabaseOperationException("creating database", "watchdog_database");
            }

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method, called from Monitor Thread's fillLists() method after the initialization
     * of the lists, deletes any existing tables in our watchdog_database, so we start fresh clean our monitoring.
     * It creates, then, Instances, Images and Volumes tables in the database.
     * Also, create Metrics (Changes) table. After every creation, it calls
     * keepTrackOf...() method so the tables are filled with the appropriate data.
     *
     * @throws DatabaseOperationException if an error occurs while creating the tables.
     */
    public static void createAllTables() throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Clear existing data.
            try {
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
            } catch (Exception e) {
                throw new DatabaseOperationException("dropping tables in database",
                        "containers, images, volumes");
            }

            // Create Metrics (Changes) and Instances tables.
            try {
                String createMetricsTable = "CREATE TABLE IF NOT EXISTS Metrics ("
                        + "id INT AUTO_INCREMENT, "
                        + "datetime TIMESTAMP, "
                        + "PRIMARY KEY(id))";
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

                // Fill with instances data.
                keepTrackOfInstances();
            } catch (Exception e) {
                throw new DatabaseOperationException("creating instances table in database", "containers");
            }

            // Create Images table.
            try {
                String createImagesTable = "CREATE TABLE IF NOT EXISTS Images (" +
                        "id VARCHAR(255), " +
                        "name VARCHAR(255), " +
                        "status VARCHAR(255), " +
                        "size LONG, " +
                        "PRIMARY KEY(id))";
                PreparedStatement createImagesStmt = conn.prepareStatement(createImagesTable);
                createImagesStmt.execute();

                // Fill with images data.
                keepTrackOfImages();
            } catch (Exception e) {
                throw new DatabaseOperationException("creating images table in database", "images");
            }

            // Create Volumes table.
            try {
                String createVolumesTable = "CREATE TABLE IF NOT EXISTS Volumes (" +
                        "name VARCHAR(255), " +
                        "driver VARCHAR(255), " +
                        "mountpoint VARCHAR(255), " +
                        "containerNamesUsing VARCHAR(750), " +
                        "PRIMARY KEY(name))";
                PreparedStatement createVolumesStmt = conn.prepareStatement(createVolumesTable);
                createVolumesStmt.execute();

                // Fill with volumes data.
                keepTrackOfVolumes();
            } catch (Exception e) {
                throw new DatabaseOperationException("creating volumes table in database", "volumes");
            }

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method is the backbone of our monitoring regarding instances.
     * It inserts a new row in the Metrics (Changes) table and then inserts or updates
     * the Instances table with the appropriate data. It is called whenever a container event
     * occurs (e.g. a container is created, started, stopped, etc) and also in the start of our program.
     *
     * @throws DatabaseOperationException if an error occurs while keeping track of instances.
     */
    public static void keepTrackOfInstances() throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Insert a new row in the Metrics (Changes) table since a container event occurred.
            try {
                String insertMetric = "INSERT INTO Metrics (datetime) VALUES (?)";
                PreparedStatement insertMetricStmt = conn.prepareStatement(insertMetric, PreparedStatement.RETURN_GENERATED_KEYS);
                insertMetricStmt.setTimestamp(1, new Timestamp(new Date().getTime()));
                insertMetricStmt.executeUpdate();


                // Get the metricId so to assign it in the last instances column.
                ResultSet rs = insertMetricStmt.getGeneratedKeys();
                int metricId = 0;
                if (rs.next()) {
                    metricId = rs.getInt(1);
                }

                // Iterate through all the instances and insert or update the Instances table.
                for (MyInstance instance : Main.myInstances) {
                    // Volumes are in an ArrayList so we need to convert them to a String.
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
            } catch (Exception e) {
                throw new DatabaseOperationException("updating instances table in database", "containers");
            }

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method is responsible about monitoring images.
     * It inserts or updates the Images table with the appropriate data.
     * It is called whenever an image is pulled or a container is deleted because it ,probably,
     * should update the status of an image to "unused".
     *
     * @throws DatabaseOperationException if an error occurs while keeping track of images.
     */
    public static void keepTrackOfImages() throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Iterate through all the images and insert or update the Images table.
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

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method is responsible for deleting an image from the database.
     * It is called when an image is deleted from Docker, and it removes the corresponding entry from the Images table in the database.
     *
     * @param image the MyImage object representing the image to be deleted
     * @throws DatabaseOperationException if an error occurs while deleting the image from the database
     */
    public static void deleteImage(MyImage image) throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Prepare the SQL statement to delete the image.
            String deleteImage = "DELETE FROM Images WHERE name = ?";
            PreparedStatement deleteImageStmt = conn.prepareStatement(deleteImage);
            deleteImageStmt.setString(1, image.getName());

            // Say goodbye to the image.
            deleteImageStmt.executeUpdate();

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method is responsible for monitoring volumes.
     * It is called whenever a volume is created or a container is deleted because it ,probably,
     * should update the containerNamesUsing column of a volume.
     *
     * @throws DatabaseOperationException if an error occurs while keeping track of volumes.
     */
    public static void keepTrackOfVolumes() throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Iterate through all the volumes and insert or update the Volumes table.
            for (MyVolume volume : Main.myVolumes) {
                // Container names are in an ArrayList, we need to convert them to a String.
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

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection and you have the right" +
                    " username and the right password");
        }
    }

    /**
     * This method deletes a volume from the database.
     * It is called when a volume is deleted from Docker, and it removes the corresponding entry from the Volumes table in the database.
     *
     * @param volume the MyVolume object representing the volume to be deleted
     * @throws DatabaseOperationException if an error occurs while deleting the volume from the database
     */
    public static void deleteVolume(MyVolume volume) throws DatabaseOperationException {
        try {
            // Configure the connection to the database.
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Prepare the SQL statement to delete the volume.
            String deleteVolume = "DELETE FROM Volumes WHERE name = ?";
            PreparedStatement deleteVolumeStmt = conn.prepareStatement(deleteVolume);
            deleteVolumeStmt.setString(1, volume.getName());

            // RIP volume.
            deleteVolumeStmt.executeUpdate();

            // Close the connection to the database.
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseOperationException("connecting in database", "mySQL connection");
        }
    }

    /**
     * This method is responsible for updating live metrics of instances in the database.
     * It uses a Timer to schedule the update task to run every 2.5 seconds.
     * It updates the Instances table with the current metrics of instances.
     *
     * @throws DatabaseOperationException if an error occurs while updating live metrics.
     */
    public static void updateLiveMetrics() throws DatabaseOperationException {
        Timer timer = new Timer();
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    // Configure the connection to the database.
                    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

                    // Get the latest metric ID.
                    String findLatestMetricIdQuery = "SELECT MAX(id) AS latestMetricId FROM Metrics";
                    Statement findLatestMetricIdStmt = conn.createStatement();
                    ResultSet latestMetricIdResult = findLatestMetricIdStmt.executeQuery(findLatestMetricIdQuery);

                    int latestMetricId = 0;
                    if (latestMetricIdResult.next()) {
                        latestMetricId = latestMetricIdResult.getInt("latestMetricId");
                    }

                    // Prepare the SQL statement to update the instances.
                    String updateInstancesQuery = "UPDATE Instances SET name = ?, image = ?, status = ?, " +
                            "memoryusage = ?, pids = ?, cpuusage = ?, blockI = ?, blockO = ?,volumes = ?,subnet = ?,gateway = ?,prefixlen = ? WHERE metricid = ? && id = ?";
                    PreparedStatement updateInstancesStmt = conn.prepareStatement(updateInstancesQuery);

                    // Iterate through all the instances and update the Instances table.
                    for (MyInstance instance : Main.myInstances) {
                        // Volumes are in an ArrayList, we need to convert them to a String.
                        String volumesUsing = "";
                        for (String volumeName : instance.getVolumes()) {
                            volumesUsing += volumeName + ",";
                        }

                        // Set the parameters in the SQL statement.
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

                        // Execute the SQL statement.
                        updateInstancesStmt.executeUpdate();
                    }

                    // Close the connection to the database.
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error while updating live metrics in database: " + e.getMessage());
                }
            }
        };

        // Schedule the task to run every 2.5 seconds
        timer.scheduleAtFixedRate(updateTask, 0, 2500);
    }
}
