package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import gr.aueb.dmst.dockerWatchdog.exceptions.ImageActionException;
import gr.aueb.dmst.dockerWatchdog.gui.models.ImageScene;
import gr.aueb.dmst.dockerWatchdog.gui.models.SearchResultScene;
import static gr.aueb.dmst.dockerWatchdog.gui.GuiApplication.client;


/**
 * PullImageController is a JavaFX controller class that handles user interactions with the Pull Image scene.
 * This includes searching for Docker images, pulling an image, and navigating to other scenes.
 * It uses the ApiService to interact with Docker Hub and the WATCHDOG REST API.
 * It also displays the search results in a table view.
 */
public class PullImageController implements Initializable {
    private Stage stage;
    private Parent root;

    // The table view that will host the search results.
    @FXML
    public TableColumn<ImageScene, String> imageNameColumn;
    @FXML
    public TableColumn<ImageScene, String> descriptionColumn;
    @FXML
    public TableColumn<SearchResultScene, Void> pullImageColumn;
    @FXML
    public TableView<SearchResultScene> searchResultTable;

    @FXML
    private TextField imagesSearch;

    /**
     * Initializes the PullImageController.
     * This method is called after the FXML file has been loaded.
     * It sets up the table columns and the pull button for each row in the table.
     * It also sets the style of the table and hides the table headers.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the cell value factories for the imageNameColumn and descriptionColumn
        imageNameColumn.setCellValueFactory(new PropertyValueFactory<>("repoName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set up the pullImageColumn with a custom cell factory
        pullImageColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SearchResultScene, Void> call(TableColumn<SearchResultScene, Void> param) {
                return new TableCell<>() {
                    // Create a new button for the pull action
                    private final Button btn = new Button("Pull");

                    {
                        // Set the action for the pull button
                        btn.setOnAction((ActionEvent event) -> {
                            SearchResultScene data = getTableView().getItems().get(getIndex());
                            try {
                                // Pull the image when the button is clicked
                                pullImage(data.getRepoName());
                            } catch (ImageActionException e) {
                                // If an error occurs, print the error message
                                e.printStackTrace();
                            }
                        });

                        // Set the style for the pull button when the mouse enters and exits
                        btn.setOnMouseEntered(e -> btn.setStyle("-fx-text-fill: #F14246;"));
                        btn.setOnMouseExited(e -> btn.setStyle("-fx-text-fill: white;"));
                    }

                    // Update the item in the cell so that the pull button is displayed
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            // If the cell is empty, don't display anything
                            setGraphic(null);
                            setStyle("");
                        } else {
                            // If the cell is not empty, display the pull button
                            setGraphic(btn);
                            setStyle("-fx-background-color: #474745");
                        }
                    }
                };
            }
        });

        // Set the row factory for the searchResultTable so all have the same colors
        searchResultTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SearchResultScene item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    // If the row is empty, don't display anything
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // If the row is not empty, display the item
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // Set the style for the searchResultTable
        searchResultTable.setStyle("-fx-background-color: #474745; -fx-horizontal-grid-lines-visible: false; -fx-vertical-grid-lines-visible: false;");

        // Hide the table headers
        hideTableHeaders(searchResultTable);

        // Set the placeholder for the searchResultTable to empty, we want no text
        searchResultTable.setPlaceholder(new Label());
    }

    /**
     * Hides the headers of a TableView.
     * This method adds a listener to the skin property of the TableView.
     * When the skin changes, it looks up the TableHeaderRow, makes it invisible, and sets its height to 0.
     * This effectively hides the headers of the TableView.
     *
     * @param tableView The TableView whose headers should be hidden.
     */
    public void hideTableHeaders(TableView<?> tableView) {
        // Add a listener to the skin property of the TableView
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            // Look up the TableHeaderRow
            final TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");

            // Make the TableHeaderRow invisible and set its height to 0
            header.setVisible(false);
            header.setMaxHeight(0);
            header.setMinHeight(0);
            header.setPrefHeight(0);

            // Set the padding of the TableHeaderRow to 0
            header.setPadding(new Insets(0));
        });
    }

    /**
     * Searches for Docker images based on the text entered in the imagesSearch text field.
     * This method sends a request to Docker Hub to search for images.
     * If the user is not logged in, it prints a message asking the user to log in.
     * If the user is logged in, it retrieves the search results, creates SearchResultScene objects for each result,
     * and adds them to the searchResultTable.
     *
     * @throws IOException If an error occurs while sending the request to Docker Hub.
     */
    public void searchForImages() throws IOException {
        // Check if the user is logged in
        if (UserController.token != null) {
            // Send a request to Docker Hub to search for images
            JSONArray searchResults = ApiService.searchImages(UserController.token, imagesSearch.getText());

            // Create an ObservableList to hold the search results
            ObservableList<SearchResultScene> items = FXCollections.observableArrayList();

            // Loop through the search results
            for (int i = 0; i < searchResults.length(); i++) {
                // Get the JSON object for the current search result
                JSONObject jsonObject = searchResults.getJSONObject(i);

                // Get the repo name and description from the JSON object
                String repoName = jsonObject.getString("repo_name");
                String description = jsonObject.getString("short_description");

                // Create a new SearchResultScene object and add it to the ObservableList
                items.add(new SearchResultScene(repoName, description));
            }
            // Set the items in the searchResultTable to the ObservableList
            searchResultTable.setItems(items);
        }
    }

    /**
     * Pulls a Docker image from Docker Hub.
     * This method sends a POST request to the WATCHDOG REST API to pull the specified image.
     * If the image name does not contain a tag, it adds the "latest" tag.
     * If an error occurs while pulling the image, it throws an ImageActionException.
     *
     * @param imageName The name of the Docker image to pull. This can include the tag.
     * @throws ImageActionException If an error occurs while pulling the image.
     */
    public void pullImage(String imageName) throws ImageActionException {
        try {
            // If the user has not given a tag, add the latest tag.
            if (!imageName.contains(":")) {
                imageName += ":latest";
            }

            // Create a new HttpRequest that sends a POST request to the WATCHDOG REST API to pull the image.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/images/" + "pull/" + imageName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send the request and get the response
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // If an error occurs, throw an ImageActionException with the error message and the image name
            throw new ImageActionException("Error occurred while pulling image: " + e.getMessage(), imageName);
        }
    }

    /**
     * Changes the current scene to the Images scene.
     * This method loads the FXML file for the Images scene, sets it as the root of the current stage, and displays the new scene.
     * It is used to navigate from the current scene to the Images scene.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        // Load the FXML file for the Images scene
        root = FXMLLoader.load(getClass().getResource("/imagesScene.fxml"));

        // Get the current stage
        stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();

        // Set the Images scene as the root of the stage and display it
        stage.getScene().setRoot(root);
        stage.show();
    }

    /**
     * Changes the current scene to the User scene.
     * This method loads the FXML file for the User scene, sets it as the root of the current stage, and displays the new scene.
     * It also initializes the User scene by calling the onUserSceneLoad method of the UserController.
     * If an error occurs while loading the FXML file, it throws a RuntimeException.
     *
     * @param actionEvent The event that triggered the scene change.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void changeToUserScene(ActionEvent actionEvent) throws IOException {
        // Create a new FXMLLoader and load the FXML file for the User scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            // If an error occurs while loading the FXML file, throw a RuntimeException
            throw new RuntimeException(e);
        }

        // Get the UserController from the FXMLLoader
        UserController userController = loader.getController();

        // Initialize the User scene by calling the onUserSceneLoad method of the UserController
        userController.onUserSceneLoad("containersScene.fxml");

        // Get the current stage
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        // Set the User scene as the root of the stage and display it
        stage.getScene().setRoot(root);
        stage.show();
    }
}