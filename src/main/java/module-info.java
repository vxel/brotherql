module brotherql {
    requires static lombok;
    requires usb4java;
    requires java.desktop;
    exports org.delaunois.brotherql;
    exports org.delaunois.brotherql.backend;
    exports org.delaunois.brotherql.util;
}