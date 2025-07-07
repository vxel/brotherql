package org.delaunois.brotherql.backend;

import lombok.Getter;
import lombok.Setter;
import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLModel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;

import static org.delaunois.brotherql.protocol.QL.CMD_STATUS_REQUEST;
import static org.delaunois.brotherql.protocol.QL.STATUS_SIZE;

/**
 * Access layer to TCP/IP Brother QL device printers.
 *
 * @author Cedric de Launois
 */
public class BrotherQLDeviceTcp implements BrotherQLDevice{
    
    private static final System.Logger LOGGER = System.getLogger(BrotherQLDeviceTcp.class.getName());
    
    private static final int DEFAULT_PORT = 9100;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private final URI uri;
    private Socket socket = null;
    
    @Getter
    @Setter
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    
    @Getter
    @Setter
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    /**
     * Construct the backend for a network Brother printer identified by the given URI.
     * Default port is 9100.
     *
     * @param uri the URI identifier for the network printer : a string like tcp://host:port
     */
    public BrotherQLDeviceTcp(URI uri) {
        this.uri = uri;
        if (uri == null) {
            throw new IllegalArgumentException("Device URI is required");
        }
        
        if (!"tcp".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Only tcp scheme is supported for network devices");
        }
    }

    @Override
    public void open() throws BrotherQLException {
        if (socket != null) {
            throw new IllegalStateException("Device is already open");
        }
        
        int port = uri.getPort() > 0 ? uri.getPort() : DEFAULT_PORT;
        try {
          socket = new Socket();
          socket.connect(new InetSocketAddress(uri.getHost(), port), connectTimeout);
        } catch  (IOException e) {
            throw new BrotherQLException(e.getMessage());
        }        
    }

    @Override
    public BrotherQLModel getModel() {
        if (socket == null) {
            return BrotherQLModel.UNKNOWN;
        }

        try {
            write(CMD_STATUS_REQUEST, 0);
            ByteBuffer bb = readStatus(readTimeout);
            if (bb != null) {
                int model = bb.get(4) & 0xFF;
                return BrotherQLModel.fromModelCode(model);
            }
        } catch (BrotherQLException e) {
            LOGGER.log(Level.WARNING, "Error sending status information request", e);
        }

        return BrotherQLModel.UNKNOWN;
    }

    @Override
    public ByteBuffer readStatus(long timeout) {
        if (socket == null) {
            throw new IllegalStateException("Device is not open");
        }

        try {
            socket.setSoTimeout((int) timeout);
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[STATUS_SIZE];
            int read = in.read(buffer);
            if (read > 0) {
                ByteBuffer bb = ByteBuffer.allocate(read);
                bb.put(buffer, 0, read);
                bb.rewind();
                return bb;
            } else {
                LOGGER.log(Level.WARNING, "No data received from printer");
            }
        } catch (IOException e) {
            // Ignore
        }
        return null;
    }

    @Override
    public void write(byte[] data, long timeout) throws BrotherQLException {
        if (socket == null) {
            throw new IllegalStateException("Device is not open");
        }
        
        try {
            OutputStream out = socket.getOutputStream(); 
            DataOutputStream dos = new DataOutputStream(out);
            if (data.length > 0) {
                dos.write(data, 0, data.length);
                dos.flush();
            }
        } catch (IOException e) {
            throw new BrotherQLException(e.getMessage());
        }
    }
    
    @Override
    public boolean isClosed() {
        return socket == null;
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing socket", e);
            }
            socket = null;
        }
    }
    
    @Override
    public boolean isUsbPrinter() {
        return false;
    }
    
}
