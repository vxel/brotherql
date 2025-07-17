package org.delaunois.brotherql.backend;

import lombok.Getter;
import lombok.Setter;
import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A dummy implementation of BrotherQLDevice interface
 *
 * @author Cedric de Launois
 */
@SuppressWarnings("unused")
public class BrotherQLDeviceFile implements BrotherQLDevice {

    private static final Logger LOGGER = System.getLogger(BrotherQLDeviceFile.class.getName());

    private static final BrotherQLModel DEFAULT_MODEL = BrotherQLModel.QL_500;

    private byte[] status = new byte[]{
            (byte) 0x80, 0x20, 0x42, 0x34, 0x35, 0x30, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private final Path path;

    @Getter
    @Setter
    private BrotherQLModel model;

    private ByteArrayOutputStream bos = null;

    private boolean open = false;

    public BrotherQLDeviceFile(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Device URI is required");
        }

        if (!"file".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Only file scheme is supported for printing to file");
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            if (uri.getAuthority() != null && !uri.getAuthority().isEmpty()) {
                // A non-regular relative path
                this.path = Path.of(uri.getAuthority() + uri.getPath() );
            } else {
                throw new IllegalArgumentException("Device path is required for printing to file");
            }
            
        } else {
            this.path = Path.of(uri.getPath());
        }
        
        this.model = getModelFromUri(uri);
    }

    private BrotherQLModel getModelFromUri(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return DEFAULT_MODEL;
        }

        String[] queryParts = uri.getQuery().split("=");
        if (queryParts.length < 2 || !queryParts[0].equals("model")) {
            return DEFAULT_MODEL;
        }
        return BrotherQLModel.fromModelName(queryParts[1].substring(1));
    }

    @Override
    public void open() {
        open = true;
        bos = new ByteArrayOutputStream();
    }

    @Override
    public ByteBuffer readStatus(long timeout) {
        return ByteBuffer.wrap(status);
    }

    @Override
    public void write(byte[] data, long timeout) throws BrotherQLException {
        if (!open) {
            throw new IllegalStateException("Device is not open");
        }

        try {
            bos.write(data);
        } catch (IOException e) {
            throw new BrotherQLException(e.getMessage());
        }
    }

    @Override
    public boolean isClosed() {
        return !open;
    }

    @Override
    public void close() {
        open = false;
        if (bos != null) {
            try {
                Files.write(path, bos.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                bos.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing file", e);
            }
        }
        bos = null;
    }

    @Override
    public boolean isUsbPrinter() {
        return false;
    }
}
