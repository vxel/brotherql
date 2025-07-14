package org.delaunois.brotherql.backend;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.BrotherQLModel;
import org.delaunois.brotherql.example.PrintExample;
import org.delaunois.brotherql.util.Hex;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static org.junit.Assert.assertEquals;

public class BrotherQLDeviceTcpTest {

    private static final System.Logger LOGGER = System.getLogger(BrotherQLDeviceTcp.class.getName());
    private static final int DEFAULT_PORT = 9101;

    private ServerSocket serverSocket;
    private Thread serverThread;
    private final ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

    @Test
    public void testRequestStatus() throws Exception {
        startServer();
        try {
            BrotherQLDeviceTcp device = new BrotherQLDeviceTcp(URI.create("tcp://localhost:" + DEFAULT_PORT + "/" + BrotherQLModel.QL_820NWB.name));
            BrotherQLConnection connection = new BrotherQLConnection(device);
            connection.open();
            connection.close();
        } finally {
            stopServer();
        }

        InputStream rasterIs = PrintExample.class.getResourceAsStream("/init.raster");
        String raster = new String(Objects.requireNonNull(rasterIs).readAllBytes());
        assertEquals(raster, Hex.toString(receivedData.toByteArray()));
    }

    @Test
    public void testSendJob() throws Exception {
        InputStream is = PrintExample.class.getResourceAsStream("/white-dove-306.png");
        InputStream rasterIs = PrintExample.class.getResourceAsStream("/white-dove-306.raster");
        String raster = new String(Objects.requireNonNull(rasterIs).readAllBytes());
        BufferedImage img = ImageIO.read(Objects.requireNonNull(is));
        
        BrotherQLJob job = new BrotherQLJob()
                .setAutocut(true)
                .setMedia(BrotherQLMedia.DC_29X90_720)
                .setAutocut(false)
                .setBrightness(1.0f)
                .setImages(List.of(img));
        
        startServer();
        try {
            BrotherQLDeviceTcp device = new BrotherQLDeviceTcp(URI.create("tcp://localhost:" + DEFAULT_PORT + "/" + BrotherQLModel.QL_820NWB.name));
            BrotherQLConnection connection = new BrotherQLConnection(device);
            connection.open();
            connection.sendJob(job);
            connection.close();
        } finally {
            stopServer();
        }

        LOGGER.log(DEBUG, "Rx:\n" + Hex.prettyDump(receivedData.toByteArray()));
        
        assertEquals(raster, Hex.toString(receivedData.toByteArray()));
    }

    private void startServer() throws IOException {
        receivedData.reset();
        // Starts a server socket that listens to request
        serverSocket = new ServerSocket(DEFAULT_PORT);
        serverSocket.setSoTimeout(10000);

        // Lance un thread pour accepter les connexions et lire les données reçues
        serverThread = new Thread(() -> {
            LOGGER.log(DEBUG, "Server started");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Accepte une connexion
                    LOGGER.log(DEBUG, "Server listening on port " + DEFAULT_PORT);
                    try (Socket clientSocket = serverSocket.accept();
                         InputStream inputStream = clientSocket.getInputStream()
                    ) {
                        byte[] data = new byte[1024];
                        int bytesRead;
                        synchronized (receivedData) {
                            while ((bytesRead = inputStream.read(data)) != -1) {
                                receivedData.write(data, 0, bytesRead);
                            }
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

    private void stopServer() throws IOException {
        // Stop the server
        if (serverThread != null) {
            LOGGER.log(DEBUG, "Stopping server");
            serverThread.interrupt();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            LOGGER.log(DEBUG, "Closing socket");
            serverSocket.close();
        }
    }
    
}
