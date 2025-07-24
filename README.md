# Java library for printing labels using the raster language protocol on Brother QL printers

A Java library for printing labels on Brother QL series label printers.

This package implements the raster language protocol and allows you to control these printers
and receive status (for USB printers only).
No printer driver is required since this package communicates directly with the label printer.
It allows to bypass difficulties encountered by many drivers and generic printing API
to set the page sizes and margins, in particular for endless labels.
   
The following printers should be supported: QL-500, QL-550, QL-560, QL-570, QL-580N, QL-600, QL-650TD, 
QL-700, QL-700M, QL-710W, QL-720NW, QL-800, QL-810W, QL-820NWB, QL-1050, QL-1060N, QL-1100, QL-1110NWB, QL-1115NWB.
                     
The code has been developed and tested on a Brother QL-700 printer, on Linux.
Please report if you successfully tested the library with another printer, or if you encountered any problem.
Support for network printers is experimental as I do not have such printer.
Two-color (black & red) printing is still experimental.

Options supported (see BrotherQLJob javadoc for more details):
- **autocut**: whether to automatically cut the label (default is true)
- **cutEach**: the number of labels after which a cut is applied (default is 1)
- **feedAmount**: the feed amount. Note that in case of using QL-550/560/570/580N/700, 35 dots is always used, and 0 for Die-cut labels.
- **delay**: delay in millis between prints
- **dither**: whether to apply a Floyd-Steinberg dithering during conversion to black/red and white (default is true)
- **threshold**: the threshold value (between 0 and 1) to discriminate between black/red and white pixels, based on pixel luminance. 
  Lower threshold means less printed dots, i.e. a brighter image. 
  Meaningless if dither is true
- **brightness**: Brightness factor applied before dithering. Higher means brighter.
- **rotate**: rotate the image (clock-wise) by this angle in degrees. Accepted angles are multiple of 90 degrees (90, 180, 270).
- **dpi600**: use 600 dpi height x 300 dpi wide resolution. Only available on some models. The image must be provided as 600x600 dpi. The width will be resized to 300dpi.
- **media**: the label size and type. Required only for network printer. Automatically detected for USB printers.

## Maven dependency

The package is available from [Maven Central](https://central.sonatype.com) and
can be used with the following dependency :
```
<dependency>
    <groupId>org.delaunois</groupId>
    <artifactId>brotherql</artifactId>
    <version>1.3.0</version>
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

    // Required only for Network printers.
    // For USB printers, the media is automatically detected and given by the printer status.
    job.setMedia(BrotherQLMedia.CT_62_720);
    
    // Print the job, using the first detected USB Brother printer
    // For a network printer, use: new BrotherQLConnection("tcp://host:post/model")
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

For network printers, use an identifier like `tcp://localhost:9100/QL-720NW`, where `localhost` is the IP address 
or hostname of the printer, `9100` is the port (`9100` is the default port), and `QL-720NW` is the name of 
the printer model (see `BrotherQLModel` enum class).

## Linux UDEV Configuration

Please note that the package needs Read/Write access to USB printer devices.
It might be needed to add specific UDEV rules to add these rights.

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
               
White dove test image is from Mali Maeder.
