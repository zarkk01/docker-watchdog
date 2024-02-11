package gr.aueb.dmst.dockerWatchdog.Models;


/**
 * This class represents a StatefulSet that can be displayed on GUI panels.
 * A StatefulSetScene has a name and a namespace, and it is used in the Kubernetes
 * panel, showing the user their stateful sets.
 */
public class StatefulSetScene {
    private final String name;
    private final String namespace;

    /**
     * Constructor for the StatefulSetScene class.
     *
     * @param name The name of the stateful set.
     * @param namespace The namespace of the stateful set.
     */
    public StatefulSetScene(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /**
     * Returns the name of the stateful set.
     *
     * @return The name of the stateful set.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace of the stateful set.
     *
     * @return The namespace of the stateful set.
     */
    public String getNamespace() {
        return namespace;
    }
}
