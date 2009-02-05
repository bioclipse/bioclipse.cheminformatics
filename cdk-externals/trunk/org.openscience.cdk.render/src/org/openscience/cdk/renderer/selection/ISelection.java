package org.openscience.cdk.renderer.selection;

import java.awt.Color;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.elements.IRenderingElement;

/**
 * @author maclean
 * @cdk.module render
 */
public interface ISelection {
    
    /**
     * Perform a selection by some method.
     * 
     * @param chemModel an IChemModel to select from.
     */
    public void select(IChemModel chemModel);
    
    public void select(IAtomContainer atomContainer);
    
    /**
     * Remove everything from this selection.
     */
    public void clear();
    
    /**
     * Make an IAtomContainer where all the bonds
     * only have atoms that are in the selection.
     * 
     * @return a well defined atom container.
     */
    public IAtomContainer getConnectedAtomContainer();
    
    /**
     * The opposite of a method like "isEmpty"
     * 
     * @return true if there is anything in the selection
     */
    public boolean isFilled();
    
    /**
     * Use this to check if the selection process has finished.
     * Some implementing classes may just choose to return 'true'
     * if their selection is a simple one-step process.
     * 
     * @return true if the selection process is complete
     */
    public boolean isFinished();
    
    /**
     * Generate a display element that represents this selection.
     * This will be used while isFilled() && !isFinished().
     * 
     * @param color the color of the element to generate.
     * @return a rendering element for display purposes.
     */
    public IRenderingElement generate(Color color);

}
