package gr.aueb.dmst.dockerWatchdog.gui.models;

public class SearchResultScene {
    private String repoName;
    private String description;

    public SearchResultScene(String repoName, String description) {
        this.repoName = repoName;
        this.description = description;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getDescription() {
        return description;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}