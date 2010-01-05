/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import java.util.ArrayList;

import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;

public class JmolGroup extends JmolObject{

	private Group group;
	private Chain chain;
	
	public JmolGroup(Group group, Chain chain) {
		this.group=group;
		this.chain=chain;
	}
	
	
	@Override
	public void createChildren() {
        ArrayList<IJmolObject> newChildren=new ArrayList<IJmolObject>();
		System.out.println("Group: " + group.getGroup1() + " " + group.getGroupID());
		for (int i=group.getFirstAtomIndex();i<=group.getLastAtomIndex();i++){
			newChildren.add(new JmolAtom(chain.getAtom(i)));
		}
		setChildren(newChildren);
	}

	public Object getObject() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSelectString() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setObject(Object object) {
		// TODO Auto-generated method stub
		
	}

}
