package org.bzdev.providers.osgbatik;
import java.util.HashSet;
import org.bzdev.gio.spi.OSGProvider;
import org.bzdev.lang.ClassFinder;

/**
 * Provider for SVG images using the Apache Batik implementation.
 */
public class BatikGraphicsZProvider implements OSGProvider {

    static String types[] = {"svgz", "SVGZ", "svgz-batik"};
    static String suffixes[] = {"svgz", "SVGZ"};
    static String mimeType = "image/svg+xml";
    static Class<BatikGraphics> clazz = BatikGraphics.class;

    static HashSet<String> tset = new HashSet<>();
    static final String BATIK_RESOURCE =
	"org/apache/batik/svggen/SVGGraphics2D.class";
    static final String BATIK_CLASS =
	"org.apache.batik.svggen.SVGGraphics2D";

    static {
	// Test that the Batik libary is on the class path.

	if (!ClassFinder.classExists(BATIK_CLASS)) {
	    types = new String[0];
	    suffixes = new String[0];
	    mimeType = null;
	    clazz = null;
	}
	/*
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
		public Void run() {
		    if (BatikGraphicsProvider.class.getClassLoader()
			.getResource(BATIK_RESOURCE)
			== null) {
			types = new String[0];
			suffixes = new String[0];
			mimeType = null;
			clazz = null;
		    }
		    return (Void) null;
		}
	    });
	*/
	for (String s: types) {
	    tset.add(s);
	}
    }

    public String[] getTypes() {
	return types.clone();
    }

    public String[] getSuffixes(String type) {
	return tset.contains(type)? suffixes.clone(): null;
    }

    public String getMediaType(String type) {
	return tset.contains(type)? mimeType: null;
    }

    public Class<BatikGraphics> getOsgClass() {
	return clazz;
    }
}
