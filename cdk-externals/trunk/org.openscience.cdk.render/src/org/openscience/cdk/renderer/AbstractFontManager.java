package org.openscience.cdk.renderer;

import java.util.HashMap;

/**
 * @cdk.module render
 */
public abstract class AbstractFontManager {
	
	private HashMap<Double, Integer> scaleToFontSizeMap; 
	
	// these two values track the font position if it falls
	// off the end of the array so that font and scale are always in synch
	private int lowerVirtualCount;
	
	private int upperVirtualCount;
	
	protected int currentFontIndex;
	
	public AbstractFontManager() {
		this.scaleToFontSizeMap = new HashMap<Double, Integer>();
	}
	
	public void registerFontSizeMapping(double scale, int size) {
		this.scaleToFontSizeMap.put(scale, size);
	}
	
	public abstract void setFontForScale(double scale);
	
	protected Integer getFontSizeForScale(double scale) {
		double lower = -1;
		for (Double upper : this.scaleToFontSizeMap.keySet()) {
			if (lower == -1) {
				lower = upper;
				continue;
			}
			if (scale > lower && scale < upper) { 
				return this.scaleToFontSizeMap.get(upper);
			}
			lower = upper;
		}
		return -1;
	}
	
	public int getNumberOfFontSizes() {
		return this.scaleToFontSizeMap.size();
	}
	
	public void resetVirtualCounts() {
		this.lowerVirtualCount = 0;
		this.upperVirtualCount = this.getNumberOfFontSizes() - 1;
	}
	
	public void toMiddle() {
		this.currentFontIndex = this.getNumberOfFontSizes() / 2;
	}
	
	public void increaseFontSize() {
		// move INTO range if we have just moved OUT of lower virtual
		if (this.inRange() || (this.atMin() && this.atLowerBoundary())) {
			this.currentFontIndex++;
		} else if (this.atMax()){
			this.upperVirtualCount++;
		} else if (this.atMin() && this.inLower()){ 
			this.lowerVirtualCount++;
		}
	}
	
	public void decreaseFontSize() {
		// move INTO range if we have just moved OUT of upper virtual
		if (this.inRange() || (this.atMax() && this.atUpperBoundary())) {
			this.currentFontIndex--;
		} else if (this.atMin()) {
			this.lowerVirtualCount--;
		} else if (this.atMax() && this.inUpper()) {
			this.upperVirtualCount--;
		}
	}
	
	public boolean inRange() {
		return this.currentFontIndex > 0 
				&& this.currentFontIndex < this.getNumberOfFontSizes() - 1;
	}
	
	public boolean atLowerBoundary() {
		return this.lowerVirtualCount == 0;
	}
	
	public boolean atUpperBoundary() {
		return this.upperVirtualCount == this.getNumberOfFontSizes() - 1;
	}
	
	public boolean inLower() {
		return this.lowerVirtualCount < 0;
	}
	
	public boolean inUpper() {
		return this.upperVirtualCount > this.getNumberOfFontSizes() - 1; 
	}
	
	public boolean atMax() {
		return this.currentFontIndex == this.getNumberOfFontSizes() - 1;
	}
	
	public boolean atMin() {
		return this.currentFontIndex == 0;
	}
}
