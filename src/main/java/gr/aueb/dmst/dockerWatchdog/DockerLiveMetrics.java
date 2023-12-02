package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;

import static gr.aueb.dmst.dockerWatchdog.Main.dockerClient;
public class DockerLiveMetrics {

    public static void liveMeasure() {

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

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
    }

    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics> {

        public String id ;
        public CustomResultCallback(String id) {
            this.id = id ;

        }
        @Override
        public void onNext(Statistics stats) {
            // Handle the received statistics

            // CPU Stats
            long cpuUsage = getRawCpuUsage(stats);
            Objects.requireNonNull(MyInstance.getInstanceByid(id)).setCpuUsage((double)cpuUsage/ 1_000_000_000);

            // Memory Stats
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            Objects.requireNonNull(MyInstance.getInstanceByid(id)).setMemoryUsage(memoryUsage);

            // process IDs (PIDs) statistics..
            Long pids = stats.getPidsStats().getCurrent();
            MyInstance.getInstanceByid(id).setPids(pids);

            // blkio (block I/O) statistics

            // network I/O statistics



        }

        private Long getRawCpuUsage(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();

            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onStart(Closeable closeable) {
        }
    }

}
