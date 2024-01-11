package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class LiveStatsException extends Exception{
    public LiveStatsException() {
        super("Live statistics did not load properly.");
    }

    @Override
    public String getMessage() {
        return "Error while loading live statistics like CPU, Memory, Network and Disk." +
                "Maybe one of them does not run properly or its data is not available.";
    }
}
