package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class DatabaseOperationException extends Exception {

    private final String operation;
    private final String actors;
    public DatabaseOperationException(String operation, String actors) {
        super("MYSQL Database operation did not performed in the way we thought.");
        this.operation = operation;
        this.actors = actors;
    }

    @Override
    public String getMessage() {
        return "Failed database operation : " + operation + " ." +
                " Make sure it is all okay with your " + actors + " and try again.";
    }
}
