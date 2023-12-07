package gr.aueb.dmst.dockerWatchdog;

import java.sql.*;
import java.util.Date;
import java.util.List;

public class DatabaseThread implements Runnable {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String USER = "your_username";
    private static final String PASS = "your_password";

    private List<MyInstance> instances;

    public DatabaseThread(List<MyInstance> instances) {
        this.instances = instances;
    }

    @Override
    public void run() {
        try {
            // Establish a connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

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
                    "size BIGINT, " +
                    "memoryUsage BIGINT, " +
                    "pids BIGINT, " +
                    "cpuUsage DOUBLE, " +
                    "blockI DOUBLE, " +
                    "blockO DOUBLE, " +
                    "metricId INT, " +
                    "PRIMARY KEY(id), " +
                    "FOREIGN KEY(metricId) REFERENCES Metrics(id))";
            PreparedStatement createInstancesStmt = conn.prepareStatement(createInstancesTable);
            createInstancesStmt.execute();

            // Iterate over the instances
            for (MyInstance instance : instances) {
                // Insert or update the instance in the Instances table
                String upsertInstance = "INSERT INTO Instances (id, name, image, status, size, memoryUsage, pids, cpuUsage, blockI, blockO, metricId) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name), image = VALUES(image), status = VALUES(status), size = VALUES(size), " +
                        "memoryUsage = VALUES(memoryUsage), pids = VALUES(pids), cpuUsage = VALUES(cpuUsage), blockI = VALUES(blockI), " +
                        "blockO = VALUES(blockO), metricId = VALUES(metricId)";
                PreparedStatement upsertInstanceStmt = conn.prepareStatement(upsertInstance);
                upsertInstanceStmt.setString(1, instance.getId());
                upsertInstanceStmt.setString(2, instance.getName());
                upsertInstanceStmt.setString(3, instance.getImage());
                upsertInstanceStmt.setString(4, instance.getStatus());
                upsertInstanceStmt.setLong(5, instance.getSize());
                upsertInstanceStmt.setLong(6, instance.getMemoryUsage());
                upsertInstanceStmt.setLong(7, instance.getPids());
                upsertInstanceStmt.setDouble(8, instance.getCpuUsage());
                upsertInstanceStmt.setDouble(9, instance.getBlockI());
                upsertInstanceStmt.setDouble(10, instance.getBlockO());
                upsertInstanceStmt.setInt(11, metricId);
                upsertInstanceStmt.executeUpdate();
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}