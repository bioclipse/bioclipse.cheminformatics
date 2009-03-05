/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.undoredo;

import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.eclipse.core.commands.operations.IUndoContext;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IElectronContainer;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.interfaces.IBond.Order;

public class SWTUndoRedoFactory implements IUndoRedoFactory {
    
    private IUndoContext context;
    
    public SWTUndoRedoFactory(IUndoContext context) {
        this.context = context;
    }

    public IUndoRedoable getAddAtomsAndBondsEdit(IChemModel chemModel,
            IAtomContainer undoRedoContainer, String type, IControllerModel c2dm) {
        return new SWTAddAtomsAndBondsEdit(
                chemModel, undoRedoContainer, type, c2dm, this.context);
    }

    public IUndoRedoable getAdjustBondOrdersEdit(
            Map<IBond, Order[]> changedBonds,
            Map<IBond, Integer[]> changedBondsStereo, String type) {
        return new SWTAdjustBondOrdersEdit(changedBonds, changedBondsStereo, type, this.context);
    }

    public IUndoRedoable getChangeAtomSymbolEdit(IAtom atom,
            String formerSymbol, String symbol, String type) {
        return new SWTChangeAtomSymbolEdit(atom, formerSymbol, symbol, type, this.context);
    }

    public IUndoRedoable getChangeChargeEdit(IAtom atomInRange,
            int formerCharge, int newCharge, String type) {
        return new SWTChangeChargeEdit(atomInRange, formerCharge, newCharge, type, this.context);
    }

    public IUndoRedoable getMoveAtomEdit(IAtomContainer undoRedoContainer,
            Vector2d offset, String type) {
        return new SWTMoveAtomEdit(undoRedoContainer, offset, type, this.context);
    }

    public IUndoRedoable getRemoveAtomsAndBondsEdit(IChemModel chemModel,
            IAtomContainer undoRedoContainer, String type) {
        return new SWTRemoveAtomsAndBondsEdit(chemModel, undoRedoContainer, type, this.context);
    }

    public IUndoRedoable getChangeCoordsEdit(Map<IAtom, Point2d[]> atomCoordsMap,
                                              String type ) {
        return new SWTChangeCoordsEdit(atomCoordsMap, type, this.context);
    }

    public IUndoRedoable getClearAllEdit( IChemModel chemModel,
                                          IMoleculeSet som, IReactionSet sor,
                                          String type ) {
        return new SWTClearAllEdit(chemModel, som, sor, type, this.context);
    }

    //The following methods are not needed in Bioclipse JCP right now.
    //Therefore they return null. If any methods in the hub using these
    //will be implemented in Bioclipse, the method needs to become active 
    //and the required SWTBlaEdit created.
    public IUndoRedoable getChangeIsotopeEdit( IAtom atom,
                                               Integer formerIsotopeNumber,
                                               Integer newIstopeNumber,
                                               String type ) {
        return null;
    }

    public IUndoRedoable getConvertToRadicalEdit(
                                                  IAtomContainer relevantContainer,
                                                  IElectronContainer electronContainer,
                                                  String type ) {

        return null;
    }

    public IUndoRedoable getMakeReactantOrProductInExistingReactionEdit(
                                                                         IChemModel chemModel,
                                                                         IAtomContainer newContainer,
                                                                         IAtomContainer oldcontainer,
                                                                         String s,
                                                                         boolean reactantOrProduct,
                                                                         String string ) {

        return null;
    }

    public IUndoRedoable getMakeReactantOrProductInNewReactionEdit(
                                                                    IChemModel chemModel,
                                                                    IAtomContainer ac,
                                                                    IAtomContainer oldcontainer,
                                                                    boolean reactantOrProduct,
                                                                    String type ) {

        return null;
    }

    public IUndoRedoable getReplaceAtomEdit( IChemModel chemModel,
                                             IAtom oldAtom, IAtom newAtom,
                                             String type ) {

        return null;
    }

}
