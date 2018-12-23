/**
 * Service-provider module for output-stream graphics
 *
 */
module org.bzdev.osgbatik {
    exports org.bzdev.providers.osgbatik;
    exports org.bzdev.providers.osgbatik.lpack;
    exports org.apache.batik.ext.awt.image.spi;
    opens org.bzdev.providers.osgbatik.lpack;
    requires java.base;
    requires java.desktop;
    requires java.xml;
    // jdk.xml.dom is needed by the Apache Batik code because
    // we filtered its packages out to avoid module conflicts
    requires jdk.xml.dom; 
    requires org.bzdev.base;
    requires org.bzdev.desktop;
    /*
      These are the automatic modules we would have used, but
      can't because packages in the Apache code are split up
      across multiple jar files.
    requires batik.svggen;
    requires batik.dom;
    requires batik.svg.dom;
    requires batik.constants;
    requires batik.util;
    requires batik.ext;
    requires batik.awt.util;
    requires batik.i18n;
    requires batik.xml;
    requires batik.codec;
    */
    provides org.bzdev.gio.spi.OSGProvider with
	org.bzdev.providers.osgbatik.BatikGraphicsProvider,
	org.bzdev.providers.osgbatik.BatikGraphicsZProvider;

    // The following are copied from the META-INF/services files,
    // which contains Apache licensing info. It is not clear from
    // the binaries if any of the batik packages included use these
    // services, but adding the 'uses' statements shouldn't hurt.
    //
    uses org.apache.batik.ext.awt.image.spi.ImageWriter;
    provides org.apache.batik.ext.awt.image.spi.ImageWriter with
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOPNGImageWriter,
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOTIFFImageWriter,
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOJPEGImageWriter;
    //
    uses org.apache.batik.ext.awt.image.spi.RegistryEntry;
    provides org.apache.batik.ext.awt.image.spi.RegistryEntry with
	org.apache.batik.ext.awt.image.codec.png.PNGRegistryEntry,
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOJPEGRegistryEntry,
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOPNGRegistryEntry,
	org.apache.batik.ext.awt.image.codec.imageio.ImageIOTIFFRegistryEntry;
}
