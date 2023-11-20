package gr.aueb.dmst.dockerWatchdog;
public class Main
{
    public static void main( String[] args )
    {
        // Initiate monitorThread
        MonitorThread dockerMonitor = new MonitorThread();
        Thread monitorThread = new Thread(dockerMonitor);

        // Start monitorThread and run method executes
        monitorThread.start();

        // Allow the monitoring to run for a while
        try {
            Thread.sleep(60000); // 1 minute
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop the monitoring thread - stop updates
        dockerMonitor.stopMonitoring();
    }
}
