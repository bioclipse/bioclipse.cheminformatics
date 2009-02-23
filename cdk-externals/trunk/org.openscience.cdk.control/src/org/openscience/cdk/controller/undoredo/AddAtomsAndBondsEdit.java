/* $RCSfile$
 * $Author: gilleain $
 * $Date: 2008-11-26 16:01:05 +0000 (Wed, 26 Nov 2008) $
 * $Revision: 13311 $
 *
 * Copyright (C) 2005-2008 Tobias Helmus, Stefan Kuhn
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
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
package org.openscience.cdk.controller.undoredo;

import java.util.Iterator;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.controller.IControllerModel;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * @cdk.module control
 * @cdk.svnrev  $Revision: 13311 $
 */
public class AddAtomsAndBondsEdit implements IUndoRedoable {

    private static final long serialVersionUID = -7667903450980188402L;

    private IChemModel chemModel;

	private IAtomContainer undoRedoContainer;

	private String type;
	
	private IControllerModel c2dm=null;

	/**
	 * @param chemModel
	 * @param undoRedoContainer
	 * @param c2dm The controller model; if none, set to null
	 */
	public AddAtomsAndBondsEdit(IChemModel chemModel,
			IAtomContainer undoRedoContainer, String type, IControllerModel c2dm) {
		this.chemModel = chemModel;
		this.undoRedoContainer = undoRedoContainer;
		this.type = type;
		this.c2dm=c2dm;
	}

	public void redo() {
		IAtomContainer container = chemModel.getBuilder().newAtomContainer();
		Iterator<IAtomContainer> containers = ChemModelManipulator.getAllAtomContainers(chemModel).iterator();
    	while (containers.hasNext()) {
    		container.add((IAtomContainer)containers.next());
    	}
		for (int i = 0; i < undoRedoContainer.getBondCount(); i++) {
			IBond bond = undoRedoContainer.getBond(i);
			container.addBond(bond);
		}
		for (int i = 0; i < undoRedoContainer.getAtomCount(); i++) {
			IAtom atom = undoRedoContainer.getAtom(i);
			container.addAtom(atom);
		}
		for (int i = 0; i < container.getAtomCount(); i++) {
			this.updateAtom(container,container.getAtom(i));
		}
		IMolecule molecule = container.getBuilder().newMolecule(container);
		IMoleculeSet moleculeSet = ConnectivityChecker
				.partitionIntoMolecules(molecule);
		chemModel.setMoleculeSet(moleculeSet);
	}

	public void undo() {
		for (int i = 0; i < undoRedoContainer.getBondCount(); i++) {
			IBond bond = undoRedoContainer.getBond(i);
			ChemModelManipulator.getRelevantAtomContainer(chemModel, bond).removeBond(bond);
		}
		for (int i = 0; i < undoRedoContainer.getAtomCount(); i++) {
			IAtom atom = undoRedoContainer.getAtom(i);
			ChemModelManipulator.getRelevantAtomContainer(chemModel, atom).removeAtom(atom);
		}
		Iterator<IAtomContainer> containers = ChemModelManipulator.getAllAtomContainers(chemModel).iterator();
    	while (containers.hasNext()) {
    		IAtomContainer container = (IAtomContainer)containers.next();
    		for (int i = 0; i < container.getAtomCount(); i++) {
    			this.updateAtom(container,container.getAtom(i));
    		}
    	}
	}

	public boolean canRedo() {
		return true;
	}

	public boolean canUndo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.undo.UndoableEdit#getPresentationName()
	 */
	public String getPresentationName() {
		return type;
	}
	
	
	/**
	 *  Updates an atom with respect to its hydrogen count
	 *
	 *@param  container  The AtomContainer to work on
	 *@param  atom       The Atom to update
	 */
	public void updateAtom(IAtomContainer container, IAtom atom)
	{
		if (c2dm!=null && c2dm.getAutoUpdateImplicitHydrogens()) {
			try {
				CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(atom.getBuilder());
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(atom.getBuilder());
				IAtomType type = matcher.findMatchingAtomType(container, atom);
				AtomTypeManipulator.configure(atom, type);
				hAdder.addImplicitHydrogens(container, atom);
			} catch (Exception exception) {
				//we fail silently, when the handling of implicit Hs can't done
			}
		}
	}
}
