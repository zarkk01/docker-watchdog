package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ContainerNotFoundException extends Exception {
    private final String containerId;

    public ContainerNotFoundException(String containerId) {
        super("Container not found.");
        this.containerId = containerId;
    }

    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " not found. Please check the container ID and try again.";
    }
}