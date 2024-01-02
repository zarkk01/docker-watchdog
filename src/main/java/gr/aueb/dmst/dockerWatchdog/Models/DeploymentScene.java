package gr.aueb.dmst.dockerWatchdog.Models;

public class DeploymentScene {
    private final String name;
    private final String namespace;

    public DeploymentScene(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }
}