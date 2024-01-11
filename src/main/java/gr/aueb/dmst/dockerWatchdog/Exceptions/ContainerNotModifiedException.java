package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ContainerNotModifiedException extends Exception {
    private final String containerId;
    private final String reason;

    public ContainerNotModifiedException(String containerId, String reason) {
        super("Container not modified.");
        this.containerId = containerId;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " could not be modified: " + reason ;
    }
}