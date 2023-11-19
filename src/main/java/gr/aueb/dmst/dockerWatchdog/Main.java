package gr.aueb.dmst.dockerWatchdog;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util. List;
public class Main
{
    public static void main( String[] args )
    {
        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
        // otherwise it clusters the console with unwanted info

        // Just for debugging reason.
        System.out.println( "Starting " );

        //Config builder and create dockerClient
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

        //No need for them.
        //builder.withDockerHost("tcp://Localhost:2375");
        //builder.withDockerCertPath("/Users/zark/.docker");
        //dockerClient.versionCmd().exec();

        List<Container> containers;
        containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        int totalContainers = 0;
        int totalImages = 0;
        int runningContainers = 0;
        int stoppedContainers = 0;

        // Print container information
        for (Container container : containers) {

            System.out.println("Container ID: " + container.getId());
            System.out.println("Container Name: " + container.getNames()[0]);
            System.out.println("Container Image: " + container.getImage());
            System.out.println("Container Status: " + container.getStatus());
            System.out.println("---------------");
        }
    }
}
