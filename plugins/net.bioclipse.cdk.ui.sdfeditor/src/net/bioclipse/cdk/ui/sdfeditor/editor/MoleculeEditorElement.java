package net.bioclipse.cdk.ui.sdfeditor.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

import net.bioclipse.cdk.domain.ICDKMolecule;


public class MoleculeEditorElement implements IAdaptable{
    
    ICDKMolecule molecule;
    
    int index;
    
    
    private MoleculeEditorElement() {

    }
    
    public MoleculeEditorElement(int index, ICDKMolecule molecule) {
        this();
        this.index = index;
        this.molecule = molecule;
    }
    
    public int getIndex() {
        return index;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {
        if(adapter.isAssignableFrom( this.getClass() ))
            return this;
        if(adapter.isAssignableFrom( ICDKMolecule.class )) {
            return molecule;
        }
        return Platform.getAdapterManager().getAdapter( this, adapter );          
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( obj == this) return true;
        if( !(obj instanceof MoleculeEditorElement))
            return false;
        MoleculeEditorElement element = (MoleculeEditorElement) obj;
        if( this.index == element.index && this.molecule == element.molecule)
            return true;
        return false;
    }
    @Override
    public int hashCode() {
        int var = 3;
        var = 31 * var + index;
        var = 31 * var + (molecule == null ? 0: molecule.hashCode());
        return var;
    }
}
