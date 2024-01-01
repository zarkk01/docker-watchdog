package gr.aueb.dmst.dockerWatchdog.Controllers;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KubernetesController implements Initializable {
    private Stage stage;
    private Parent root;
    @FXML
    private TextArea podsTextArea;
    @FXML
    private TextArea servicesTextArea;
    @FXML
    private TextArea deploymentsTextArea;
    @FXML
    private TextArea statefulSetsTextArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            AppsV1Api appsApi = new AppsV1Api();

            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null,null,null);
            StringBuilder podsData = new StringBuilder();
            for (V1Pod pod : list.getItems()) {
                podsData.append("Pod Name: ").append(pod.getMetadata().getName()).append("\n");
                podsData.append("Namespace: ").append(pod.getMetadata().getNamespace()).append("\n");
                podsData.append("Status: ").append(pod.getStatus().getPhase()).append("\n\n");
            }
            podsTextArea.setText(podsData.toString());

            V1ServiceList serviceList = api.listServiceForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            StringBuilder servicesData = new StringBuilder();
            for (V1Service service : serviceList.getItems()) {
                servicesData.append("Service Name: ").append(service.getMetadata().getName()).append("\n");
                servicesData.append("Namespace: ").append(service.getMetadata().getNamespace()).append("\n");
                servicesData.append("Type: ").append(service.getSpec().getType()).append("\n\n");
            }
            servicesTextArea.setText(servicesData.toString());

            V1DeploymentList deploymentList = appsApi.listDeploymentForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            StringBuilder deploymentsData = new StringBuilder();
            for (V1Deployment deployment : deploymentList.getItems()) {
                deploymentsData.append("Deployment Name: ").append(deployment.getMetadata().getName()).append("\n");
                deploymentsData.append("Namespace: ").append(deployment.getMetadata().getNamespace()).append("\n");
                deploymentsData.append("Replicas: ").append(deployment.getSpec().getReplicas()).append("\n\n");
            }
            deploymentsTextArea.setText(deploymentsData.toString());

            V1StatefulSetList statefulSetList = appsApi.listStatefulSetForAllNamespaces(null,null, null, null, null, null, null, null, null, null, null);
            StringBuilder statefulSetsData = new StringBuilder();
            for (V1StatefulSet statefulSet : statefulSetList.getItems()) {
                statefulSetsData.append("StatefulSet Name: ").append(statefulSet.getMetadata().getName()).append("\n");
                statefulSetsData.append("Namespace: ").append(statefulSet.getMetadata().getNamespace()).append("\n");
                statefulSetsData.append("Replicas: ").append(statefulSet.getSpec().getReplicas()).append("\n\n");
            }
            statefulSetsTextArea.setText(statefulSetsData.toString());
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