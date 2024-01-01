package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Threads.MonitorThread;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import javafx.application.Application;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    // Initiate myInstancesList and myImagesList
    public static ArrayList<MyInstance> myInstancesList = new ArrayList<>();
    public static ArrayList<MyImage> myImagesList = new ArrayList<>();
    public static ArrayList<MyVolume> myVolumesList = new ArrayList<>();

    // Initiate dockerClient
    public static DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
            //          .withDockerHost("tcp://localhost:2375") // Use "tcp" for TCP connections
            .build();
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

    //Initiate dbThread
    public static Thread dbThread = new Thread(new DatabaseThread());

    public static void main(String[] args) {

        try {

            listPods();

            // Initiate and start newMonitorThread
            MonitorThread newDockerMonitor = new MonitorThread();
            Thread newMonitorThread = new Thread(newDockerMonitor);
            newMonitorThread.start();

            // Initiate and start executorThread
            ExecutorThread dockerExecutor = new ExecutorThread();
            Thread executorThread = new Thread(dockerExecutor);
            executorThread.start();

            // start dbThread
            dbThread.start();

            new Thread(DatabaseThread::updateLiveMetcrics).start();

            Application.launch(DesktopApp.class, args);

        } catch (Exception e) {
            // Handle exceptions here
            e.printStackTrace();
        }
    }

    public static void listPods() {
        try {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null,null,null);
            list.getItems().forEach(pod -> System.out.println(pod.getMetadata().getName()));
        } catch (ApiException | IOException e) {
            e.printStackTrace();
        }
    }
}
