import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.SvgOps;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileOutputStream;
public class Test2 {

    public static void main(String argv[]) throws Exception {

	FileOutputStream fos = new FileOutputStream("svgmm.svg");


	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(fos, 250, 300, "svg");
	if (osg instanceof SvgOps) {
	    ((SvgOps) osg).setDimensions(500, "mm", 600, "mm");
	}
	Graphics2D g2d = osg.createGraphics();
	g2d.setColor(Color.black);
	g2d.drawRect(10, 10, 230, 280);
	osg.imageComplete();
	fos.close();
    }
}
