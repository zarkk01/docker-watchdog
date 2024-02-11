package gr.aueb.dmst.dockerWatchdog.models;


/**
 * This class represents a Service that be displayed on GUI panels.
 * A ServiceScene has a name and a namespace, and we use it in Kubernetes
 * panel, showing user his services.
 */
public class ServiceScene {
    private final String name;
    private final String namespace;

    /**
     * Constructor for the ServiceScene class.
     *
     * @param name The name of the service scene.
     * @param namespace The namespace of the service scene.
     */
    public ServiceScene(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /**
     * Returns the name of the service scene.
     *
     * @return The name of the service scene.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace of the service scene.
     *
     * @return The namespace of the service scene.
     */
    public String getNamespace() {
        return namespace;
    }
}
