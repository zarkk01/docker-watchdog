package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class EventHandlingException extends Exception {

    private final String actor;
    public EventHandlingException(String actor) {
        super("Event handling did not load properly.");
        this.actor = actor;
    }

    @Override
    public String getMessage() {
        return "Error while handling an event of a " + actor + " ."
                + " Make sure your actions follow docker instructions.";
    }
}
