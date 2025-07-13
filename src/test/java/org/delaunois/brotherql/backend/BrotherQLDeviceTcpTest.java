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
        BrotherQLDeviceTcp device = new BrotherQLDeviceTcp(URI.create("tcp://localhost:" + DEFAULT_PORT + "/" + BrotherQLModel.QL_820NWB.name));
        BrotherQLConnection connection = new BrotherQLConnection(device);
        connection.open();
        connection.close();
        stopServer();

        InputStream rasterIs = PrintExample.class.getResourceAsStream("/init.raster");
        String raster = new String(Objects.requireNonNull(rasterIs).readAllBytes());
        assertEquals(raster, receivedData.toString());
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
        BrotherQLDeviceTcp device = new BrotherQLDeviceTcp(URI.create("tcp://localhost:" + DEFAULT_PORT + "/" + BrotherQLModel.QL_820NWB.name));
        BrotherQLConnection connection = new BrotherQLConnection(device);
        connection.open();
        connection.sendJob(job);
        connection.close();
        stopServer();

        LOGGER.log(INFO, "Rx:\n" + dumpHex(receivedData.toString()));
        
        assertEquals(raster, receivedData.toString());
    }

    private void startServer() throws IOException {
        receivedData.delete(0, receivedData.length());
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

    public static String dumpHex(final String text) {
        StringBuilder sb = new StringBuilder();
        
        for (int start = 0; start < text.length(); start += 32) {
            sb.append(String.format("%08X:  ", start / 2));
            String hex1 = text.substring(start, Math.min(text.length(), start + 16));
            sb.append(hex1);
            sb.append(" ");
            if (start + 16 < text.length()) {
                String hex2 = text.substring(start + 16, Math.min(text.length(), start + 32));
                sb.append(hex2);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
