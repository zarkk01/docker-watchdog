//package gr.aueb.dmst.dockerWatchdog.Threads;
//
//import com.github.dockerjava.api.model.EventActor;
//import gr.aueb.dmst.dockerWatchdog.*;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.model.EventType;
//import com.github.dockerjava.core.DefaultDockerClientConfig;
//import com.github.dockerjava.core.DockerClientBuilder;
//import gr.aueb.dmst.dockerWatchdog.Main;
//import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
//import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
//import gr.aueb.dmst.dockerWatchdog.Threads.MonitorThread;
//import jdk.jfr.Event;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//
//import static javafx.beans.binding.Bindings.when;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MonitorThreadTest {
//
//    @BeforeEach
//    void setUp() {
//        DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .build();
//        DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();
//    }
//
//    @AfterEach
//    void tearDown() {}
//
//    @Test
//    void testHandleContainerEvent_Start() {
//        // Create a new instance for testing
//        MyInstance testInstance = new MyInstance("TestId", "TestContainer", "TestImage", "created",
//                0, 0, 0, 0, 0, "TestPorts", new ArrayList<>(), "TestIP", "TestGateway", 24);
//
//        // Create a new image for testing
//        MyImage testImage = new MyImage("TestImage", "TestId", 12345L, "Unused");
//
//        // Set up the test environment
//        // (You might need to set up other necessary dependencies or configurations)
//        MonitorThread monitorThread = new MonitorThread();
//
//
//        // Call the method you want to test
//        monitorThread.handleContainerEvent("start", testInstance.getId(), null);
//
//        // Verify the expected outcome
//        assertEquals("created", testInstance.getStatus());
//    }
//
//    // TODO: Add more test methods for other cases in handleContainerEvent and other methods
//
//    @Test
//    void testLiveMeasure() {
//        // Create a new instance for testing
//        MyInstance testInstance = new MyInstance("TestId", "TestContainer", "TestImage", "created",
//                0, 0, 0, 0, 0, "TestPorts", new ArrayList<>(), "TestIP", "TestGateway", 24);
//
//        // Set up the test environment
//        // (You might need to set up other necessary dependencies or configurations)
//        MonitorThread monitorThread = new MonitorThread();
//
//
//        // Call the method you want to test
//        monitorThread.liveMeasure();
//
//        // Verify the expected outcome or check for errors
//        // (The liveMeasure method may not produce a direct outcome visible in the test,
//        // so you might need to adapt this verification based on what you expect)
//    }
//
//    @Test
//    void testLiveMeasureForNewContainer() {
//        // Create a new instance for testing
//        MyInstance testInstance = new MyInstance("TestId", "TestContainer", "TestImage", "created",
//                0, 0, 0, 0, 0, "TestPorts", new ArrayList<>(), "TestIP", "TestGateway", 24);
//
//        // Set up the test environment
//        // (You might need to set up other necessary dependencies or configurations)
//        MonitorThread monitorThread = new MonitorThread();
//
//
//        // Call the method you want to test
//        monitorThread.liveMeasureForNewContainer(testInstance.getId());
//
//        // Verify the expected outcome or check for errors
//        // (The liveMeasureForNewContainer method may not produce a direct outcome visible in the test,
//        // so you might need to adapt this verification based on what you expect)
//    }
//}
//// TODO: Add more test methods as needed