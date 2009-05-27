package net.bioclipse.cdk.ui.sdfeditor.describer;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.RecordableList;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


public class MolTableDescriber implements IBioObjectDescriber {

    public MolTableDescriber() {

    }

    @SuppressWarnings("unchecked")
    public String getPreferredEditorID( IBioObject object ) throws BioclipseException {

        if ( object instanceof RecordableList ) {
            RecordableList<IBioObject> biolist = (RecordableList<IBioObject>) object;
            if (biolist.isEmpty())
                throw new BioclipseException("BioList is empty");
            return "net.bioclipse.cdk.ui.sdfeditor";
        }

        return null;
    }

}
