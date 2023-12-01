package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import java.io.Closeable;
import java.util.List;

public class DockerLiveMetrics {

    public static void liveMeasure() {
        // Create a Docker client

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            String id = container.getId();
            AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd = dockerClient.statsCmd(id);
            try {
                asyncStatsCmd.exec(new CustomResultCallback(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        try {
//            Thread.sleep(6000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics> {

        public String id ;
        public CustomResultCallback(String id) {
            this.id = id ;

        }
        @Override
        public void onNext(Statistics stats) {
            // Handle the received statistics

            // Memory Stats
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage : 0L;
            //System.out.println("Memory Usage: " + memoryUsage +" bytes");
            MyInstance.getInstanceByid(id).setMemoryUsage(memoryUsage);

            // process IDs (PIDs) statistics..


            // blkio (block I/O) statistics

            // network I/O statistics



        }

        @Override
        public void onError(Throwable throwable) {
            // Handle errors appropriately
            System.err.println("Error during execution: " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            // Clean up or perform any necessary actions when the stream is completed
            System.out.println("Stream completed");
        }

        @Override
        public void onStart(Closeable closeable) {
            // Perform any setup actions when the stream starts
            System.out.println("Stream started");
        }
    }

}
