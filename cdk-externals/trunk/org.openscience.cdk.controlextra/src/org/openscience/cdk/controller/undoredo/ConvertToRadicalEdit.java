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

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IElectronContainer;

/**
 * Undo/Redo Edit class for the ConvertToRadicalAction,containing the methods
 * for undoing and redoing the regarding changes
 * 
 * @cdk.module controlextra
 * @cdk.svnrev  $Revision: 10979 $
 */
public class ConvertToRadicalEdit implements IUndoRedoable {

    private static final long serialVersionUID = 2348438340238651134L;

    private IAtomContainer container;

	private IElectronContainer electronContainer;
	
	private String type;

	/**
	 * @param relevantContainer -
	 *            The container the changes were made
	 * @param electronContainer -
	 *            AtomContainer containing the SingleElectron
	 */
	public ConvertToRadicalEdit(IAtomContainer relevantContainer,
			IElectronContainer electronContainer, String type) {
		this.container = relevantContainer;
		this.electronContainer = electronContainer;
		this.type=type;
	}

	public void redo(){
		container.addElectronContainer(electronContainer);

	}

	public void undo(){
		container.removeElectronContainer(electronContainer);
	}

	public boolean canRedo() {
		return true;
	}

	public boolean canUndo() {
		return true;
	}

	public String getPresentationName() {
		return type;
	}
}
