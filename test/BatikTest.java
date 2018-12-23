import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

/**
 * Stand-alone test of Batik.
 * This was copied from
 * https://xmlgraphics.apache.org/batik/using/svg-generator.html
 * and modified slightly - to spcify an optional output file on the command
 * line.  The goal is to verify that Batik is working and that the
 * class path we use is correct.
 */

public class BatikTest {

    public static void paint(Graphics2D g2d) throws IOException {
	g2d.setPaint(Color.red);
	g2d.fill(new Rectangle(10, 10, 100, 100));
	Image img = ImageIO.read(new File("aleft.png"));
	g2d.drawImage(img, 20, 20, null);
	
    }

    public static void main(String[] argv) throws IOException {

	// Get a DOMImplementation.
	DOMImplementation domImpl =
	    GenericDOMImplementation.getDOMImplementation();

	// Create an instance of org.w3c.dom.Document.
	String svgNS = "http://www.w3.org/2000/svg";
	Document document = domImpl.createDocument(svgNS, "svg", null);

	// Create an instance of the SVG Generator.
	SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

	// Ask the test to render into the SVG Graphics2D implementation.
	/*
	  -- made paint a static method to avoid supriously creating a class
	  TestSVGGen test = new TestSVGGen();
	  test.paint(svgGenerator);
	*/
	paint(svgGenerator);

	// Finally, stream out SVG to the standard output using
	// UTF-8 encoding.
	boolean useCSS = true; // we want to use CSS style attributes
    
	Writer out = (argv.length == 0)?
	    new OutputStreamWriter(System.out, "UTF-8"):
	    new OutputStreamWriter(new FileOutputStream(argv[0]), "UTF-8");
	svgGenerator.stream(out, useCSS);
    }
}
