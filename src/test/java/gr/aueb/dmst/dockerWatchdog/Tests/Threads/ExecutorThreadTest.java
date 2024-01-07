package gr.aueb.dmst.dockerWatchdog.Tests.Threads;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import gr.aueb.dmst.dockerWatchdog.Tests.Threads.TestDataProvider;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Threads.MonitorThread;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorThreadTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/docker_database";
    private static final String USER = "docker_db";
    private static final String PASS = "dockerW4tchd0g$";
    private DockerClient dockerClient;

    @BeforeEach
    void setUp() {
        DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                //          .withDockerHost("tcp://localhost:2375") // Use "tcp" for TCP connections
                .build();
        dockerClient = DockerClientBuilder.getInstance(builder).build();

        TestDataProvider.insertTestInstances();
        TestDataProvider.insertTestImages();
        TestDataProvider.insertTestMetrics();
        TestDataProvider.insertTestVolumes();
    }

    @AfterEach
    void tearDown() throws IOException {

        if (dockerClient != null) {
            dockerClient.close();
        }
    }

    @Test
    void testStartContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();

        // Run the startContainer method
        executorThread.startContainer(containerId);

        // Add assertions to check whether the container is started successfully
        assertTrue(checkIfContainerStarted(containerId));
    }
    @Test
    void testStopContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();

        // Run the stopContainer method
        executorThread.stopContainer(containerId);

        // Add an assertion to check whether the container is stopped successfully
        assertTrue(checkIfContainerStopped(containerId));
    }
    @Test
    void testRunContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();

        try {
            // Run the runContainer method
            executorThread.runContainer(containerId);

            // Add an assertion to check whether the container is running successfully
            assertTrue(checkIfContainerRunning(containerId));
        } catch (InterruptedException e) {
            // Handle the InterruptedException, or rethrow it as an unchecked exception
            fail("Exception occurred: " + e.getMessage());
        }
    }
    @Test
    void testRemoveContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();
        // Remove the runContainer method
        executorThread.removeContainer(containerId);

        // Add an assertion to check whether the container is running successfully
        assertTrue(checkIfContainerRemoved(containerId));
    }

    @Test
    void testPauseContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();
        // Remove the runContainer method
        executorThread.pauseContainer(containerId);

        // Add an assertion to check whether the container is running successfully
        assertTrue(checkIfContainerPaused(containerId));
    }

    @Test
    void testUnpauseContainer() {
        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Get the container ID from the database
        String containerId = TestDataProvider.getTestContainerId();
        // Remove the runContainer method
        executorThread.unpauseContainer(containerId);

        // Add an assertion to check whether the container is running successfully
        assertTrue(checkIfContainerUnpaused(containerId));
    }

    @Test
    void testRenameContainer() {
        // Create an instance of ExecutorThread
        ExecutorThread executorThread = new ExecutorThread();

        // Insert test data for container into the database
        TestDataProvider.insertTestInstances();

        // Get the container ID and original name from the database
        String containerId = TestDataProvider.getTestContainerId();
        String originalName = TestDataProvider.getTestContainerName();

        // Specify the new name for the container
        String newName = "newContainerName";

        // Run the renameContainer method
        executorThread.renameContainer(containerId, newName);

        // Add an assertion to check whether the container has been renamed successfully
        assertTrue(checkIfContainerRenamed(containerId, newName));
    }

    //Exception handling tests
    @Test
    void testContainerNotFoundStartContainer() {
        // Attempt to start a nonexistent container
        String nonExistentContainerId = "nonexistent_container_id";
        ExecutorThread executorThread = new ExecutorThread();

        // Ensure NotFoundException is handled appropriately
        assertThrows(NotFoundException.class, () -> executorThread.startContainer(nonExistentContainerId));
    }
    @Test
    void testRemoveNonexistentContainer() {
        // Attempt to remove a nonexistent container
        String nonExistentContainerId = "nonexistent_container_id";
        ExecutorThread executorThread = new ExecutorThread();

        // Ensure NotFoundException is handled appropriately
        assertThrows(NotFoundException.class, () -> executorThread.removeContainer(nonExistentContainerId));
    }
    @Test
    void testPauseNonexistentContainer() {
        // Attempt to pause a nonexistent container
        String nonExistentContainerId = "nonexistent_container_id";
        ExecutorThread executorThread = new ExecutorThread();

        // Ensure NotFoundException is handled appropriately
        assertThrows(NotFoundException.class, () -> executorThread.pauseContainer(nonExistentContainerId));
    }
    @Test
    void testUnpauseNonexistentContainer() {
        // Attempt to unpause a nonexistent container
        String nonExistentContainerId = "nonexistent_container_id";
        ExecutorThread executorThread = new ExecutorThread();

        // Ensure NotFoundException is handled appropriately
        assertThrows(NotFoundException.class, () -> executorThread.unpauseContainer(nonExistentContainerId));
    }
    @Test
    void testStopNonexistentContainer() {
        // Attempt to stop a nonexistent container
        String nonExistentContainerId = "nonexistent_container_id";
        ExecutorThread executorThread = new ExecutorThread();

        // Ensure NotFoundException is handled appropriately
        assertThrows(NotFoundException.class, () -> executorThread.stopContainer(nonExistentContainerId));
    }

    private boolean checkIfContainerStarted(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container is in the "running" state in the database
            String checkContainerStateQuery = "SELECT * FROM Instances WHERE id = ? AND status = 'running'";
            PreparedStatement checkContainerStateStmt = conn.prepareStatement(checkContainerStateQuery);
            checkContainerStateStmt.setString(1, containerId);
            return checkContainerStateStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean checkIfContainerStopped(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container is in the "stopped" state in the database
            String checkContainerStateQuery = "SELECT * FROM Containers WHERE id = ? AND state = 'stopped'";
            PreparedStatement checkContainerStateStmt = conn.prepareStatement(checkContainerStateQuery);
            checkContainerStateStmt.setString(1, containerId);
            return checkContainerStateStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean checkIfContainerRunning(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container is in the "running" state in the database
            String checkContainerStateQuery = "SELECT * FROM Containers WHERE id = ? AND state = 'running'";
            PreparedStatement checkContainerStateStmt = conn.prepareStatement(checkContainerStateQuery);
            checkContainerStateStmt.setString(1, containerId);
            return checkContainerStateStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkIfContainerRemoved(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container with the specified ID is not present in the database
            String checkContainerQuery = "SELECT * FROM Containers WHERE id = ?";
            PreparedStatement checkContainerStmt = conn.prepareStatement(checkContainerQuery);
            checkContainerStmt.setString(1, containerId);
            return !checkContainerStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Helper method to check if a container has been renamed
    private boolean checkIfContainerRenamed(String containerId, String newName) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container has the new name in the Instances table
            String checkContainerQuery = "SELECT * FROM Instances WHERE id = ? AND name = ?";
            PreparedStatement checkContainerStmt = conn.prepareStatement(checkContainerQuery);
            checkContainerStmt.setString(1, containerId);
            checkContainerStmt.setString(2, newName);
            return checkContainerStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkIfContainerPaused(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container is in the "paused" state in the database
            String checkContainerStateQuery = "SELECT * FROM Containers WHERE id = ? AND state = 'paused'";
            PreparedStatement checkContainerStateStmt = conn.prepareStatement(checkContainerStateQuery);
            checkContainerStateStmt.setString(1, containerId);
            return checkContainerStateStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkIfContainerUnpaused(String containerId) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Check if the container is not in the "paused" state in the database
            String checkContainerStateQuery = "SELECT * FROM Containers WHERE id = ? AND state != 'paused'";
            PreparedStatement checkContainerStateStmt = conn.prepareStatement(checkContainerStateQuery);
            checkContainerStateStmt.setString(1, containerId);
            return checkContainerStateStmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
