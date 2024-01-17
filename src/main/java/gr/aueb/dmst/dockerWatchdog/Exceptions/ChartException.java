package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * The ChartException class is a custom exception class used to handle specific issues related to charts in the GraphicsController.
 * The class contains a single field, `chartName`, which represents the name of the chart where the error occurred.
 */
public class ChartException extends Exception {
    private String chartName;

    /**
     * Constructs a new ChartException with the specified detail message.
     *
     * @param chartName The name of the chart where the error occurred.
     */
    public ChartException(String chartName) {
        super("Error occurred in chart: " + chartName);
        // Save the chart name for later retrieval.
        this.chartName = chartName;
    }

    /**
     * Retrieves the name of the chart where the error occurred.
     *
     * @return The name of the chart where the error occurred.
     */
    public String getChartName() {
        return chartName;
    }
}