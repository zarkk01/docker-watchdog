package gr.aueb.dmst.dockerWatchdog.Exceptions;


/**
 * This class represents a custom exception that is thrown when a Docker volume cannot be modified.
 * It extends the Exception class and overrides the getMessage method to provide a custom error message.
 */
public class VolumeNotModifiedException extends Exception{
    // The name of the volume that was not found
    private final String volumeName;

    /**
     * Constructor for the VolumeNotModifiedxception class.
     * @param volumeName The name of the volume that was not modified.
     */
    public VolumeNotModifiedException(String volumeName) {
        super("Volume did not modified.");
        this.volumeName = volumeName;
    }

    /**
     * Overrides the getMessage method from the Exception class.
     * Provides a custom error message that includes the name of the volume that was not modified.
     * @return A string that represents the custom error message.
     */
    @Override
    public String getMessage() {
        return "Volume " + volumeName + " not modified. Please check the volume name and try again.";
    }
}