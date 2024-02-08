package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when an attempt is made to delete a running container.
 * It includes the ID of the container that is currently running.
 */
public class ContainerRunningException extends Exception {
    // The ID of the container that is currently running
    private final String containerId;

    /**
     * Constructor for the ContainerRunningException class.
     *
     * @param containerId The ID of the container that is currently running.
     */
    public ContainerRunningException(String containerId) {
        super("Container is running.");
        this.containerId = containerId;
    }

    /**
     * Returns a detailed message about the exception with the container ID.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " is currently running. Try stopping it first.";
    }
}
