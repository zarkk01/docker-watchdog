//package gr.aueb.dmst.dockerWatchdog.Controllers;
//
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import org.junit.jupiter.api.Test;
//import org.testfx.api.FxRobot;
//import org.testfx.framework.junit5.ApplicationTest;
//
//import static org.testfx.api.FxAssert.verifyThat;
//import static org.testfx.matcher.control.LabeledMatchers.hasText;
//import static org.testfx.matcher.control.ListViewMatchers.hasItems;
//import static org.testfx.matcher.control.TableViewMatchers.containsRow;
//import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
//
//public class GraphicsControllerTest extends ApplicationTest {
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        // Load the FXML file
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/graphicsScene.fxml"));
//        Parent root = loader.load();
//
//        // Set up the stage
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//
//    @Test
//    void testChartsUpdate(FxRobot robot) {
//        // Simulate user interactions and verify the results
//
//        // Click a button that triggers chart updates
//        robot.clickOn("#someButton");
//
//        // Verify that the CPU chart has been updated
//        verifyThat("#cpuChart", hasDataPoints());
//
//        // Verify that the PIDs chart has been updated
//        verifyThat("#pidsChart", hasDataPoints());
//
//        // Verify that the Memory chart has been updated
//        verifyThat("#memoryChart", hasDataPoints());
//    }
//
//    private static <T> Matcher<T> hasDataPoints() {
//        return new TypeSafeMatcher<T>() {
//            @Override
//            protected boolean matchesSafely(T item) {
//                // Check if the chart has data points
//                // Implement your logic to check for data points in the chart
//                return true; // Replace with your implementation
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("Chart should have data points");
//            }
//        };
//    }
//
//    @Test
//    void testButtonInteraction(FxRobot robot) {
//        // Simulate button interaction and verify the application's response
//
//        // Click a button that triggers some action
//        robot.clickOn("#someOtherButton");
//
//        // Verify that a label or component has been updated
//        verifyThat("#statusLabel", hasText("Action completed")); // Adjust based on your application
//    }
//
//    @Test
//    void testTableViewContent(FxRobot robot) {
//        // Verify the content of the TableView
//
//        // Assuming a TableView with ID "instancesTableView"
//        verifyThat("#instancesTableView", containsRow("someColumn1", "someColumn2")); // Replace with actual column values
//    }
//
//    @Test
//    void testListViewContent(FxRobot robot) {
//        // Verify the content of a ListView
//
//        // Assuming a ListView with ID "someListView"
//        verifyThat("#someListView", hasItems("item1", "item2", "item3")); // Replace with actual item values
//    }
//
//    @Test
//    void testTextFieldContent(FxRobot robot) {
//        // Verify the content of a TextField
//
//        // Assuming a TextField with ID "someTextField"
//        verifyThat("#someTextField", hasText("initialValue")); // Replace with actual initial value
//    }
//
//    @Test
//    void testComboBoxSelection(FxRobot robot) {
//        // Verify the selection of a ComboBox
//
//        // Assuming a ComboBox with ID "someComboBox"
//        robot.clickOn("#someComboBox").clickOn("Option1");
//        verifyThat("#someComboBox", hasText("Option1"));
//    }
//
//    @Test
//    void testCheckboxSelection(FxRobot robot) {
//        // Verify the selection of a Checkbox
//
//        // Assuming a Checkbox with ID "someCheckbox"
//        robot.clickOn("#someCheckbox");
//        verifyThat("#someCheckbox", isSelected());
//    }
//
//    // Add more tests based on your application's functionality...
//
//    @Test
//    void testCheckboxDeselection(FxRobot robot) {
//        // Verify the deselection of a Checkbox
//
//        // Assuming a Checkbox with ID "someCheckbox"
//        robot.clickOn("#someCheckbox"); // Select the checkbox
//        robot.clickOn("#someCheckbox"); // Deselect the checkbox
//        verifyThat("#someCheckbox", isSelected().negate()); // Ensure the checkbox is not selected
//    }
//
//    @Test
//    void testComboBoxSelectionChange(FxRobot robot) {
//        // Verify the change of selection in a ComboBox
//
//        // Assuming a ComboBox with ID "someComboBox"
//        robot.clickOn("#someComboBox").clickOn("Option1");
//        robot.clickOn("#someComboBox").clickOn("Option2");
//        verifyThat("#someComboBox", hasSelectedItem("Option2")); // Ensure Option2 is selected
//    }
//
//    @Test
//    void testListViewSelection(FxRobot robot) {
//        // Verify the selection of items in a ListView
//
//        // Assuming a ListView with ID "someListView"
//        robot.clickOn("#someListView").clickOn("item1", MouseButton.CONTROL_DOWN); // Select multiple items
//        verifyThat("#someListView", hasSelectedItems("item1"));
//    }
//
//    @Test
//    void testTableViewDoubleClick(FxRobot robot) {
//        // Verify the response to a double-click on a TableView row
//
//        // Assuming a TableView with ID "instancesTableView"
//        robot.clickOn("#instancesTableView").type(KeyCode.DOWN).type(KeyCode.ENTER);
//        // Verify that a specific action is triggered upon double-click
//        // For example, check if a new scene is loaded or a dialog is displayed
//    }
//
//    @Test
//    void testTextFieldEdit(FxRobot robot) {
//        // Verify the editing of a TextField
//
//        // Assuming a TextField with ID "someTextField"
//        robot.doubleClickOn("#someTextField").write("newText");
//        verifyThat("#someTextField", hasText("newText"));
//    }
//
//    @Test
//    void testComboBoxKeyboardNavigation(FxRobot robot) {
//        // Verify keyboard navigation in a ComboBox
//
//        // Assuming a ComboBox with ID "someComboBox"
//        robot.clickOn("#someComboBox").type(KeyCode.DOWN).type(KeyCode.ENTER);
//        verifyThat("#someComboBox", hasSelectedItem("Option1"));
//    }
//
//    @Test
//    void testTableViewSort(FxRobot robot) {
//        // Verify sorting of TableView columns
//
//        // Assuming a TableView with ID "instancesTableView"
//        robot.clickOn("#instancesTableView .columnHeader"); // Click on a column header to sort
//        // Verify that the data in the column is sorted in the correct order
//    }
//
//
//}
