package org.openscience.cdk.renderer.selection;

import java.util.Collection;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;

/**
 * A selection of some atoms and bonds from an atom container or chem model.
 *
 * @author maclean
 * @cdk.module render
 */
public interface IChemObjectSelection {

	/**
     * Perform a selection by some method. This is used for selecting outside
     * the hub, for example:
     *
     *   IChemModel model = createModelBySomeMethod();
     *   selection.select(model);
     *   renderModel.setSelection(selection);
     *
     * @param chemModel an IChemModel to select from.
     */
    public void select(IChemModel chemModel);


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
