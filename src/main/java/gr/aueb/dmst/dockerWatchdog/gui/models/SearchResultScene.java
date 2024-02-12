package gr.aueb.dmst.dockerWatchdog.gui.models;


/**
 * SearchResultScene represents the result of an image search on DockerHub.
 * It contains the repository name and description of the image.
 */
public class SearchResultScene {
    private String repoName; // The name of the repository
    private String description; // The description of the image

    /**
     * Constructs a new SearchResultScene with the given repository name and description.
     *
     * @param repoName the name of the repository
     * @param description the description of the image
     */
    public SearchResultScene(String repoName, String description) {
        this.repoName = repoName;
        this.description = description;
    }

    /**
     * Returns the name of the repository.
     *
     * @return the name of the repository
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * Returns the description of the image.
     *
     * @return the description of the image
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the name of the repository.
     *
     * @param repoName the new name of the repository
     */
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    /**
     * Sets the description of the image.
     *
     * @param description the new description of the image
     */
    public void setDescription(String description) {
        this.description = description;
    }
}