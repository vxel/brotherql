package org.delaunois.brotherql.example;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.BrotherQLModel;
import org.delaunois.brotherql.BrotherQLStatus;
import org.delaunois.brotherql.BrotherQLStatusType;
import org.delaunois.brotherql.backend.BrotherQLDeviceSimulator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.List;

/**
 * Example of printing a label
 *
 * @author Cedric de Launois
 */
public class PrintExample {
    
    private static final System.Logger LOGGER = System.getLogger(PrintExample.class.getName());

    public static void main(String[] args) throws IOException, BrotherQLException {
        // Create or load an image
        InputStream is = PrintExample.class.getResourceAsStream("/david.png");
        if (is == null) {
            LOGGER.log(Level.INFO, "Resource not found");
            return;
        }
        BufferedImage img = ImageIO.read(is);
        
        // Create a print job
        BrotherQLJob job = new BrotherQLJob()
                .setAutocut(false)
                .setDither(true)
                .setBrightness(1.8f)
                .setDelay(3000)
                .setImages(List.of(img));
        
        // List available printers
        List<String> devices = BrotherQLConnection.listDevices();
        LOGGER.log(Level.INFO, devices);
        
        // Simulate a job print
        BrotherQLConnection connection = new BrotherQLConnection(new BrotherQLDeviceSimulator(
                BrotherQLModel.QL_700_P,
                BrotherQLMedia.CT_62_720
        ));
        
        // Or actually print it ! with BrotherQLConnection connection = new BrotherQLConnection(devices.get(0));

        try (connection) {
            // Open the connection with the printer
            connection.open();

            LOGGER.log(Level.INFO, "Printer: " + connection.getModel().toString());
            
            // Check the printer state (optional)
            BrotherQLStatus status = connection.requestDeviceStatus();
            if (status.getStatusType() != BrotherQLStatusType.READY) {
                // Printer is not ready !
                LOGGER.log(Level.INFO, "Printer is not ready : {0}", status);
                return;
            }

            // Note : this checks the printer state before printing
            connection.sendJob(job, (pageNumber, s) -> {
                LOGGER.log(Level.INFO, "Label {0} succesfully printed ! Status is {1}", pageNumber, s);
                // Called when a page is printed.
                // Return true to continue with next page, false otherwise
                return true;
            });

            status = connection.requestDeviceStatus();
            LOGGER.log(Level.INFO, "Printer status is {0}", status);

        } catch (BrotherQLException e) {
            // Error while printing, See e.getMessage()
            LOGGER.log(Level.WARNING, e.getMessage());

        }
    }
    
}
