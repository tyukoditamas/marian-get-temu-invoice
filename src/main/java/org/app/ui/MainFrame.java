package org.app.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.app.signiture.SignCalculationExample;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private static final String CLIENT_ID = "YOUR_CLIENT_ID";
    private static final String APP_SECRET = "YOUR_APP_SECRET";

    private final JTextField tokenField = new JTextField(30);
    private final JTextArea logArea = new JTextArea();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MainFrame() {
        super("TEMU Invoice Downloader");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 400);

        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Resource Token:"));
        topPanel.add(tokenField);

        JButton getInvoiceButton = new JButton("Get Invoice");
        topPanel.add(getInvoiceButton);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        getInvoiceButton.addActionListener(e -> onGetInvoice());
    }

    private void onGetInvoice() {
        String token = tokenField.getText().trim();
        if (token.isEmpty()) {
            log("Please enter a resource token.");
            return;
        }

        log("Fetching invoice URL for token: " + token);
        String url = fetchInvoiceUrl(token);
        if (url == null) {
            log("Failed to fetch invoice URL.");
            return;
        }

        log("Received invoice URL: " + url);

        String home = System.getProperty("user.home");
        String downloadsDir = home + File.separator + "Downloads";
        File dir = new File(downloadsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            downloadsDir = home;
        }

        String outputPath = downloadsDir + File.separator + token + ".pdf";
        log("Downloading invoice to: " + outputPath);
        boolean success = downloadPdf(url, outputPath);

        if (success) {
            log("Download completed: " + outputPath);
        } else {
            log("Download failed.");
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String fetchInvoiceUrl(String resourceToken) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("client_id", CLIENT_ID);
            params.put("data_type", "JSON");
            params.put("resourceToken", resourceToken);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("type", "bg.clearance.invoice.get");

            String sign = SignCalculationExample.calculateSign(params, APP_SECRET);
            params.put("sign", sign);

            String requestBody = objectMapper.writeValueAsString(params);

            HttpPost post = new HttpPost("https://openapi-b-eu.temudemo.com/ark/router");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(body);
                if (root.path("success").asBoolean()) {
                    return root.path("result").path("url").asText();
                } else {
                    log("Error fetching invoice URL: " + root.path("errorMsg").asText());
                }
            }
        } catch (IOException e) {
            log("Exception fetching invoice URL: " + e.getMessage());
        }
        return null;
    }

    private boolean downloadPdf(String url, String outputPath) {
        try {
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    Files.write(Paths.get(outputPath), EntityUtils.toByteArray(entity));
                    return true;
                } else {
                    log("Download failed with status: " + response.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            log("Exception downloading PDF: " + e.getMessage());
        }
        return false;
    }
}
