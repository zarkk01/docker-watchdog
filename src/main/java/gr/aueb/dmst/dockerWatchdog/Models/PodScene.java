package gr.aueb.dmst.dockerWatchdog.Models;

public class PodScene {
    private final String name;
    private final String namespace;
    private final String status;

    public PodScene(String name, String namespace, String status) {
        this.name = name;
        this.namespace = namespace;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getStatus() {
        return status;
    }
}