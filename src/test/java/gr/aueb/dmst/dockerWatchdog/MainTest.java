//package gr.aueb.dmst.dockerWatchdog;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//public class MainTest {
//
//    @Test
//    public void testMainMethod() {
//
//        // Mock the behavior of SpringApplication.run()
//        Main.springContext = new MockConfigurableApplicationContext();
//
//        // Call the main method
//        Main.main(new String[]{});
//
//        // Verify that myInstancesList, myImagesList, myVolumesList are initialized
//        assertNotNull(Main.myInstancesList);
//        assertNotNull(Main.myImagesList);
//        assertNotNull(Main.myVolumesList);
//
//        // Verify that dockerClient is initialized
//        assertNotNull(Main.builder);
//        assertNotNull(Main.dockerClient);
//
//        // Verify that dbThread is initialized
//        assertNotNull(Main.dbThread);
//
//        // Add more assertions based on your specific requirements
//
//    }
//
//    // Mock ConfigurableApplicationContext to use in the test
//    static class MockConfigurableApplicationContext extends org.springframework.context.support.GenericApplicationContext {
//        @Override
//        public void close() {
//            // Do nothing to mock the behavior of the springContext
//        }
//    }
//}
