package gr.aueb.dmst.dockerWatchdog.Exceptions;


/**
 * This class represents a custom exception that is thrown when a container could not be modified.
 * For example, when we try to start a container that is already running.
 * It includes the ID of the container that could not be modified and the reason for the failure.
 */
public class ContainerNotModifiedException extends Exception {
    // The ID of the container that could not be modified
    private final String containerId;
    // The reason for the failure to modify the container
    private final String reason;

    /**
     * Constructor for the ContainerNotModifiedException class.
     *
     * @param containerId The ID of the container that could not be modified.
     * @param reason The reason for the failure to modify the container.
     */
    public ContainerNotModifiedException(String containerId, String reason) {
        super("Container not modified.");
        this.containerId = containerId;
        this.reason = reason;
    }

    /**
     * Returns a detailed message about the exception with the container ID and the reason for the failure.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " could not be modified: " + reason ;
    }
}
