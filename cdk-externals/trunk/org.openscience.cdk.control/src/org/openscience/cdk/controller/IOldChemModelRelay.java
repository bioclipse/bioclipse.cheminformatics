/*
 * Copyright (C) 2009  Arvid Berg <goglepox@users.sourceforge.net>
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

import org.openscience.cdk.controller.undoredo.IUndoRedoFactory;
import org.openscience.cdk.controller.undoredo.UndoRedoHandler;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.selection.IncrementalSelection;

/**
 * @cdk.module control
 */
@Deprecated
public interface IOldChemModelRelay {
    
    public IChemModel getIChemModel();

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
    
    public void select(IChemObjectSelection selection);

    /* Event model */
    public void updateView();
    public void fireZoomEvent();
    public void fireStructureChangedEvent();
    
    /* Editing actions for the complete model */
    public void updateImplicitHydrogenCounts();
    public void zap();

    public void addFragment(IAtomContainer toPaste);
    public IAtomContainer deleteFragment(IAtomContainer toDelete);
    public void cleanup();
    public void flip(boolean horizontal);
    public void makeReactantInNewReaction(IAtomContainer newContainer, IAtomContainer oldcontainer);
    /**
     * @param reactionId    The id of the reaction to add to
     * @param newContainer  The structure to add to the reaction
     * @param container     The structure to remove from the MoleculeSet
     */
    public void makeReactantInExistingReaction(String reactionId,
            IAtomContainer newContainer, IAtomContainer container);
    public void makeProductInNewReaction(IAtomContainer newContainer,
            IAtomContainer container);
    /**
     * @param reactionId    The id of the reaction to add to
     * @param newContainer  The structure to add to the reaction
     * @param container     The structure to remove from the MoleculeSet
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
}
