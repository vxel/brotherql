package org.delaunois.brotherql;

import org.delaunois.brotherql.backend.BrotherQLDeviceSimulator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Example of printing a label
 *
 * @author Cedric de Launois
 */
public class PrintExample {
    
    private static final System.Logger LOGGER = System.getLogger(PrintExample.class.getName());

    public static void main(String[] args) throws IOException {
        // Create or load an image
        InputStream is = PrintExample.class.getResourceAsStream("/david.png");
        if (is == null) {
            LOGGER.log(System.Logger.Level.INFO, "Resource not found");
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
        
        // Print the job
        BrotherQLConnection connection = new BrotherQLConnection(new BrotherQLDeviceSimulator(
                BrotherQLPrinterId.QL_700_P,
                BrotherQLMedia.CT_62_720
        ));

        try {
            // Open the connection with the printer
            connection.open();
            
            // Check the printer state (optional)
            BrotherQLStatus status = connection.requestDeviceStatus();
            if (status.getStatusType() != BrotherQLStatusType.READY) {
                // Printer is not ready !
                LOGGER.log(System.Logger.Level.INFO, "Printer is not ready : {0}", status);
                return;
            }
                
            // Note : this checks the printer state before printing
            connection.sendJob(job, (pageNumber, s) -> {
                LOGGER.log(System.Logger.Level.INFO, "Label {0} succesfully printed ! Status is {1}", pageNumber, s);
                // Called when a page is printed.
                // Return true to continue with next page, false otherwise
                return true;
            });
            
            status = connection.requestDeviceStatus();
            LOGGER.log(System.Logger.Level.INFO, "Printer status is {0}", status);
            
        } catch (BrotherQLException e) {
            // Error while printing, See e.getMessage()
            LOGGER.log(System.Logger.Level.WARNING, e.getMessage());
            
        } finally {
            connection.close();
        }
    }
    
}
