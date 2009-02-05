package org.openscience.cdk.renderer.font;

/**
 * An interface for managing the drawing of fonts at different zoom levels.
 * 
 * @author maclean
 * @cdk.module render
 */
public interface IFontManager {
    
    public enum FontStyle { NORMAL, BOLD }
    
    /**
     * For a particular zoom level, set the appropriate font size to use.
     * 
     * @param zoom a real number in the range (0.0, INF)
     */
    public void setFontForZoom(double zoom);
    
    /**
     * Set the font style (Normal or Bold).
     * 
     * @param fontStyle
     */
    public void setFontStyle(IFontManager.FontStyle fontStyle);
    
    /**
     * Set the font name ('Arial', 'Times New Roman') and so on. 
     * 
     * @param fontName
     */
    public void setFontName(String fontName);

}
