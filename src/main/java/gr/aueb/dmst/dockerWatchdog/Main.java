package gr.aueb.dmst.dockerWatchdog;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util. List;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);
        // otherwise it clusters the console with unwanted info

        System.out.println( "Hello World!" );
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        //builder.withDockerHost("tcp://Localhost:2375");
        builder.withDockerCertPath("/Users/giannistampakis/.docker");
        DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();
        dockerClient.versionCmd().exec();
        List<Container> containers;
        System.out.println("-------------------ALL CONTAINER INSTANCES-~========-=--");
        containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        containers.forEach(c -> System.out.println(c.getId() + " " + c.getState()));
        String id = containers.get(0).getId();
        dockerClient.stopContainerCmd(id).exec();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
            System.err.println("Thread pause interrupted: " + e.getMessage());
        }
        System.out.println("-----. -ACTIVE CONTAINER INSTANCES-- ");
        containers = dockerClient.listContainersCmd().withShowAll(false).exec();
        containers.forEach(c -> System.out.println(c.getId() + " " + c.getState()));
        System.out.println(" - - - - - - - -------ALL CONTAINER INSTANCES----- --");
                containers = dockerClient.listContainersCmd() .withShowAll(true).exec();
        containers.forEach(c -> System.out.println(c.getId() + " "
                + c.getState()));
        dockerClient.startContainerCmd(id).exec();
        System.out.println("--------");
        containers = dockerClient.listContainersCmd() .withShowAll(false).exec();
        containers.forEach(c -> System.out.println(c.getId() +" "+ c.getState()));
    }
}
