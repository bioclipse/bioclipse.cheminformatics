/* Copyright (c) 2013  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.jmol.editors;

import java.util.List;

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jmol.model.IJmolMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;

public interface IJmolEditor {

	void runScript(String script, boolean reportErrorToJSConsole);

	void load(IFile file) throws CoreException;

	void snapshot(IFile file);

	ISelection getSelection();

	void append(IFile file);

	List<IJmolMolecule> getJmolMolecules();

}
