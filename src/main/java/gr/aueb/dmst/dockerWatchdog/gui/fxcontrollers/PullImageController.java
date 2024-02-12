package gr.aueb.dmst.dockerWatchdog.gui.fxcontrollers;

import gr.aueb.dmst.dockerWatchdog.api.services.ApiService;
import gr.aueb.dmst.dockerWatchdog.exceptions.ImageActionException;
import gr.aueb.dmst.dockerWatchdog.gui.models.ImageScene;
import gr.aueb.dmst.dockerWatchdog.gui.models.SearchResultScene;
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.gui.GuiApplication.client;

public class PullImageController implements Initializable {

    @FXML
    public TableColumn<ImageScene, String> imageNameColumn;
    @FXML
    public TableColumn<ImageScene, String> descriptionColumn;
    @FXML
    public TableColumn<ImageScene, String> pullCountColumn;
    @FXML
    public TableColumn<SearchResultScene, Void> pullImageColumn;
    @FXML
    public TableView<SearchResultScene> searchResultTable;


    private Stage stage;
    private Parent root;

    @FXML
    private TextField imagesSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        imageNameColumn.setCellValueFactory(new PropertyValueFactory<>("repoName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        pullCountColumn.setCellValueFactory(new PropertyValueFactory<>("pullCount"));

        // Set up the pullImageColumn
        pullImageColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SearchResultScene, Void> call(TableColumn<SearchResultScene, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Pull");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            SearchResultScene data = getTableView().getItems().get(getIndex());
                            try {
                                pullImage(data.getRepoName());
                            } catch (ImageActionException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setStyle("");
                        } else {
                            setGraphic(btn);
                            setStyle("-fx-background-color: #474745");
                        }
                    }
                };
            }
        });

        searchResultTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SearchResultScene item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("-fx-background-color: #474745;");
                } else {
                    setStyle("-fx-background-color: #474745;");

                }
            }
        });
        searchResultTable.setStyle("-fx-background-color: #474745; -fx-horizontal-grid-lines-visible: false; -fx-vertical-grid-lines-visible: false;");
        hideTableHeaders(searchResultTable);
        searchResultTable.setPlaceholder(new Label());
    }

    public void searchForImages() throws IOException {
        if (UserController.token == null) {
            System.out.print("You need to login first");
        } else {
            JSONArray searchResults = ApiService.searchImages(UserController.token, imagesSearch.getText());
            ObservableList<SearchResultScene> items = FXCollections.observableArrayList();
            for (int i = 0; i < searchResults.length(); i++) {
                JSONObject jsonObject = searchResults.getJSONObject(i);
                String repoName = jsonObject.getString("repo_name");
                String description = jsonObject.getString("short_description");
                int pullCount = jsonObject.getInt("pull_count");
                items.add(new SearchResultScene(repoName, description, pullCount));
            }
            searchResultTable.setItems(items);
        }
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/imagesScene.fxml"));

        // Get the current stage.
        stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();

        // Set the new scene as the root of the stage and display it.
        stage.getScene().setRoot(root);
        stage.show();
    }

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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ImageActionException("Error occurred while pulling image: " + e.getMessage(), imageName);
        }
    }

    public void hideTableHeaders(TableView<?> tableView) {
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            final TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            header.setVisible(false);
            header.setMaxHeight(0);
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setPadding(new Insets(0));
        });
    }
}