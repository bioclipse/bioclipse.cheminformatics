package net.bioclipse.cdk.renderer.blur.impl;

/**
 * Eclipse Public Licence 1.0 Dollyn <dollyn.sun>
 * https://code.google.com/p/dollynprojects
 * /source/browse/eclipse/swt/swt-image-effects/src/imageEffects/Blur.java
 */

import java.util.*;

import org.eclipse.swt.graphics.*;
/**
 * Class for performing image blurs.
 * 
 * References:
 * http://www.jasonwaltman.com/thesis/filter-blur.html
 * http://www.blackpawn.com/texts/blur/default.html
 */
public class Blur {
	/**
	 * @param originalImageData The ImageData to be average blurred.
	 * Transparency information will be ignored.
	 * @param radius the number of radius pixels to consider when blurring image.
	 * @return A blurred copy of the image data, or null if an error occured.
	 */
	public static ImageData blur (ImageData originalImageData, int radius) {
		/*
		 * This method will vertically blur all the pixels in a row at once.
		 * This blurring is performed incrementally to each row.
		 * 
		 * In order to vertically blur any given pixel, maximally (radius * 2 + 1)
		 * pixels must be examined. Since each of these pixels exists in the same column,
		 * they span across a series of consecutive rows. These rows are horizontally
		 * blurred before being cached and used as input for the vertical blur.
		 * Blurring a pixel horizontally and then vertically is equivalent to blurring
		 * the pixel with both its horizontal and vertical neighbours at once.
		 * 
		 * Pixels are blurred under the notion of a 'summing scope'. A certain scope
		 * of pixels in a column are summed then averaged to determine a target pixel's
		 * resulting RGB value. When the next lower target pixel is being calculated,
		 * the topmost pixel is removed from the summing scope (by subtracting its RGB) and
		 * a new pixel is added to the bottom of the scope (by adding its RGB).
		 * In this sense, the summing scope is moving downward.
		 */
		if (radius < 1) return originalImageData;
		// prepare new image data with 24-bit direct palette to hold blurred copy of image
		ImageData newImageData = new ImageData (originalImageData.width, originalImageData.height, 24, new PaletteData (0xFF, 0xFF00, 0xFF0000));
		if (radius >= newImageData.height || radius >= newImageData.width)
			radius = Math.min (newImageData.height, newImageData.width) - 1;
		// initialize cache
		ArrayList<RGB[]> rowCache = new ArrayList<RGB[]>();
		int cacheSize = radius * 2 + 1 > newImageData.height ? newImageData.height
				: radius * 2 + 1; // number of rows of imageData we cache
		int cacheStartIndex = 0; // which row of imageData the cache begins with
		for (int row = 0; row < cacheSize; row++) {
			// row data is horizontally blurred before caching
			rowCache.add (rowCache.size (), blurRow (originalImageData, row, radius));
		}
		// sum red, green, and blue values separately for averaging
		RGB[] rowRGBSums = new RGB[newImageData.width]; 
		int[] rowRGBAverages = new int[newImageData.width];
		int topSumBoundary = 0; // current top row of summed values scope
		int targetRow = 0; // row with RGB averages to be determined
		int bottomSumBoundary = 0; // current bottom row of summed values scope
		int numRows = 0; // number of rows included in current summing scope
		for (int i = 0; i < newImageData.width; i++)
			rowRGBSums[i] = new RGB(0,0,0);
		while (targetRow < newImageData.height) {
			if (bottomSumBoundary < newImageData.height) {
				do {
					// sum pixel RGB values for each column in our radius scope
					for (int col = 0; col < newImageData.width; col++) {
						rowRGBSums[col].red +=
							((RGB[])rowCache.get (bottomSumBoundary - cacheStartIndex))[col].red;
						rowRGBSums[col].green +=
							((RGB[])rowCache.get (bottomSumBoundary - cacheStartIndex))[col].green;
						rowRGBSums[col].blue +=
							((RGB[])rowCache.get (bottomSumBoundary - cacheStartIndex))[col].blue;
					}
					numRows++;
					bottomSumBoundary++; // move bottom scope boundary lower
					if (bottomSumBoundary < newImageData.height 
							&& (bottomSumBoundary - cacheStartIndex) > (radius * 2)) {
						// grow cache
						rowCache.add (rowCache.size (), blurRow (originalImageData, bottomSumBoundary, radius));
					}
				} while (bottomSumBoundary <= radius); // to initialize rowRGBSums at start
			}
			if ((targetRow - topSumBoundary) > (radius)) {
				// subtract values of top row from sums as scope of summed values moves down
				for (int col = 0; col < newImageData.width; col++) {
					rowRGBSums[col].red -=
						((RGB[])rowCache.get (topSumBoundary - cacheStartIndex))[col].red;
					rowRGBSums[col].green -=
						((RGB[])rowCache.get (topSumBoundary - cacheStartIndex))[col].green;
					rowRGBSums[col].blue -=
						((RGB[])rowCache.get (topSumBoundary - cacheStartIndex))[col].blue;
				}
				numRows--;
				topSumBoundary++; // move top scope boundary lower
				rowCache.remove (0); // remove top row which is out of summing scope
				cacheStartIndex++;
			}
			// calculate each column's RGB-averaged pixel
			for (int col = 0; col < newImageData.width; col++) {
				rowRGBAverages[col] = newImageData.palette.getPixel (
						new RGB(
								rowRGBSums[col].red / numRows,
								rowRGBSums[col].green / numRows,
								rowRGBSums[col].blue / numRows)
				);
			}
			// replace original pixels
			newImageData.setPixels (0, targetRow, newImageData.width, rowRGBAverages, 0);
			targetRow++;
		}
		return newImageData;
	}

	/**
	 * Average blurs a given row of image data. Returns the blurred row as a
	 * matrix of separated RGB values.
	 */
	private static RGB[] blurRow (ImageData originalImageData, int row, int radius) {
		RGB[] rowRGBAverages = new RGB[originalImageData.width]; // resulting rgb averages 
		int[] lineData = new int[originalImageData.width];
		originalImageData.getPixels (0, row, originalImageData.width, lineData, 0);
		int r = 0, g = 0, b = 0; // sum red, green, and blue values separately for averaging
		int leftSumBoundary = 0; // beginning index of summed values scope
		int targetColumn = 0; // column of RGB average to be determined
		int rightSumBoundary = 0; // ending index of summed values scope
		int numCols = 0; // number of columns included in current summing scope
		RGB rgb;
		while (targetColumn < lineData.length) {
			if (rightSumBoundary < lineData.length) {
				// sum RGB values for each pixel in our radius scope
				do {
					rgb = originalImageData.palette.getRGB (lineData[rightSumBoundary]);
					r += rgb.red;
					g += rgb.green;
					b += rgb.blue;
					numCols++;
					rightSumBoundary++;
				} while (rightSumBoundary <= radius); // to initialize summing scope at start
			}
			// subtract sum of left pixel as summing scope moves right
			if ((targetColumn - leftSumBoundary) > (radius)) {
				rgb = originalImageData.palette.getRGB (lineData[leftSumBoundary]);
				r -= rgb.red;
				g -= rgb.green;
				b -= rgb.blue;
				numCols--;
				leftSumBoundary++;
			}
			// calculate RGB averages
			rowRGBAverages[targetColumn] = new RGB(r / numCols, g / numCols, b / numCols);
			targetColumn++;
		}
		return rowRGBAverages;
	}
	
}