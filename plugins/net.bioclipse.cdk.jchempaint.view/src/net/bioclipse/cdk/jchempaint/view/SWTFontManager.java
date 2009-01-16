package net.bioclipse.cdk.jchempaint.view;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.renderer.AbstractFontManager;


public class SWTFontManager extends AbstractFontManager {

	public static final String FONT_FAMILY_NAME = "Arial";

	private HashMap<Integer, Font> fontSizeToFontMap;

	private int minFontSize;

	private Font currentFont;

	private double scale;

	public SWTFontManager(Device device) {
		// apparently 9 pixels per em is the minimum
		// but I don't know if (size 9 == 9 px.em-1)...
		this.minFontSize = 1;

		this.makeFonts(device);

		this.toMiddle();
		this.resetVirtualCounts();
	}

	private void makeFonts(Device device) {
		int size = this.minFontSize;
		double scale = 12;
		this.fontSizeToFontMap = new HashMap<Integer, Font>();

//		for (int i = 0; i < this.getNumberOfFontSizes(); i++) {
		for (int i = 0; i < 20; i++) {
			this.fontSizeToFontMap.put(size,
					new Font(device, SWTFontManager.FONT_FAMILY_NAME, size,SWT.NONE));
			this.registerFontSizeMapping(scale, size);
			size += 1;
			scale += 2.3;
		}
	}

	@Override
	public void setFontForScale(double scale) {
	  this.scale = scale;
		int size = this.getFontSizeForScale(scale);
		if (size != -1) {
			this.currentFont = this.fontSizeToFontMap.get(size);
		}
	}

	public Font getSmallFont() {
	    int size = this.getFontSizeForScale( scale*.5 );
	    if(size != -1) {
	        return this.fontSizeToFontMap.get(size);
	    }
	    return null;
	}

	public Font getFont() {
		return currentFont;
	}

	public void dispose() {

	}

	public static class FontTest {
	    public static void main(String [] args) {
	        final Display display = new Display();
	        final Shell shell = new Shell(display);

	        final SWTFontManager fontManager = new SWTFontManager(display);


	        shell.addPaintListener(new PaintListener() {
	          public void paintControl(PaintEvent event) {
	              Rectangle rect = shell.getClientArea();
	            for(int y =0 ;y<rect.height;) {
	                fontManager.setFontForScale( rect.height-y+1 );

	                Font font = fontManager.getFont();
	                event.gc.setFont( font );
	                event.gc.drawText( "C", rect.width/2, y );
	                y+=event.gc.getFontMetrics().getHeight();
	            }


	          }
	        });
	        shell.setBounds(10, 30, 200, 500);
	        shell.open ();
	        while (!shell.isDisposed()) {
	          if (!display.readAndDispatch()) display.sleep();
	        }
	        display.dispose();
	      }

	}
}
