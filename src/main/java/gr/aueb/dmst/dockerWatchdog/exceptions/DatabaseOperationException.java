package gr.aueb.dmst.dockerWatchdog.exceptions;


/**
 * This class represents a custom exception that is thrown when a database operation fails.
 * It includes the operation that failed and the actors involved in the operation.
 */
public class DatabaseOperationException extends Exception {
    // The operation that failed
    private final String operation;
    // The actors (instances, images, volumes) involved in the operation
    private final String actors;

    /**
     * Constructor for the DatabaseOperationException class.
     *
     * @param operation The operation that failed.
     * @param actors The actors involved in the operation.
     */
    public DatabaseOperationException(String operation, String actors) {
        super("MYSQL Database operation did not performed in the way we thought.");
        this.operation = operation;
        this.actors = actors;
    }

    /**
     * Returns a detailed message about the exception with the operation and the actors.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Failed database operation : " + operation + " ." +
                " Make sure it is all okay with your " + actors + " and try again.";
    }
}

