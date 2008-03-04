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
package net.bioclipse.jmol.views.outline;

import java.util.ArrayList;

import org.jmol.modelset.Chain;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;

/**
 * A class wrapping a ModelSet in JmolContentOutline
 * @author ola
 */
public class JmolModelSet extends JmolObject{

	private ModelSet modelSet;

	/**
	 * Construct a JmolModelSet from a ModelSet.
	 * @param modelSet
	 */
	public JmolModelSet(ModelSet modelSet) {
		this.modelSet=modelSet;
	}
	
	public Object getObject() {
		return modelSet;
	}

	public void setObject(Object object) {
		if (object instanceof Model) {
			modelSet=(ModelSet)object;
		}
	}

	/**
	 * For e.g. properties view
	 */
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create the children as all Chains of this Model
	 */
	public void createChildren() {
		
		ArrayList<IJmolObject> newChildren=new ArrayList<IJmolObject>();
		for (int i=0; i< modelSet.getModelCount(); i++){
			Model model=modelSet.getModels()[i];
			JmolModel c=new JmolModel(model);
			newChildren.add(c);
		}
		setChildren(newChildren);
	}

	/**
	 * ModelSet is not shown and therefore returns null as selectString
	 */
	public String getSelectString() {
		return null;
	}

}
