package org.delaunois.brotherql.backend;

import lombok.Getter;
import lombok.Setter;
import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLMediaType;
import org.delaunois.brotherql.BrotherQLModel;
import org.delaunois.brotherql.BrotherQLPhaseType;
import org.delaunois.brotherql.BrotherQLStatusType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;

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

    private static final byte[] READY = new byte[]{
            (byte)0x80, 0x20, 0x42, 0, 0, 0, 0, 0,
            0, 0, 0, BrotherQLMediaType.UNKNOWN.code, 0, 0, 0, 0,
            0, 0, BrotherQLStatusType.READY.code, BrotherQLPhaseType.WAITING_TO_RECEIVE.code, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private final URI uri;
    private Socket socket = null;
    
    @Getter
    @Setter
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    
    @Getter
    @Setter
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    
    @Getter
    private BrotherQLModel model;

    /**
     * Construct the backend for a network Brother printer identified by the given URI.
     * Default port is 9100.
     *
     * @param uri the URI identifier for the network printer : a string like tcp://host:port/model
     */
    public BrotherQLDeviceTcp(URI uri) {
        this.uri = uri;
        if (uri == null) {
            throw new IllegalArgumentException("Device URI is required");
        }
        
        if (!"tcp".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Only tcp scheme is supported for network devices");
        }
        
        if (uri.getPath() == null || uri.getPath().isEmpty()) {
            throw new IllegalArgumentException("Device model is required for network devices");
        }
        
        model = BrotherQLModel.fromModelName(uri.getPath().substring(1));
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
    public ByteBuffer readStatus(long timeout) {
        return ByteBuffer.wrap(READY);
    }

    @Override
    public void write(byte[] data, long timeout) throws BrotherQLException {
        if (socket == null) {
            throw new IllegalStateException("Device is not open");
        }
        
        try {
            OutputStream out = socket.getOutputStream(); 
            if (data.length > 0) {
                DataOutputStream dos = new DataOutputStream(out);
                dos.write(data, 0, data.length);
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
        model = null;
    }
    
    @Override
    public boolean isUsbPrinter() {
        return false;
    }
    
}
