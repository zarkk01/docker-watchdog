package gr.aueb.dmst.dockerWatchdog.Controllers;

import gr.aueb.dmst.dockerWatchdog.Models.DeploymentScene;
import gr.aueb.dmst.dockerWatchdog.Models.PodScene;
import gr.aueb.dmst.dockerWatchdog.Models.ServiceScene;
import gr.aueb.dmst.dockerWatchdog.Models.StatefulSetScene;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KubernetesController implements Initializable {
    private Stage stage;
    private Parent root;
    @FXML
    private TableView<PodScene> podsTableView;
    @FXML
    private TableColumn<PodScene, String> podNameColumn;
    @FXML
    private TableColumn<PodScene, String> podNamespaceColumn;
    @FXML
    private TableColumn<PodScene, String> podStatusColumn;
    @FXML
    private TableView<DeploymentScene> deploymentsTableView;
    @FXML
    private TableColumn<DeploymentScene, String> deploymentNameColumn;
    @FXML
    private TableColumn<DeploymentScene, String> deploymentNamespaceColumn;

    @FXML
    private TableView<StatefulSetScene> statefulSetsTableView;
    @FXML
    private TableColumn<StatefulSetScene, String> statefulSetNameColumn;
    @FXML
    private TableColumn<StatefulSetScene, String> statefulSetNamespaceColumn;

    @FXML
    private TableView<ServiceScene> servicesTableView;
    @FXML
    private TableColumn<ServiceScene, String> serviceNameColumn;
    @FXML
    private TableColumn<ServiceScene, String> serviceNamespaceColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            AppsV1Api appsApi = new AppsV1Api();

            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null,null,null);
            ObservableList<PodScene> podsData = FXCollections.observableArrayList();
            for (V1Pod pod : list.getItems()) {
                podsData.add(new PodScene(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), pod.getStatus().getPhase()));
            }

            podNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            podNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));
            podStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            podsTableView.setItems(podsData);

            V1DeploymentList deploymentList = appsApi.listDeploymentForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            ObservableList<DeploymentScene> deploymentsData = FXCollections.observableArrayList();
            for (V1Deployment deployment : deploymentList.getItems()) {
                deploymentsData.add(new DeploymentScene(deployment.getMetadata().getName(), deployment.getMetadata().getNamespace()));
            }

            deploymentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            deploymentNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));

            deploymentsTableView.setItems(deploymentsData);

            V1StatefulSetList statefulSetList = appsApi.listStatefulSetForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            ObservableList<StatefulSetScene> statefulSetsData = FXCollections.observableArrayList();
            for (V1StatefulSet statefulSet : statefulSetList.getItems()) {
                statefulSetsData.add(new StatefulSetScene(statefulSet.getMetadata().getName(), statefulSet.getMetadata().getNamespace()));
            }

            statefulSetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            statefulSetNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));

            statefulSetsTableView.setItems(statefulSetsData);

            V1ServiceList serviceList = api.listServiceForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            ObservableList<ServiceScene> servicesData = FXCollections.observableArrayList();
            for (V1Service service : serviceList.getItems()) {
                servicesData.add(new ServiceScene(service.getMetadata().getName(), service.getMetadata().getNamespace()));
            }

            serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            serviceNamespaceColumn.setCellValueFactory(new PropertyValueFactory<>("namespace"));

            servicesTableView.setItems(servicesData);

            podsTableView.setPlaceholder(new Label("No Pods available"));
            deploymentsTableView.setPlaceholder(new Label("No Deployments available"));
            statefulSetsTableView.setPlaceholder(new Label("No StatefulSets available"));
            servicesTableView.setPlaceholder(new Label("No Services available"));
        } catch (ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes the scene to the specified FXML file.
     *
     * @param actionEvent The ActionEvent triggering the scene change.
     * @param fxmlFile    The name of the FXML file to load.
     * @throws IOException If an I/O error occurs during loading.
     */
    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/volumesScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();
        changeScene(actionEvent, "volumesScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }
}