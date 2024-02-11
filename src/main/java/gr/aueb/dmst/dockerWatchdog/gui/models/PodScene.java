package gr.aueb.dmst.dockerWatchdog.gui.models;


/**
 * This class represents a Pod that be displayed in GUI panels.
 * A PodScene has a name, namespace, and status, and it is what the user
 *  actually sees on the Kubernetes panel.
 */
public class PodScene {
    private final String name;
    private final String namespace;
    private final String status;

    /**
     * Constructor for the PodScene class.
     *
     * @param name The name of the pod scene.
     * @param namespace The namespace of the pod scene.
     * @param status The status of the pod scene.
     */
    public PodScene(String name, String namespace, String status) {
        this.name = name;
        this.namespace = namespace;
        this.status = status;
    }

    /**
     * Returns the name of the pod scene.
     *
     * @return The name of the pod scene.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace of the pod scene.
     *
     * @return The namespace of the pod scene.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the status of the pod scene.
     *
     * @return The status of the pod scene.
     */
    public String getStatus() {
        return status;
    }
}
