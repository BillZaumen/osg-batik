/**
 * Service-provider module for output-stream graphics
 *
 */
module org.bzdev.osgbatik {
    exports org.bzdev.providers.osgbatik;
    exports org.bzdev.providers.osgbatik.lpack;
    opens org.bzdev.providers.osgbatik.lpack;
    requires java.base;
    requires java.desktop;
    requires java.xml;
    requires org.bzdev.base;
    requires org.bzdev.desktop;
    requires batik.osgmod;
    /*
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
}
