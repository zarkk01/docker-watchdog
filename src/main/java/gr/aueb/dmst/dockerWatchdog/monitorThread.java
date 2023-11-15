package gr.aueb.dmst.dockerWatchdog;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util. List;

public class monitorThread implements Runnable {
    @Override
    public void run(){
        System.out.println("Monitor thread is running");
    }
}
