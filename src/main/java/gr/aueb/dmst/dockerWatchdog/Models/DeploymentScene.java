package gr.aueb.dmst.dockerWatchdog.Models;

/**
 * This class represents a Deployment that be displayed in GUI panels.
 * A deployment has a name and a namespace, and we use it in Kubernetes
 * panel, showing user his deployments.
 */
public class DeploymentScene {
    // Name of the deployment
    private final String name;
    // Namespace of the deployment
    private final String namespace;

    /**
     * Constructor for the DeploymentScene class.
     * @param name The name of the deployment.
     * @param namespace The namespace of the deployment.
     */
    public DeploymentScene(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /**
     * This method returns the name of the deployment.
     * @return The name of the deployment.
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the namespace of the deployment.
     * @return The namespace of the deployment.
     */
    public String getNamespace() {
        return namespace;
    }
}
