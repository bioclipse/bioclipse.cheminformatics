package net.bioclipse.cdk.domain;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * 
 * @author ola
 *
 */
public class AtomContainerSelection {

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
    public AtomContainerSelection(IAtomContainer selection) {

        super();
        this.selection = selection;
    }
    
}
