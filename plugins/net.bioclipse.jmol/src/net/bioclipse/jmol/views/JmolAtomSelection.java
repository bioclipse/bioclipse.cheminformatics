/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views;
import java.util.HashSet;
/**
 * A selection of atoms in Jmol that can be propagated to the workbench
 * @author ola
 *
 */
public class JmolAtomSelection extends JmolSelection {
    public JmolAtomSelection(String atomIndex) {
        selectionSet=new HashSet<String>();
        selectionSet.add("atomno="+atomIndex);
    }
}
