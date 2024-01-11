package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ListFillingException extends Exception {
    private final String listName;
    public ListFillingException(String listName) {
        super("Lists did not filled properly.");
        this.listName = listName;
    }

    @Override
    public String getMessage() {
        return "Error while filling list: " + listName + " . Check out your : " + listName.toLowerCase() + " .";
    }
}
