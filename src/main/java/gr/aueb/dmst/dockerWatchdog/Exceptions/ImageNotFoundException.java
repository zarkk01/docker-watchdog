package gr.aueb.dmst.dockerWatchdog.Exceptions;

public class ImageNotFoundException extends Exception {
    private final String imageName;

    public ImageNotFoundException(String imageName) {
        super("Image not found.");
        this.imageName = imageName;
    }

    @Override
    public String getMessage() {
        return "Image " + imageName + " not found. Please check the image name and try again.";
    }
}