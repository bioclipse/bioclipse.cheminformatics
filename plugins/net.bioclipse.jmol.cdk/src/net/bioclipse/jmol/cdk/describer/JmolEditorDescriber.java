package net.bioclipse.jmol.cdk.describer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.jmol.editors.JmolEditor;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


public class JmolEditorDescriber implements IBioObjectDescriber{

    ICDKManager cdk;
    
    
    public JmolEditorDescriber() {
        cdk=Activator.getDefault().getCDKManager();
    }
    
    public String getPreferredEditorID(IBioObject object) {
        
        if ( object instanceof ICDKMolecule ) {
            ICDKMolecule cdkmol = (ICDKMolecule) object;
            try {
                if (cdk.has3d( cdkmol )){
                    return JmolEditor.EDITOR_ID;
                }
            } catch ( BioclipseException e ) {
                return null;
            }
        }
        
        return null;
    }
    
}
