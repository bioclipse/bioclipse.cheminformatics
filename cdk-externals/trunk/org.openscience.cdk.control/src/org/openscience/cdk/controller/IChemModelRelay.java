/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-09-02 11:46:10 +0100 (su, 02 sep 2007) $
 * 
 * Copyright (C) 2007  Egon Willighagen <egonw@users.lists.sf>
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.controller;

import java.io.IOException;
import java.util.Collection;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.ControllerHub.Direction;
import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.selection.IncrementalSelection;

/**
 * @cdk.module control
 */
public interface IChemModelRelay {

    /* Interaction*/
    public IControllerModel getController2DModel();
    public Renderer getRenderer();
    public IChemModel getIChemModel();
    public void setChemModel(IChemModel model);
    public IAtom getClosestAtom(Point2d worldCoord);
    public IBond getClosestBond(Point2d worldCoord);
    public IAtom getAtomInRange(Point2d worldCoord, Collection<IAtom> toignore, IAtom atomtoignore);
    public void updateView();
    public void select(IncrementalSelection selection);
    
    /* Event model */
    public void setEventHandler(IChemModelEventRelayHandler handler);
    public void fireZoomEvent();
    /**
     * Adds an temporary atom which might be cleared later, when the final
     * atom is added. Controllers can use this to draw temporary atoms, for
     * example while drawing new bonds.
     *  
     * @param atom atom to add as phantom
     */
    public void addPhantomAtom(IAtom atom);
    /**
     * Adds an temporary bond which might be cleared later, when the final
     * bond is added. Controllers can use this to draw temporary bonds, for
     * example while drawing new bonds.
     *  
     * @param bond bond to add as phantom
     */
    public void addPhantomBond(IBond bond);
    /**
     * Returns an IAtomContainer containing all phantom atoms and bonds.
     */
    public IAtomContainer getPhantoms();
    /**
     * Deletes all temporary atoms.
     */
    public void clearPhantoms();

    /* Editing actions for the complete model */
    public abstract void updateImplicitHydrogenCounts();
    public void zap();
    public IRing addRing(int size, Point2d worldcoord);
    public IRing addRing(IAtom atom, int size);
    public IRing addPhenyl(IAtom atom);
    public IRing addPhenyl(Point2d worldcoord);
    public IRing addRing(IBond bond, int size);
    public IRing addPhenyl(IBond bond);
    public void addFragment(IAtomContainer toPaste);
    public IAtomContainer deleteFragment(IAtomContainer toDelete);
    public void cleanup();
    public void flip(boolean horizontal);
	public void makeReactantInNewReaction(IAtomContainer newContainer, IAtomContainer oldcontainer);
	public void makeReactantInExistingReaction(String s,
			IAtomContainer newContainer, IAtomContainer container);
	public void makeProductInNewReaction(IAtomContainer newContainer,
			IAtomContainer container);
	public void makeProductInExistingReaction(String s,
			IAtomContainer newContainer, IAtomContainer container);
    /**
     * Adjusts all bond orders to fit valency
     */
    public void adjustBondOrders() throws IOException, ClassNotFoundException, CDKException;
    /**
     * Sets all bond order to single
     */
    public void resetBondOrders();
    public void clearValidation();
    public void makeAllImplicitExplicit();
    public void makeAllExplicitImplicit();
//    public abstract void cleanupSelection(Selector sectionIdentifier);

    /* Editing actions for atoms */
    public abstract IAtomContainer removeAtom(IAtom atom);
    public abstract IAtomContainer removeAtomWithoutUndo(IAtom atom);
    public abstract IAtom addAtom(String element, Point2d worldcoord);
    public abstract IAtom addAtomWithoutUndo(String element, Point2d worldcoord);
    public abstract IAtom addAtom(String element, IAtom atom);
    public abstract IAtom addAtomWithoutUndo(String element, IAtom atom);
    public abstract void moveToWithoutUndo(IAtom atom, Point2d point);
    public abstract void moveTo(IAtom atom, Point2d point);
    public abstract void setSymbol(IAtom atom, String symbol);
    public abstract void setCharge(IAtom atom, int charge);
    public abstract void setMassNumber(IAtom atom, int charge);
    public void setHydrogenCount(IAtom atom, int intValue);
    public void replaceAtom(IAtom atomnew, IAtom atomold);
    public void addSingleElectron(IAtom atom);
    public void updateAtoms(IAtomContainer container, Iterable<IAtom> atoms);
    public void updateAtom(IAtom atom);

    /* Editing actions for bonds */
    public abstract IBond addBond(IAtom fromAtom, IAtom toAtom);
    public abstract void removeBondWithoutUndo(IBond bond);
    public abstract void removeBond(IBond bond);
    public abstract void moveToWithoutUndo(IBond bond, Point2d point);
    public abstract void moveTo(IBond bond, Point2d point);
    public abstract void setOrder(IBond bond, IBond.Order order);
    public abstract void setWedgeType(IBond bond, int type);
    public abstract void addNewBond(Point2d worldCoordinate);
    public void cycleBondValence(IBond bond);
    public void makeBondStereo(IBond bond, Direction desiredDirection);
    public void makeNewStereoBond(IAtom atom, Direction desiredDirection);
    		
    public IUndoRedoFactory getUndoRedoFactory();
    public UndoRedoHandler getUndoRedoHandler();	
}
