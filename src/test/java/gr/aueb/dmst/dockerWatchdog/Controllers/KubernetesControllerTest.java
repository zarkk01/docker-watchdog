//package gr.aueb.dmst.dockerWatchdog.Controllers;
//
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.scene.control.TableView;
//import javafx.stage.Stage;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.testfx.framework.junit.ApplicationTest;
//import org.testfx.matcher.base.NodeMatchers;
//import org.testfx.api.FxAssert;
//
//@RunWith(JUnitPlatform.class)
//public class KubernetesControllerTest extends ApplicationTest {
//
//    private KubernetesController controller;
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/kubernetesScene.fxml"));
//        Scene scene = new Scene(loader.load());
//        stage.setScene(scene);
//        stage.show();
//        stage.toFront();
//
//        controller = loader.getController();
//    }
//
//    @Test
//    public void testPodsTableView() {
//        TableView tableView = controller.podsTableView;
//        FxAssert.verifyThat(tableView, NodeMatchers.isNotNull());
//    }
//
//    @Test
//    public void testDeploymentsTableView() {
//        TableView tableView = controller.deploymentsTableView;
//        FxAssert.verifyThat(tableView, NodeMatchers.isNotNull());
//    }
//
//    @Test
//    public void testChangeScene() {
//        // Simulate a click on the button to change to the Containers scene
//        clickOn("#containersButton"); // Assuming you have a button with this ID
//        // Add verifications based on expected changes in the UI elements or navigation
//        // For example, you can check if the current scene root is the expected one
//        Scene currentScene = stage.getScene();
//        FxAssert.verifyThat(currentScene.getRoot(), NodeMatchers.hasChild("#containersSceneRoot")); // Assuming the root of the Containers scene has this ID
//    }
//
//}