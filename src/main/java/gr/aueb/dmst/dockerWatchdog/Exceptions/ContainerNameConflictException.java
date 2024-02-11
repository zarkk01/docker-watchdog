package gr.aueb.dmst.dockerWatchdog.Exceptions;


/**
 * This class represents a custom exception that is thrown when there is a conflict
 * in a container's rename process.
 * It includes the old name of the container and the new name that caused the conflict.
 */
public class ContainerNameConflictException extends Exception{
    // The old name of the container
    private final String oldName;
    // The new name that caused the conflict
    private final String newName;

    /**
     * Constructor for the ContainerNameConflictException class.
     *
     * @param oldName The old name of the container.
     * @param newName The new name that caused the conflict.
     */
    public ContainerNameConflictException(String oldName, String newName) {
        super("Container name conflict.");
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * Returns a detailed message about the exception with the old and new container names.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Container " + oldName + " could not be renamed to " + newName + " : Container with the same name already exists.";
    }
}
