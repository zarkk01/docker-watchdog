package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ContainerRunningException extends Exception {
    private final String containerId;

    public ContainerRunningException(String containerId) {
        super("Container is running.");
        this.containerId = containerId;
    }

    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " is currently running. Try stopping it first.";
    }
}