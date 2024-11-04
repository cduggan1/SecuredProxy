package org.cduggan;

import java.io.*;
import java.net.Socket;
import java.net.URL;

public class ConnectionHandler {
    private static final String DENIED_URL = "facebook.com";

    private final InputStream clientInput;
    private final OutputStream clientOutput;
    private final String requestLine;

    public ConnectionHandler(InputStream clientInput, OutputStream clientOutput, String requestLine) {
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
        this.requestLine = requestLine;
    }

    public void handleConnection() throws IOException {
        String[] requestParts = requestLine.split(" ");
        String method = requestParts[0];
        String url = requestParts[1];

        Logger.log("Processing request for URL: " + url, false);
        if (isDeniedUrl(url)) {
            denyAccess();
        } else if (method.equals("CONNECT")) {
            Logger.log("Handling HTTPS connection for URL: " + url, true);
            handleConnect(url);
        } else {
            Logger.log("Handling HTTP request to URL: " + url, true);
            handleHttpRequest();
        }
    }

    private boolean isDeniedUrl(String url) {
        try {
            URL parsedUrl = new URL("http://" + url);
            return parsedUrl.getHost().contains(DENIED_URL);
        } catch (Exception e) {
            Logger.log("Error parsing URL: " + e.getMessage(), true);
            return false;
        }
    }

    private void denyAccess() throws IOException {
        String response = "HTTP/1.1 403 Forbidden\r\n\r\n";
        clientOutput.write(response.getBytes());
        clientOutput.flush();
        Logger.log("Access denied to " + requestLine, false);
    }

    private void handleHttpRequest() throws IOException {
        String[] parts = requestLine.split(" ");
        String urlStr = parts[1];
        URL url = new URL(urlStr);
        String host = url.getHost();
        int port = url.getPort() == -1 ? 80 : url.getPort();

        Logger.log("Connecting to HTTP server at " + host + ":" + port, true);

        try (Socket socket = new Socket(host, port);
             OutputStream serverOutput = socket.getOutputStream();
             InputStream serverInput = socket.getInputStream()) {

            Logger.log("Connected to HTTP server, sending request.", true);
            serverOutput.write((requestLine + "\r\n").getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                serverOutput.write((line + "\r\n").getBytes());
            }
            serverOutput.write("\r\n".getBytes());
            serverOutput.flush();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = serverInput.read(buffer)) != -1) {
                Logger.log("Forwarding " + bytesRead + " bytes from server to client.", true);
                clientOutput.write(buffer, 0, bytesRead);
                clientOutput.flush();
            }

            Logger.log("HTTP request handled successfully.", true);
        }
    }

    private void handleConnect(String url) throws IOException {
        String[] urlParts = url.split(":");
        String host = urlParts[0];
        int port = Integer.parseInt(urlParts[1]);

        Logger.log("Establishing HTTPS tunnel to " + host + ":" + port, true);

        try (Socket serverSocket = new Socket(host, port)) {
            clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            clientOutput.flush();

            try (InputStream serverInput = serverSocket.getInputStream();
                 OutputStream serverOutput = serverSocket.getOutputStream()) {

                Thread clientToServer = new Thread(() -> forwardData(clientInput, serverOutput, "Client to Server"));
                Thread serverToClient = new Thread(() -> forwardData(serverInput, clientOutput, "Server to Client"));

                clientToServer.start();
                serverToClient.start();
                clientToServer.join();
                serverToClient.join();

                Logger.log("HTTPS tunnel closed.", true);
            }
        } catch (IOException | InterruptedException e) {
            Logger.log("Error in HTTPS tunnel: " + e.getMessage(), true);
        }
    }

    private void forwardData(InputStream input, OutputStream output, String direction) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                Logger.log(direction + ": Transferred " + bytesRead + " bytes.", true);
                output.write(buffer, 0, bytesRead);
                output.flush();
            }
        } catch (IOException e) {
            Logger.log("Error during " + direction + ": " + e.getMessage(), true);
        }
    }
}
