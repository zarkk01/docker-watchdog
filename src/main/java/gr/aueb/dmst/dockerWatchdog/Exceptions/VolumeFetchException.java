package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when there is an error
 * while fetching volumes from the database.
 */
public class VolumeFetchException extends Exception {

    /**
     * Constructs a new VolumeFetchException with the specified detail message.
     *
     * @param message the detail message.
     */
    public VolumeFetchException(String message) {
        super(message);
    }
}