package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when live statistics fail to load properly.
 */
public class LiveStatsException extends Exception{

    /**
     * Constructor for the LiveStatsException class.
     */
    public LiveStatsException() {
        super("Live statistics did not load properly.");
    }

    /**
     * Returns a detailed message about the exception.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Error while loading live statistics like CPU, Memory, Network and Disk." +
                "Maybe one of the containers does not run properly or its data is not available.";
    }
}
