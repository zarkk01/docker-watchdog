package gr.aueb.dmst.dockerWatchdog;

import java.sql.*;
import java.util.Date;

public class DatabaseThread implements Runnable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "root";
    private static final String PASS = "Zarko1213";

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
                    "metricid INT, " +
                    "FOREIGN KEY(metricid) REFERENCES Metrics(id), "+
                    "PRIMARY KEY(id,metricid))";
            PreparedStatement createInstancesStmt = conn.prepareStatement(createInstancesTable);
            createInstancesStmt.execute();

            // Iterate over the instances
            for (MyInstance instance : Main.myInstancesList) {
                // Insert or update the instance in the Instances table
                String upsertInstance = "INSERT INTO Instances (id, name, image, status, memoryusage, pids, cpuusage, blockI, blockO, metricid) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), image = VALUES(image), status = VALUES(status), " +
                        "memoryusage = VALUES(memoryusage), pids = VALUES(pids), cpuUsage = VALUES(cpuusage), blockI = VALUES(blockI), " +
                        "blockO = VALUES(blockO), metricid = VALUES(metricid)";
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
                upsertInstanceStmt.setInt(10, metricId);
                upsertInstanceStmt.executeUpdate();

            }
            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateLiveMetcrics() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            while (true){
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
                        "memoryusage = ?, pids = ?, cpuusage = ?, blockI = ?, blockO = ? WHERE metricid = ? && id = ?";
                PreparedStatement updateInstancesStmt = conn.prepareStatement(updateInstancesQuery);

                for (MyInstance instance : Main.myInstancesList) {
                    // Set parameters for the update query
                    updateInstancesStmt.setString(1, instance.getName());
                    updateInstancesStmt.setString(2, instance.getImage());
                    updateInstancesStmt.setString(3, instance.getStatus());
                    updateInstancesStmt.setDouble(4, instance.getMemoryUsage());
                    updateInstancesStmt.setLong(5, instance.getPids());
                    updateInstancesStmt.setDouble(6, instance.getCpuUsage());
                    updateInstancesStmt.setDouble(7, instance.getBlockI());
                    updateInstancesStmt.setDouble(8, instance.getBlockO());
                    updateInstancesStmt.setInt(9, latestMetricId);
                    updateInstancesStmt.setString(10, instance.getId());

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
}
