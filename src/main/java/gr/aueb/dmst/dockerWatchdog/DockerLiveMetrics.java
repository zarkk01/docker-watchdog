package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.BlkioStatEntry;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

import java.io.Closeable;
import java.util.List;

public class DockerLiveMetrics {

    // Method liveMeasure that keeps track of CPU Usage, Memory Usage, Block I/O and PIDs
    public static void liveMeasure() {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        // Lists of all containers of docker desktop using dockerClient that is initiated in Main.java
        List < Container > containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        // For every container in containers list
        for (Container container: containers) {
            // Get the id of the container
            String id = container.getId();
            // Get the stats of the container
            AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd = Main.dockerClient.statsCmd(id);
            try {
                // Execute the statsCmd and call CustomResultCallback
                asyncStatsCmd.exec(new CustomResultCallback(id, asyncStatsCmd));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // CustomResultCallback that extends ResultCallbackTemplate and implements Statistics class
    private static class CustomResultCallback extends ResultCallbackTemplate < CustomResultCallback, Statistics > {

        // Initiate id and asyncStatsCmd
        public String id;
        private AsyncDockerCmd < StatsCmd,
                Statistics > asyncStatsCmd;

        // Constructor
        public CustomResultCallback(String id, AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd) {
            this.id = id;
            this.asyncStatsCmd = asyncStatsCmd;

        }

        // Override onNext method of ResultCallbackTemplate class that is called
        // when a new statistics is received from the docker daemon
        @Override
        public void onNext(Statistics stats) {

            // CPU stats
            long cpuUsage = getCpuUsageInNanos(stats);

            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                if (cpuUsage != 0) {
                    // If cpuUsage is not 0 then set the cpuUsage of the instance with the value of cpuUsage
                    MyInstance.getInstanceByid(id).setCpuUsage((double) cpuUsage / 1_000_000_000);
                } else {
                    // If cpuUsage is 0 then set the cpuUsage of the instance with 0
                    MyInstance.getInstanceByid(id).setCpuUsage(0.0);
                }
            }

            // Memory stats
            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setMemoryUsage(memoryUsage);
            }

            // Process IDs (PIDs) stats
            Long pids = stats.getPidsStats().getCurrent();
            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                if (pids != null) {
                    MyInstance.getInstanceByid(id).setPids(pids);
                } else {
                    MyInstance.getInstanceByid(id).setPids(0);
                }
            }

            // BLCKIO (block I/O) stats
            List < BlkioStatEntry > ioServiceBytes = stats.getBlkioStats().getIoServiceBytesRecursive();
            Long readBytes = null;

            // If ioServiceBytes is not null then get the value of Read
            if (ioServiceBytes != null) {
                readBytes = getIoServiceBytesValue(ioServiceBytes, "Read");
            }
            Long writeBytes = null;
            // If ioServiceBytes is not null then get the value of Write
            if (ioServiceBytes != null) {
                writeBytes = getIoServiceBytesValue(ioServiceBytes, "Write");
            }

            // If the instance with the id exists
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
                MyInstance.getInstanceByid(id).setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            }
        }

        // Method getCpuUsageInNanos that returns the CPU usage in nanoseconds
        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();
            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        // Method getIoServiceBytesValue that returns the value of ioServiceBytes
        private Long getIoServiceBytesValue(List < BlkioStatEntry > ioServiceBytes, String type) {
            for (BlkioStatEntry entry: ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        // Override onError method of ResultCallbackTemplate class
        // that is called when some error occurs
        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        // Override onComplete method of ResultCallbackTemplate
        @Override
        public void onComplete() {
            // Close the AsyncDockerCmd
            asyncStatsCmd.close();
        }

        // Override onStart method of ResultCallbackTemplate that do nothing
        @Override
        public void onStart(Closeable closeable) {}
    }
}