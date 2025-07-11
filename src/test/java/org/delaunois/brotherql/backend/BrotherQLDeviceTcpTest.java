package org.delaunois.brotherql.backend;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.example.PrintExample;
import org.delaunois.brotherql.util.Hex;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Objects;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static org.junit.Assert.assertEquals;

public class BrotherQLDeviceTcpTest {

    private static final System.Logger LOGGER = System.getLogger(BrotherQLDeviceTcp.class.getName());
    private static final int DEFAULT_PORT = 9101;

    private ServerSocket serverSocket;
    private Thread serverThread;
    private final StringBuilder receivedData = new StringBuilder();

    @Test
    public void testRequestStatus() throws Exception {
        startServer();
        BrotherQLDeviceTcp device = new BrotherQLDeviceTcp(URI.create("tcp://localhost:" + DEFAULT_PORT + "/QL-720NW"));
        BrotherQLConnection connection = new BrotherQLConnection(device);
        connection.open();
        connection.close();
        stopServer();

        InputStream rasterIs = PrintExample.class.getResourceAsStream("/init.raster");
        String raster = new String(Objects.requireNonNull(rasterIs).readAllBytes());
        assertEquals(raster, receivedData.toString());
    }

    private void startServer() throws IOException {
        // Starts a server socket that listens to request
        serverSocket = new ServerSocket(DEFAULT_PORT);

        // Lance un thread pour accepter les connexions et lire les données reçues
        serverThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Accepte une connexion
                    try (Socket clientSocket = serverSocket.accept();
                         InputStream inputStream = clientSocket.getInputStream();
                         ByteArrayOutputStream buffer = new ByteArrayOutputStream()
                    ) {
                        byte[] data = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(data)) != -1) {
                            buffer.write(data, 0, bytesRead);
                        }

                        synchronized (receivedData) {
                            byte[] rx = buffer.toByteArray();
                            LOGGER.log(INFO, "Rx: " + Hex.toString(rx));
                            receivedData.append(Hex.toString(rx));
                        }
                    }
                }
            } catch (Exception e) {
                if (!serverSocket.isClosed()) {
                    LOGGER.log(ERROR, e);
                }
            }
        });

        serverThread.start();
    }

    private void stopServer() throws IOException, InterruptedException {
        // Stop the server
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread.join();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

}
