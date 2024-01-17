package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * Exception thrown when an action fails on a container in its individual panel.
 * This class extends Exception and provides a constructor that accepts the action that failed and the ID of the container.
 * The constructor builds a detailed error message that includes the action and the container ID.
 */
public class ContainerActionFailedException extends Exception {
    /**
     * Constructs a new ContainerActionFailedException with a detailed error message.
     *
     * @param action The action that failed.
     * @param containerId The ID of the container on which the action failed.
     */
    public ContainerActionFailedException(String action, String containerId) {
        super("Failed to " + action + " container with ID " + containerId + ".");
    }
}

