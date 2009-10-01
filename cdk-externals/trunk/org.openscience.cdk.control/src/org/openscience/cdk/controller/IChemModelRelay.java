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

import org.openscience.cdk.controller.edit.IEdit;
import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.renderer.IRenderer;
import org.openscience.cdk.renderer.selection.IncrementalSelection;

/**
 * @cdk.module control
 */
public interface IChemModelRelay extends IAtomBondEdits {

	public enum Direction { UP, DOWN, UNDEFINED, EZ_UNDEFINED };

    /* Interaction*/
    public IControllerModel getController2DModel();
    public IRenderer getRenderer();
    public IChemModel getIChemModel();
    public void setChemModel(IChemModel model);
    public IAtom getClosestAtom(Point2d worldCoord);
    public IBond getClosestBond(Point2d worldCoord);
    
    /**
     * Get the closest atom that is in 'range' (highlight distance) of the
     * atom 'atom', ignoring all the atoms in the collection 'toIgnore'.
     * 
     * @param toIgnore the atoms to ignore in the search
     * @param atom the atom to use as the base of the search
     * @return the closest atom that is in highlight distance
     */
    public IAtom getAtomInRange(Collection<IAtom> toIgnore, IAtom atom);
    
    
    /**
     * Find the atom closest to 'atom', exclusind the atom itself.
     * 
     * @param atom the atom around which to search
     * @return the nearest atom other than 'atom'
     */
    public IAtom getClosestAtom(IAtom atom);
    
    public void updateView();
    public void select(IncrementalSelection selection);

    /* Event model */
    public void setEventHandler(IChemModelEventRelayHandler handler);
    public void fireZoomEvent();
    public void fireStructureChangedEvent();
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
    public void updateImplicitHydrogenCounts();
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
	/**
	 * @param reactionId	The id of the reaction to add to
	 * @param newContainer	The structure to add to the reaction
	 * @param container		The structure to remove from the MoleculeSet
	 */
	public void makeReactantInExistingReaction(String reactionId,
			IAtomContainer newContainer, IAtomContainer container);
	public void makeProductInNewReaction(IAtomContainer newContainer,
			IAtomContainer container);
	/**
	 * @param reactionId	The id of the reaction to add to
	 * @param newContainer	The structure to add to the reaction
	 * @param container		The structure to remove from the MoleculeSet
	 */
	public void makeProductInExistingReaction(String reactionId,
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
//    public  void cleanupSelection(Selector sectionIdentifier);

    public IUndoRedoFactory getUndoRedoFactory();
    public UndoRedoHandler getUndoRedoHandler();
    public void execute( IEdit edit );
}
