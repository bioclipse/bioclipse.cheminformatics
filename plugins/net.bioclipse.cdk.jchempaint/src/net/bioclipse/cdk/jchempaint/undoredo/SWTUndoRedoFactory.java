package net.bioclipse.cdk.jchempaint.undoredo;

import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IBond.Order;

public class SWTUndoRedoFactory implements IUndoRedoFactory {

    public IUndoRedoable getAddAtomsAndBondsEdit(IChemModel chemModel,
            IAtomContainer undoRedoContainer, String type, IControllerModel c2dm) {
        return new SWTAddAtomsAndBondsEdit(
                chemModel, undoRedoContainer, type, c2dm);
    }

    public IUndoRedoable getAdjustBondOrdersEdit(
            Map<IBond, Order[]> changedBonds,
            Map<IBond, Integer[]> changedBondsStereo, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    public IUndoRedoable getChangeAtomSymbolEdit(IAtom atom,
            String formerSymbol, String symbol, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    public IUndoRedoable getChangeChargeEdit(IAtom atomInRange,
            int formerCharge, int newCharge, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    public IUndoRedoable getCleanUpEdit(Map<IAtom, Point2d[]> atomCoordsMap,
            String type) {
        // TODO Auto-generated method stub
        return null;
    }

    public IUndoRedoable getMoveAtomEdit(IAtomContainer undoRedoContainer,
            Vector2d offset, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    public IUndoRedoable getRemoveAtomsAndBondsEdit(IChemModel chemModel,
            IAtomContainer undoRedoContainer, String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
