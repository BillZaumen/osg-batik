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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
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

    /*
     * We measure the height and width in points while
     * Batik does not provide units so the default is pixels.
     * This class fixes up the SVG element by adding units to the
     * width and height attributes anb by adding a viewBox.
     */
    static class SVGFilter extends FilterOutputStream {
	StringBuilder sb = new StringBuilder();
	int gtcnt = 0;

	public SVGFilter(OutputStream os) {
	    super(os);
	}

	@Override
	public void close()  throws IOException {
	    super.close();
	}

	@Override
	public void flush()  throws IOException {
	    if (gtcnt == 3) {
		super.flush();
	    }
	}

	@Override
	public void write(byte[] b)  throws IOException {
	    for (int i = 0; i < b.length; i++) {
		if (gtcnt == 3) {
		    super.write(b, i, b.length);
		    return;
		} else {
		    handleChar((int) b[i]);
		}
	    }
	}

	@Override
	public void write(byte[] b, int off, int len)  throws IOException {
	    for (int i = off; i < len; i++) {
		if (gtcnt == 3) {
		    super.write(b, i, len);
		    return;
		} else {
		    len--;
		    handleChar((int) b[i]);
		}
	    }
	}

	@Override
	public void write(int b) throws IOException {
	    if (gtcnt == 3) {
		super.write(b);
	    } else {
		handleChar(b);
	    }
	}

	void handleChar(int b) throws IOException {
	    if (b == '>') gtcnt++;
	    sb.append((char) b);
	    if (gtcnt == 3) {
		processStringBuffer();
	    }
	}

	private static final Pattern pattern = Pattern
	    .compile("\\swidth=\"|\\sheight=\"|>");

	void processStringBuffer() throws IOException {
	    Matcher matcher = pattern.matcher(sb);
	    int wloc = -1, hloc = -1, gtloc = -1;
	    int wlocEnd = -1, hlocEnd = -1;
	    while (matcher.find()) {
		int sindex = matcher.start();
		int eindex = matcher.end();
		String matched = sb.substring(sindex, eindex);
		if (matched.startsWith(">")) {
		    gtloc = sindex;
		} else {
		    matched = matched.substring(1);
		    if (matched.startsWith("width")) {
			wloc = eindex;
			wlocEnd = wloc;
			while (Character.isDigit(sb.charAt(wlocEnd))) {
			    wlocEnd++;
			}
		    } else if (matched.startsWith("height")) {
			hloc = eindex;
			hlocEnd = hloc;
			while (Character.isDigit(sb.charAt(hlocEnd))) {
			    hlocEnd++;
			}
		    }
		}
	    }
	    if (wloc != -1 && hloc != -1 && gtloc != -1) {
		if (sb.charAt(wlocEnd) == '"' && sb.charAt(hlocEnd) == '"') {
		    // found the digits with nothing after them before the
		    // closing quote.
		    String width = sb.substring(wloc, wlocEnd);
		    String height = sb.substring(hloc, hlocEnd);
		    sb.insert(gtloc, " viewBox=\"0 0 " + width + " " + height
			      + "\"");
		    if (wloc < hloc) {
			sb.insert(hlocEnd, "pt");
			sb.insert(wlocEnd, "pt");
		    } else {
			sb.insert(wlocEnd, "pt");
			sb.insert(hlocEnd, "pt");
		    }
		}
	    }
	    sb.chars().forEachOrdered(val -> {
		    try {
			super.write(val);
		    } catch (IOException e) {
		    }
		});
	    sb = null;
	}
    }


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
	    OutputStream fos = new SVGFilter(gzos);
	    Writer out = new OutputStreamWriter(fos, "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    out.flush();
	    fos.flush();
	    gzos.finish();
	} else {
	    OutputStream fos = new SVGFilter(os);
	    Writer out = new OutputStreamWriter(fos, "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    out.flush();
	    fos.flush();
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
