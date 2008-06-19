package net.bioclipse.cdk.domain;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * 
 * @author ola
 *
 */
public class MoleculeSelection {

    IAtomContainer selection;

    
    public IAtomContainer getSelection() {
    
        return selection;
    }

    
    public void setSelection( IAtomContainer selection ) {
    
        this.selection = selection;
    }

    /**
     * Constructor
     * @param selection
     */
    public MoleculeSelection(IAtomContainer selection) {

        super();
        this.selection = selection;
    }
    
}
