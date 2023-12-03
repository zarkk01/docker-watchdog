package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.BlkioStatEntry;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;

public class DockerLiveMetrics {

    public static void liveMeasure() {

        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger
                (org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            String id = container.getId();
            AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd = Main.dockerClient.statsCmd(id);
            try {
                asyncStatsCmd.exec(new CustomResultCallback(id,asyncStatsCmd));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics> {

        public String id ;
        private AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd;
        public CustomResultCallback(String id,AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd) {
            this.id = id ;
            this.asyncStatsCmd = asyncStatsCmd;

        }
        @Override
        public void onNext(Statistics stats) {

            // CPU Stats
            long cpuUsage = getCpuUsageInNanos(stats);
            if(cpuUsage != 0 ){
                MyInstance.getInstanceByid(id).setCpuUsage((double)cpuUsage/ 1_000_000_000);
            } else {
                MyInstance.getInstanceByid(id).setCpuUsage(0.0);
            }

            // Memory Stats
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            Objects.requireNonNull(MyInstance.getInstanceByid(id)).setMemoryUsage(memoryUsage);

            // process IDs (PIDs) stats..
            Long pids = stats.getPidsStats().getCurrent();
            if(pids != null) {
                MyInstance.getInstanceByid(id).setPids(pids);
            } else {
                MyInstance.getInstanceByid(id).setPids(0);
            }
            // blkio (block I/O) statistics
            List<BlkioStatEntry> ioServiceBytes = stats.getBlkioStats().getIoServiceBytesRecursive();

// Assuming you are interested in read and write block I/O statistics
            Long readBytes = getIoServiceBytesValue(ioServiceBytes, "Read");
            Long writeBytes = getIoServiceBytesValue(ioServiceBytes, "Write");

            MyInstance.getInstanceByid(id).setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
            MyInstance.getInstanceByid(id).setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            // network I/O statistics

        }

        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();

            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
            // Close the AsyncDockerCmd in a separate block
            asyncStatsCmd.close();
        }

        @Override
        public void onStart(Closeable closeable) {
        }
        private Long getIoServiceBytesValue(List<BlkioStatEntry> ioServiceBytes, String type) {
            for (BlkioStatEntry entry : ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

}
