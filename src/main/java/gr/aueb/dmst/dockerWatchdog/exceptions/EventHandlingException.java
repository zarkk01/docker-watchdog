package gr.aueb.dmst.dockerWatchdog.exceptions;


/**
 * This class represents a custom exception that is thrown when an event handling operation fails.
 * It includes the actor involved in the event handling operation.
 */
public class EventHandlingException extends Exception {
    // The actor involved in the event handling operation
    private final String actor;

    /**
     * Constructor for the EventHandlingException class.
     *
     * @param actor The actor involved in the event handling operation.
     */
    public EventHandlingException(String actor) {
        super("Event handling did not load properly.");
        this.actor = actor;
    }

    /**
     * Returns a detailed message about the exception with the actor involved in the event handling operation.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Error while handling an event of a " + actor + " ."
                + " Make sure your actions follow docker instructions.";
    }
}
