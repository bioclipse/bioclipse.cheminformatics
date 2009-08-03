package org.openscience.cdk.controller.edit;

import java.util.Set;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.Changed;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

/**
 *
 * @author Arvid
 * @cdk.module controlbasic
 */
public class CreateBond extends AbstractEdit implements IEdit{

    Point2d first;
    Point2d second;

    IAtom firstAtom;
    IAtom secondAtom;
    IBond bond;

    public static IEdit edit(Point2d first, Point2d second) {
        CreateBond edit = new CreateBond();
        edit.first = first;
        edit.second = second;
        return edit;
    }
    public void redo() {

         firstAtom = model.getBuilder().newAtom( "C", first );
         secondAtom = model.getBuilder().newAtom( "C", first );

         bond = model.getBuilder().newBond( firstAtom, secondAtom );

         model.addAtom( firstAtom );
         model.addAtom( secondAtom );
         model.addBond( bond );

         updateHydrogenCount( firstAtom, secondAtom );
    }

    public void undo() {

        model.removeBond( bond );
        model.removeAtom( firstAtom );
        model.removeAtom( secondAtom );

    }

    public Set<Changed> getTypeOfChanges() {
        return changed( Changed.Structure );
    }
}
