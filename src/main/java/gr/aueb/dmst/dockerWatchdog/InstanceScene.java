package gr.aueb.dmst.dockerWatchdog;

public class InstanceScene {
    private String id;
    private String name;
    private String status;

    public InstanceScene(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
