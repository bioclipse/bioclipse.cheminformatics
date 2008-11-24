package org.openscience.cdk.renderer;

import java.awt.Font;
import java.util.HashMap;

/**
 * @cdk.module render
 */
public class AWTFontManager extends AbstractFontManager {
	
	public static final String FONT_FAMILY_NAME = "Arial";
	
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
	
	private void makeFonts() {
		int size = this.minFontSize;
		double scale = 0.5;
		this.fontSizeToFontMap = new HashMap<Integer, Font>();
		
//		for (int i = 0; i < this.getNumberOfFontSizes(); i++) {
		for (int i = 0; i < 20; i++) {
			this.fontSizeToFontMap.put(size,
					new Font(AWTFontManager.FONT_FAMILY_NAME, Font.PLAIN, size));
			this.registerFontSizeMapping(scale, size);
			size += 1;
			scale += 0.1;
		}
	}
	
	@Override
	public void setFontForScale(double scale) {
		int size = this.getFontSizeForScale(scale);
		if (size != -1) {
			this.currentFont = this.fontSizeToFontMap.get(size); 
		}
	}
	
	public Font getFont() {
		return currentFont;
	}
}
