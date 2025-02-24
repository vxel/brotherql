# Java library for printing labels using the raster language protocol on Brother QL printers

A Java library for printing labels via USB on Brother QL series label printers.

This package implements the raster language protocol and allows you to control these printers
and receive status via USB connection.
No printer driver is required since this package communicates directly with the label printer.
It allows to bypass difficulties encountered by many drivers and generic printing API
to set the page sizes and margins, in particular for endless labels.
   
The following printers should be supported:
- Brother QL-500
- Brother QL-550
- Brother QL-560
- Brother QL-570
- Brother QL-580N
- Brother QL-650TD
- Brother QL-700
- Brother QL-700M
- Brother QL-1050
- Brother QL-1060N
                     
The code has been developed and tested on a Brother QL-700 printer, on Linux.

Options supported (see BrotherQLJob javadoc for more details):
- **autocut**: whether to automatically cut the label (default is true)
- **cutEach**: the number of labels after which a cut is applied (default is 1)
- **feedAmount**: the feed amount. Note that in case of using QL-550/560/570/580N/700, 35 dots is always used, and 0 for Die-cut labels.
- **delay**: delay in millis between prints
- **dither**: whether to apply a Floyd-Steinberg dithering during conversion to black and white (default is true)
- **threshold**: the threshold value (between 0 and 1) to discriminate between black and white pixels, based on pixel luminance. 
  Lower threshold means less printed dots, i.e. a brighter image. 
  Meaningless if dither is true
- **brightness**: Brightness factor applied before dithering. Higher means brighter.
- **rotate**: rotate the image (clock-wise) by this angle in degrees. Accepted angles are multiple of 90 degrees (90, 180, 270).

## Maven dependency

The package is available from [Maven Central](https://central.sonatype.com) and
can be used with the following dependency :
```
<dependency>
    <groupId>org.delaunois</groupId>
    <artifactId>brotherql</artifactId>
    <version>1.1.1</version>
</dependency>
```

## Code example

Here is a quick example of how to print a label using the library:
```
    // Create or load one or more images
    BufferedImage image = ...;
    
    // Create a print job
    BrotherQLJob job = new BrotherQLJob()
            .setAutocut(true)
            .setDither(true)
            .setDelay(1000)
            .setImages(List.of(image));
    
    // Print the job, using the first detected USB Brother printer
    BrotherQLConnection connection = new BrotherQLConnection();
    try (connection) {
        // Open the connection with the printer
        connection.open();
        
        // Check the printer state (optional)
        BrotherQLStatus status = connection.requestDeviceStatus();
        if (status.getStatusType() != BrotherQLStatusType.READY) {
            // Printer is not ready !
            return;
        }
            
        // Note : this checks the printer state before printing
        connection.sendJob(job, (pageNumber, s) -> {
            // Called when a page is printed.
            // Return true to continue with next page, false otherwise
            return true;
        });
        
    } catch (BrotherQLException e) {
        // Error while printing, See e.getMessage()
        ...        
    }
```
        
The list of available USB printers can be obtained through a call to `BrotherQLConnection.listDevices()`,
that will return a list of printer identifier like `usb://Brother/QL-700?serial=XXXX`, where `QL-700` is the name
of a model (see `BrotherQLModel` enum class), and `?serial=XXXX` is optional and can be used to define the serial number
of the printer to use.
The identifier is to be used as parameter of the `BrotherQLConnection` constructor.


## Linux UDEV Configuration

Please note that the package need Read/Write access to the USB printer device.
It might be needed to add specific UDEV rules to add these right.

Example :

1. Create a new udev rule file :
```
sudo vi /etc/udev/rules.d/50-brotherusb.rules
```
2. Write in this new file the following rule (example) : 
```
SUBSYSTEMS=="usb", ATTRS{idVendor}=="04f9", MODE="0666"`
```
3. Reload the rules :
```
sudo udevadm control --reload
```

## Author

This software package was written by Cédric de Launois based on Brother's documentation.

## Acknowledgement

This package was made thanks to :
- the [libusb library](https://libusb.info/) for the generic access to USB devices
- the [usb4java](http://usb4java.org/) library to access USB devices with Java
- the [Brother QL printers documentation](https://download.brother.com/welcome/docp000678/cv_qlseries_eng_raster_600.pdf)
- the [brother_ql](https://github.com/pklaus/brother_ql) package that implements the same protocol for the Python language
