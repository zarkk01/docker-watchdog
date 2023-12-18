package gr.aueb.dmst.dockerWatchdog;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.*;
import java.awt.*;

public class DesktopApp {

    private HttpClient client;

    public DesktopApp() {
        this.client = HttpClient.newHttpClient();
    }

    public void start() {
        JFrame frame = new JFrame("Docker Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);

        JTextField containerIdField = new JTextField();
        containerIdField.setPreferredSize(new Dimension(200, 20));

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


        JPanel panel = new JPanel();
        panel.add(containerIdField);
        panel.add(startButton);
        panel.add(stopButton);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void startContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }

    public void stopContainer(String containerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + containerId + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }


}