package gr.aueb.dmst.dockerWatchdog.Exceptions;


public class ContainerCreationException extends Exception {
    private final String imageName;

    public ContainerCreationException(String imageName) {
        super("Failed to create container.");
        this.imageName = imageName;
    }

    @Override
    public String getMessage() {
        return "Failed to create container from " + imageName + " . Check if all requirements are met.";
    }
}