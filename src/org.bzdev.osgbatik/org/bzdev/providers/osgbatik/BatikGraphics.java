package org.bzdev.providers.osgbatik;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageOrientation;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.color.ColorSpace;
import java.awt.Dimension;
import java.awt.image.ColorModel;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;

import org.apache.batik.svggen.*;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
// import java.security.AccessController;
// import java.security.PrivilegedAction;

//@exbundle org.bzdev.providers.osgbatik.lpack.OSG


/**
 * OutputStreamGraphics implementation for SVG using the
 * Apache Batik software.
 * <P>
 * Note: Java 11 is significantly more restrictive on access to
 * resources than Java 8 and earlier. If a security manager is
 * installed, appropriate permissions must be granted so that
 * this class can load the resource bundle
 * org.bzdev.providers.osgbatik.lpack.OSG.
 */
public class BatikGraphics extends OutputStreamGraphics {

    private static ResourceBundle exbundle =
	ResourceBundle.getBundle("org.bzdev.providers.osgbatik.lpack.OSG");

    /*
    static {
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
		public Void run() {
		    exbundle = ResourceBundle.getBundle
			("org.bzdev.providers.osgbatik.lpack.OSG");
		    return (Void) null;
		}
	    });
    }
    */
    static String errorMsg(String key, Object... args) {
	// We can user Formatter rather than SafeFormatter
	// because the format directives are all %s ones.
	if (exbundle == null) {
	    String result = key + ":";
	    String sep = " ";
	    for (Object arg: args) {
		result = result + sep + arg.toString();
		sep = ", ";
	    }
	    return result;
	} else {
	    return (new Formatter()).format(exbundle.getString(key), args)
		.toString();
	}
    }

    Document document;
    SVGGraphics2D svgGenerator;
    boolean useCSS = true;
    boolean compress = false;
 
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    /**
     * Get the default width.
     * @param orientation the image orientation
     * @return the width in units of points
     */
    public static Integer getDefaultWidth(ImageOrientation orientation) {
	return DEFAULT_WIDTH;
    }

    /**
     * Get the default height.
     * @param orientation the image orientation
     * @return the height in units of points
     */
    public static Integer getDefaultHeight(ImageOrientation orientation) {
	return DEFAULT_HEIGHT;
    }

    @Override
    public Graphics2D createGraphics() {
	Graphics g = svgGenerator.create();
	if (g instanceof Graphics2D) {
	    return (Graphics2D) g;
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    @Override
    public ColorModel getColorModel() {
	return ColorModel.getRGBdefault();
    }

    boolean done = false;

    @Override
    public void imageComplete() throws IOException {
	if (done) throw new IOException(errorMsg("imageComplete"));
	done = true;
	OutputStream os = getOutputStream();
	if (compress) {
	    GZIPOutputStream gzos = new GZIPOutputStream(os);
	    Writer out = new OutputStreamWriter(gzos, "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    out.flush();
	    gzos.finish();
	} else {
	    Writer out = new OutputStreamWriter(os, "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    out.flush();
	}
	os.flush();
	svgGenerator.dispose();
    }

    /**
     * Constructor.
     * @param os the output stream
     * @param width the image width (used by Postscript only for
     *        determining an aspect ratio, as the image will be scaled
     *        to fit on a page)
     * @param height the image height (used by Postscript only for
     *        determining an aspect ratio, as the image will be scaled
     *        to fit on a page)
     * @param orientation the image orientation
     * @param type a string naming an image type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     */
    public BatikGraphics(OutputStream os, int width, int height,
			 ImageOrientation orientation,
			 String type, boolean preferAlpha) {
	super(os, width, height, orientation, type, preferAlpha);
	if (type.startsWith("svgz") || type.startsWith("SVGZ")) {
	    compress = true;
	}
	DOMImplementation domImpl =
	    GenericDOMImplementation.getDOMImplementation();

	String svgNS = "http://www.w3.org/2000/svg";
	document = domImpl.createDocument(svgNS, "svg", null);
	SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
	ImageHandler ihandler  = new ImageHandlerBase64Encoder() {
		public void handleImage(Image image, Element element,
					SVGGeneratorContext ctx) {
		    // workaround for Batik-1.8 bug
		    if (image == null || element == null
			|| ctx == null) throw new NullPointerException();
		    try {
			super.handleImage(image, element, ctx);
		    } catch (NullPointerException e) {};
		}
		public void handleImage(RenderableImage image, Element element,
					SVGGeneratorContext ctx) {
		    // workaround for Batik-1.8 bug
		    if (image == null || element == null
			|| ctx == null) throw new NullPointerException();
		    try {
			super.handleImage(image, element, ctx);
		    } catch (NullPointerException e) {};
		}
		public void handleImage(RenderedImage image, Element element,
					SVGGeneratorContext ctx) {
		    // workaround for Batik-1.8 bug
		    if (image == null || element == null
			|| ctx == null) throw new NullPointerException();
		    try {
			super.handleImage(image, element, ctx);
		    } catch (NullPointerException e) {};
		}
	    };
	ctx.setImageHandler(ihandler);
	// svgGenerator = new SVGGraphics2D(document);
	svgGenerator = new SVGGraphics2D(ctx, false);
	switch(orientation) {
	case COUNTERCLOCKWISE90:
	    svgGenerator.setSVGCanvasSize(new Dimension(height, width));
	    break;
	case CLOCKWISE90:
	    svgGenerator.setSVGCanvasSize(new Dimension(height, width));
	    break;
	case NORMAL:
	default:
	    svgGenerator.setSVGCanvasSize(new Dimension(width, height));
	    break;
	}
	setupGraphicsForImages(svgGenerator);
	applyInitialTransform(svgGenerator);
    }
}
