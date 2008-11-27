package net.bioclipse.cdk.jchempaint.view;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
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
		this.minFontSize = 9;
		
		this.makeFonts(device);
		
		this.toMiddle();
		this.resetVirtualCounts();
	}
	
	private void makeFonts(Device device) {
		int size = this.minFontSize;
		double scale = 0.5;
		this.fontSizeToFontMap = new HashMap<Integer, Font>();
		
//		for (int i = 0; i < this.getNumberOfFontSizes(); i++) {
		for (int i = 0; i < 20; i++) {
			this.fontSizeToFontMap.put(size,
					new Font(device, SWTFontManager.FONT_FAMILY_NAME, size,SWT.NONE));
			this.registerFontSizeMapping(scale, size);
			size += 1;
			scale += 0.1;
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
}
