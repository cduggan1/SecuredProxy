package org.cduggan;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URL;

public class ConnectionHandler {
    private static final String DENIED_URL = "facebook.com";

    private final InputStream clientInput;
    private final OutputStream clientOutput;
    private final String requestLine;
    private Policy policy;
    private final String requesterIp;
    private boolean FAIL_OPEN = true;

    public ConnectionHandler(InputStream clientInput, OutputStream clientOutput, String requestLine, Policy policy, String requesterIp) {
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
        this.requestLine = requestLine;
        this.policy = policy;
        this.requesterIp = requesterIp;
    }

    public void handleConnection() throws IOException {
        String[] requestParts = requestLine.split(" ");
        String method = requestParts[0];
        String urlStr = requestParts[1];

        Logger.log("Processing request for URL: " + urlStr + " from IP: " + requesterIp, false);

        try {
            URL url = new URL("http://" + urlStr);
            if (policy.checkAndLogAccess(url, requesterIp)) {
                System.out.println("Denyinh");
                denyAccess();
            } else if (method.equals("CONNECT")) {
                Logger.log("Handling HTTPS connection for URL: " + urlStr, true);
                handleConnect(urlStr);
            } else {
                Logger.log("Handling HTTP request to URL: " + urlStr, true);
                handleHttpRequest();
            }
        } catch (Exception e) {
            Logger.log("Error processing URL: " + e.getMessage(), true);
            denyAccess();
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

        try {
            clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            clientOutput.flush();

            // Establish SSL connection
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port)) {
                sslSocket.startHandshake();

                String cipherSuite = sslSocket.getSession().getCipherSuite();
                Logger.log("Cipher Suite used: " + cipherSuite, true);

                // Forward data between client and server using SSL socket
                try (InputStream serverInput = sslSocket.getInputStream();
                     OutputStream serverOutput = sslSocket.getOutputStream()) {

                    Thread clientToServer = new Thread(() -> forwardData(clientInput, serverOutput, "Client to Server"));
                    Thread serverToClient = new Thread(() -> forwardData(serverInput, clientOutput, "Server to Client"));

                    clientToServer.start();
                    serverToClient.start();
                    clientToServer.join();
                    serverToClient.join();

                    Logger.log("HTTPS tunnel closed.", true);
                }
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
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                Logger.log("Error closing streams in " + direction + ": " + e.getMessage(), true);
            }
        }
    }

}
