/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A selection in Jmol that can be propagated to the workbench
 * @author ola
 *
 */
public class JmolSelection implements IStructuredSelection {

	Set<String> selectionSet;
	
	public JmolSelection(String monomer, String chain) {

		selectionSet=new HashSet<String>();
		selectionSet.add(monomer + ":" + chain);
	}

	public Object getFirstElement() {
		return selectionSet.toArray()[0];
	}

	public Iterator iterator() {
		return selectionSet.iterator();
	}

	public int size() {
		return selectionSet.size();
	}

	public Object[] toArray() {
		return selectionSet.toArray();
	}

	public List toList() {
		List lst=new ArrayList<String>();
		lst.addAll(selectionSet);
		return lst;
	}

	public boolean isEmpty() {
		if (selectionSet==null) return true;
		if (selectionSet.size()<=0) return true;
		return false;
	}

}
