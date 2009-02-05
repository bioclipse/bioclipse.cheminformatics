package net.bioclipse.cdk.jchempaint.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.renderer.font.AbstractFontManager;



public class SWTFontManager extends AbstractFontManager {

	public String FONT_FAMILY_NAME = "Arial";
	public int    FONT_STYLE = SWT.NONE;

	private HashMap<Integer, Font> fontSizeToFontMap;

	private int minFontSize;

	private Font currentFont;

	private double scale;

	private Device device;

	public SWTFontManager(Device device,java.awt.Font font) {
		// apparently 9 pixels per em is the minimum
		// but I don't know if (size 9 == 9 px.em-1)...
		this.minFontSize = 9;
		setFont( font );
		this.device = device;
		this.makeFonts();

		this.toMiddle();
		this.resetVirtualCounts();
	}

	public void setFont(java.awt.Font font) {
	    FONT_FAMILY_NAME = font.getFamily();
	    switch (font.getStyle()) {
	        case java.awt.Font.PLAIN: FONT_STYLE = SWT.NONE;break;
	        case java.awt.Font.BOLD: FONT_STYLE = SWT.BOLD;break;
	        case java.awt.Font.ITALIC: FONT_STYLE = SWT.BOLD;break;
	        default: FONT_STYLE = SWT.ITALIC | SWT.BOLD;
	    }
	}

	private void makeFontsAWT(Device device) {
	    int size = this.minFontSize;
	    double scale = 0.5;
	    this.fontSizeToFontMap = new HashMap<Integer, Font>();

//	    for (int i = 0; i < this.getNumberOfFontSizes(); i++) {
	    for (int i = 0; i < 20; i++) {
	      this.fontSizeToFontMap.put(size,
	          new Font(device,FONT_FAMILY_NAME, size,FONT_STYLE));
	      this.registerFontSizeMapping(scale, size);
	      size += 1;
	      scale += 0.1;
	    }
	  }

	private void makeFonts(Device device) {
		int size = this.minFontSize;
		double scale = 12;
		this.fontSizeToFontMap = new HashMap<Integer, Font>();

//		for (int i = 0; i < this.getNumberOfFontSizes(); i++) {
		for (int i = 0; i < 20; i++) {
			this.fontSizeToFontMap.put(size,
					new Font(device, FONT_FAMILY_NAME, size,FONT_STYLE));
			this.registerFontSizeMapping(scale, size);
			size += 1;
			scale += 2.3;
		}
	}

	public void setFontForZoom(double scale) {
	  this.scale = scale;
		int size = getFontSizeForZoom( scale);
		if (size != -1) {
			this.currentFont = this.fontSizeToFontMap.get(size);
		}
	}

	public Font getSmallFont() {
	    int size = this.getFontSizeForZoom( scale*.5 );
	    if(size != -1) {
	        return this.fontSizeToFontMap.get(size);
	    } else {
	        return this.fontSizeToFontMap.get( minFontSize );
	    }
	}

	public Font getFont() {
		return currentFont;
	}

	public void dispose() {
	    Collection<Font> fonts = new ArrayList<Font>(fontSizeToFontMap.values());
	    fontSizeToFontMap.clear();
	    for(Font font:fonts)
	        font.dispose();
	    fonts.clear();
	}

	@Override
	protected void makeFonts() {

	    makeFonts( device );

	}

	public static class FontTest {
	    public static void main(String [] args) {
	        final Display display = new Display();
	        final Shell shell = new Shell(display);

	        final SWTFontManager fontManager = new SWTFontManager( display,
	                            new java.awt.Font( "Arial",
	                                               java.awt.Font.PLAIN,
	                                               9));

	        List<Integer> list = new ArrayList<Integer>(fontManager.fontSizeToFontMap.keySet());
          Collections.sort(list);
          for(int i:list) {
              System.out.println(i);
          }
          System.out.println();

          List<Integer> list2 = new ArrayList<Integer>();

          for(Font i:fontManager.fontSizeToFontMap.values()) {
              FontData fm = i.getFontData()[0];
              list2.add( fm.getHeight());
          }
          Collections.sort( list2 );
          for(int i : list2) System.out.println(i);
          System.out.println();

          for(double d=.5;d<.5+(20*.1);d+=.1) {
              System.out.println(fontManager.getFontSizeForZoom( d ));
          }

	        shell.addPaintListener(new PaintListener() {
	          public void paintControl(PaintEvent event) {
	              Rectangle rect = shell.getClientArea();
	              double low=0.5,high=2.5;
                double k = (high-low)/rect.height;
                boolean gra = false;
	            for(int y =0 ;y<rect.height;) {

	                double scale = y*k+low;
	                fontManager.setFontForZoom( scale );

	                Font font = fontManager.getFont();
	                event.gc.setFont( font );
	                FontMetrics fm = event.gc.getFontMetrics();
	                if(gra) event.gc.setBackground( display.getSystemColor( SWT.COLOR_WIDGET_LIGHT_SHADOW ) );
	                else event.gc.setBackground( display.getSystemColor( SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW ) );
	                event.gc.fillRectangle( 0,y,rect.width,y+fm.getHeight() );
	                event.gc.drawText( "C", rect.width/2, y );

	                event.gc.drawText( Double.toString( Math.floor( scale*100 )/100), 3, y );
	                event.gc.drawText( Integer.toString( fm.getHeight()),(int) (rect.width*.66),y);

	                    event.gc.drawText((font!=null? Integer.toString(
	                                        font.getFontData()[0].getHeight()):"null"),

	                     (int) (rect.width*.75),y);
	                y+= fm.getHeight();
	                gra = !gra;
	            }


	          }
	        });
	        shell.setBounds(10, 30, 200, 500);
	        shell.open ();
	        while (!shell.isDisposed()) {
	          if (!display.readAndDispatch()) display.sleep();
	        }
	        display.dispose();
	        fontManager.dispose();

	      }
	}

}
