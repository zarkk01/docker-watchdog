package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.ImageScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;

public class ImagesController implements Initializable {

    @FXML
    private TableView<ImageScene> imagesTableView;

    @FXML
    private CheckBox usedImagesCheckbox;

    @FXML
    private TableColumn<ImageScene,String> idColumn;
    @FXML
    private TableColumn<ImageScene,String> nameColumn;
    @FXML
    private TableColumn<ImageScene,String> statusColumn;
    @FXML
    private TableColumn<ImageScene,Long> sizeColumn;
    @FXML
    private TableColumn<ImageScene,Void> createContainerCollumn;

    private Stage stage;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            Callback<TableColumn<ImageScene, Void>, TableCell<ImageScene, Void>> startCellFactory = new Callback<>() {
                @Override
                public TableCell<ImageScene, Void> call(final TableColumn<ImageScene, Void> param) {
                    final TableCell<ImageScene, Void> cell = new TableCell<>() {
                        private final Button btn = new Button();
                        Image img = new Image(getClass().getResource("/images/play.png").toExternalForm());
                        ImageView view = new ImageView(img);

                        {
                            view.setFitHeight(20);
                            view.setPreserveRatio(true);
                            btn.setGraphic(view);
                            btn.setOnAction((ActionEvent event) -> {
                                ImageScene image = getTableView().getItems().get(getIndex());
                                try {
                                    createContainer(image);
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    };
                    return cell;
                }
            };

            createContainerCollumn.setCellFactory(startCellFactory);

            refreshImages();

            usedImagesCheckbox.setOnAction(event -> {
                refreshImages();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }

    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "volumesScene.fxml");
    }

    public void refreshImages() {
        try {
            List<ImageScene> images = getAllImages();
            imagesTableView.getItems().clear();
            for(ImageScene image : images) {
                if (usedImagesCheckbox.isSelected()) {
                    if (image.getStatus().equals("In use")) {
                        imagesTableView.getItems().add(image);
                    }
                } else {
                    imagesTableView.getItems().add(image);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ImageScene> getAllImages() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());
        List<ImageScene> images = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String status = jsonObject.getString("status");
            Long size = jsonObject.getLong("size");
            images.add(new ImageScene(id, name ,size, status));
        }
        return images;
    }

    public void createContainer(ImageScene image) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/images/create/" + image.getName()))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        image.setStatus("In use");
        refreshImages();
    }
}
