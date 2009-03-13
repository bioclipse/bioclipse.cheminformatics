package org.openscience.cdk.renderer.selection;

import java.util.Collection;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;

/**
 * @author maclean
 * @cdk.module render
 */
public interface IChemObjectSelection {

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

    public boolean contains(IChemObject obj);
    
    public <E extends IChemObject> Collection<E> elements(Class<E> clazz);
}
