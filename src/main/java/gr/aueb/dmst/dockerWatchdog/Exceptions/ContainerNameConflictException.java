package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ContainerNameConflictException extends Exception{
    private final String oldName;
    private final String newName;

    public ContainerNameConflictException(String oldName, String newName) {
        super("Container name conflict.");
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public String getMessage() {
        return "Container " + oldName + " could not be renamed to " + newName + " : Container with the same name already exists.";
    }
}
