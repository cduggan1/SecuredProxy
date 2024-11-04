package org.cduggan;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Main {
    private static final int PORT = 8081;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
        Logger.log("Proxy Server started on port " + PORT,false);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setKeepAlive(true);
            Logger.log("Accepted connection from " + clientSocket.getInetAddress(), true);
            threadPool.submit(new ProxyTask(clientSocket));
        }
    }
}

class ProxyTask implements Runnable {
    private Socket clientSocket;

    public ProxyTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                InputStream clientInput = clientSocket.getInputStream();
                OutputStream clientOutput = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput))
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null) {
                Logger.log("Received an empty request line.", true);
                return;
            }

            Logger.log("Request Line: " + requestLine, true);

            ConnectionHandler handler = new ConnectionHandler(clientInput, clientOutput, requestLine);
            handler.handleConnection();

        } catch (IOException e) {
            Logger.log("Error in ProxyTask: " + e.getMessage(), false);
        }
    }
}
