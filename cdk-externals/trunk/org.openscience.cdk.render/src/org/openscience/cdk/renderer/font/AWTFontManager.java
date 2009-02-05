package org.openscience.cdk.renderer.font;

import java.awt.Font;
import java.util.HashMap;

/**
 * @cdk.module render
 */
public class AWTFontManager extends AbstractFontManager {
	
	private HashMap<Integer, Font> fontSizeToFontMap;
	
	private int minFontSize;
	
	private Font currentFont;
	
	public AWTFontManager() {
		// apparently 9 pixels per em is the minimum
		// but I don't know if (size 9 == 9 px.em-1)... 
		this.minFontSize = 9;
		this.makeFonts();
		this.toMiddle();
		this.resetVirtualCounts();
	}
	
	protected void makeFonts() {
		int size = this.minFontSize;
		double scale = 0.5;
		this.fontSizeToFontMap = new HashMap<Integer, Font>();
		
		for (int i = 0; i < 20; i++) {
		    if (super.getFontStyle() == IFontManager.FontStyle.NORMAL) {
    			this.fontSizeToFontMap.put(size,
    					new Font(super.getFontName(), Font.PLAIN, size));
		    } else {
		        this.fontSizeToFontMap.put(size,
                        new Font(super.getFontName(), Font.BOLD, size));
		    }
			this.registerFontSizeMapping(scale, size);
			size += 1;
			scale += 0.1;
		}
	}
	
	public void setFontForZoom(double zoom) {
		int size = this.getFontSizeForZoom(zoom);
		if (size != -1) {
			this.currentFont = this.fontSizeToFontMap.get(size); 
		}
	}
	
	public Font getFont() {
		return currentFont;
	}
}
