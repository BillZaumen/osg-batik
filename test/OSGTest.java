import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageOrientation;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

public class OSGTest {
    static public void main(String argv[]) {
	try {
	    if (argv.length == 0) {
		System.setSecurityManager(new SecurityManager());
		System.out.println("preferred image types:");
		for (String type: OutputStreamGraphics.getImageTypes()) {
		    System.out.println("    " + type + ", aliases:");
		    String mediaType =
			OutputStreamGraphics.getMediaTypeForImageType(type);
		    String type2 =
			OutputStreamGraphics
			.getImageTypeForMediaType(mediaType);
		    if (!type.equals(type2)) {
			    throw new Exception (type  + " != " + type2);
		    }
		    for (String atype: OutputStreamGraphics
			     .getAliasesForImageType(type)) {
			System.out.println("\t" + atype);
		    }
		}
		for (String type: OutputStreamGraphics.getAllImageTypes()) {
		    String mediaType =
			OutputStreamGraphics.getMediaTypeForImageType(type);
		    System.out.println
			(type + ", preferred extension = "
			 + OutputStreamGraphics.getSuffixForImageType(type)
			 + ", media type = " + mediaType);
		    System.out.println
			("default width and height for " + type +": "
			 + OutputStreamGraphics.getDefaultWidth
			 (type, ImageOrientation.NORMAL) + ", "
			 + OutputStreamGraphics.getDefaultHeight
			 (type, ImageOrientation.NORMAL));
		    System.out.println("Suffixes for " + type + ":");
		    for (String suffix: OutputStreamGraphics
			     .getSuffixesForImageType(type)) {
			System.out.println("    " + suffix);
		    }
		}
		System.out.println("media-type list:");
		for (String type: OutputStreamGraphics.getMediaTypes()) {
		    System.out.println("    " + type);
		    for (String itype: OutputStreamGraphics
			     .getAliasesForMediaType(type)) {
			System.out.println("\t" + itype);
		    }
		}
		System.out.println("Suffix List:");
		for (String suffix: OutputStreamGraphics.getSuffixes()) {
		    System.out.println(suffix + ": image type = "
				       + OutputStreamGraphics
				       .getImageTypeForSuffix(suffix));
				       
		}


		System.out.println("static method tests:");
		System.out.println(" type for foo.ps: "
				   + OutputStreamGraphics
				   .getImageTypeForFile("foo.ps"));
		System.out.println(" type for foo.jpg: "
				   + OutputStreamGraphics
				   .getImageTypeForFile("foo.jpg"));
		System.out.println(" type for File foo.ps: "
				   + OutputStreamGraphics
				   .getImageTypeForFile(new File("foo.ps")));
		System.out.println(" type for File foo.jpg: "
				   + OutputStreamGraphics
				   .getImageTypeForFile(new File("foo.jpg")));

		System.out.println(" type for extension \"ps\": "
				   + OutputStreamGraphics
				   .getImageTypeForSuffix("ps"));
		System.out.println(" type for File extension \"jpg\": "
				   + OutputStreamGraphics
				   .getImageTypeForSuffix("jpg"));
		System.exit(0);
	    }

	    ImageOrientation orientation = null;
	    if (argv.length >= 2) {
		orientation = ImageOrientation.valueOf(argv[1]);
	    }
	    String fn = ((orientation == null)? "out.": "out-" + argv[1] + ".")
		+ argv[0];
	    FileOutputStream fos = new FileOutputStream(fn);
	    /*
	    System.out.println
		("number of image types = "
		 + OutputStreamGraphics.getAllImageTypes().length);
	    */
	    System.setSecurityManager(new SecurityManager());
	    OutputStreamGraphics osg = (orientation == null)?
		OutputStreamGraphics.newInstance(fos, 800, 600, argv[0]):
		OutputStreamGraphics.newInstance(fos, 800, 600, orientation,
						 argv[0]);
	    Graphics2D g2d = osg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	    String text= "example string\u00b9\u2074";
	    g2d.drawString(text, 250, 250);
	    g2d.fillRect(0, 0, 200, 200);
	    g2d.setColor(Color.black);
	    g2d.drawRect(2, 596, 796, 2);
	    g2d.fillRect(2, 596, 796, 2);
	    g2d.drawRect(796, 0, 2, 596);
	    g2d.fillRect(796, 0, 2, 596);

	    FontMetrics fm = g2d.getFontMetrics();
	    Rectangle2D bounds = fm.getStringBounds(text, g2d);
	    double width1 = bounds.getWidth();
	    double x = 250 + bounds.getWidth();
	    double y = 250 + bounds.getHeight();
	    g2d.rotate(Math.PI, x, y);
	    g2d.drawString(text, (int)Math.round(x), (int)Math.round(y));
	    fm = g2d.getFontMetrics();
	    bounds = fm.getStringBounds(text, g2d);
	    double width2 = bounds.getWidth();
	    System.out.println("width ratio = " + (width2/width1));
	    osg.imageComplete();
	    fos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
