package gr.aueb.dmst.dockerWatchdog.Exceptions;


/**
 * This class represents a custom exception that is thrown when a list fails to be filled properly.
 * It includes the name of the list that failed to be filled.
 */
public class ListFillingException extends Exception {
    // The name of the list that failed to be filled
    private final String listName;

    /**
     * Constructor for the ListFillingException class.
     *
     * @param listName The name of the list that failed to be filled.
     */
    public ListFillingException(String listName) {
        super("Lists did not filled properly.");
        this.listName = listName;
    }

    /**
     * Returns a detailed message about the exception with the name of the list
     * , and with the docker components (instances, images, volumes) that caused the exception.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Error while filling list: " + listName + " . Check out your : " + listName.toLowerCase() + " .";
    }
}
