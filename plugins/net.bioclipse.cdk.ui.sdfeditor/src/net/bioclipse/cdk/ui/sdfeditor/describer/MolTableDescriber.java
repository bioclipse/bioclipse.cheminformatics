package net.bioclipse.cdk.ui.sdfeditor.describer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioList;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


public class MolTableDescriber implements IBioObjectDescriber {

    ICDKManager cdk;


    public MolTableDescriber() {

        cdk=Activator.getDefault().getCDKManager();
    }

    @SuppressWarnings("unchecked")
    public String getPreferredEditorID( IBioObject object ) throws BioclipseException {

        if ( object instanceof BioList ) {
            BioList<IBioObject> biolist = (BioList<IBioObject>) object;
            if (biolist.isEmpty())
                throw new BioclipseException("BioList is empty");
            return "net.bioclipse.cdk.ui.sdfeditor";
        }

        return null;
    }

}
