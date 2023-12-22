package gr.aueb.dmst.dockerWatchdog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DesktopApp {

    private HttpClient client;

    public DesktopApp() {
        this.client = HttpClient.newHttpClient();
    }

    public void start() {
        JFrame frame = new JFrame("Docker Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JTextField containerIdField = new JTextField();
        containerIdField.setPreferredSize(new Dimension(200, 20));

        JTextArea instancesTextArea = new JTextArea(10, 30);
        instancesTextArea.setEditable(false);

        JTextArea metricsTextArea = new JTextArea(10, 30);
        metricsTextArea.setEditable(false);

        JButton startButton = new JButton("Start Container");
        startButton.addActionListener(e -> {
            try {
                String containerId = containerIdField.getText();
                startContainer(containerId);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        JButton stopButton = new JButton("Stop Container");
        stopButton.addActionListener(e -> {
            try {
                String containerId = containerIdField.getText();
                stopContainer(containerId);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        JButton refreshInstancesButton = new JButton("Refresh Instances");
        refreshInstancesButton.addActionListener(e -> {
            try {
                String instances = getAllInstances();
                instancesTextArea.setText(instances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        JLabel startDateLabel = new JLabel("Start Date:");
        JTextField startDateField = new JTextField();
        startDateField.setPreferredSize(new Dimension(200, 20));

        JLabel endDateLabel = new JLabel("End Date:");
        JTextField endDateField = new JTextField();
        endDateField.setPreferredSize(new Dimension(200, 20));

        JButton showMetricsButton = new JButton("Show Metrics between dates");
        showMetricsButton.addActionListener(e -> {
            try {
                String startDateString = startDateField.getText();
                String endDateString = endDateField.getText();
                Timestamp startDate = Timestamp.valueOf(startDateString);
                Timestamp endDate = Timestamp.valueOf(endDateString);
                String metrics = getAllMetrics(startDate, endDate);
                metricsTextArea.setText(metrics);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        JTextField runningInstancesField = new JTextField();
        runningInstancesField.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(containerIdField);
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(refreshInstancesButton);
        panel.add(new JScrollPane(instancesTextArea));
        panel.add(startDateLabel);
        panel.add(startDateField);
        panel.add(endDateLabel);
        panel.add(endDateField);
        panel.add(showMetricsButton);
        panel.add(new JScrollPane(metricsTextArea));
        panel.add(runningInstancesField);
        try {
            String instances = getAllInstances();
            int runningCount = getRunningInstancesCount();
            runningInstancesField.setText("Running instances: " + runningCount);
            instancesTextArea.setText(instances);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public void startContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void stopContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String getAllInstances() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        StringBuilder formattedResponse = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            formattedResponse.append(jsonObject.toString(4)); // 4 is the number of spaces for indentation
            formattedResponse.append("\n");
        }
        return formattedResponse.toString();
    }

    public String getAllMetrics(Timestamp startDate, Timestamp endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/metrics?startDate=" + startDateString + "&endDate=" + endDateString))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        StringBuilder formattedResponse = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            formattedResponse.append(jsonObject.toString(4)); // 4 is the number of spaces for indentation
            formattedResponse.append("\n");
        }
        return formattedResponse.toString();
    }

    public int getRunningInstancesCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/instances"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body());
        int runningCount = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if ("running".equals(jsonObject.getString("status"))) {
                runningCount++;
            }
        }
        return runningCount;
    }
}