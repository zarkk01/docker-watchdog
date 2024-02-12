package gr.aueb.dmst.dockerWatchdog.gui.models;

public class SearchResultScene {
    private String repoName;
    private String description;
    private int pullCount;

    public SearchResultScene(String repoName, String description, int pullCount) {
        this.repoName = repoName;
        this.description = description;
        this.pullCount = pullCount;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getDescription() {
        return description;
    }

    public int getPullCount() {
        return pullCount;
    }
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPullCount(int pullCount) {
        this.pullCount = pullCount;
    }
}