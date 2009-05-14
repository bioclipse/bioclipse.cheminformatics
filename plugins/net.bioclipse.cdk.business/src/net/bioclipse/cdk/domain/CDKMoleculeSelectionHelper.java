/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth <ospjuth@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A helper method to extract molecules from a selection
 * @author ola
 *
 */
public class CDKMoleculeSelectionHelper {

	public static List<ICDKMolecule> getMoleculesFromSelection(ISelection selection){
		

		if (selection.isEmpty()) return null;

		if (!( selection instanceof IStructuredSelection )) return null;

		List<ICDKMolecule> mols=new ArrayList<ICDKMolecule>();

		ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
		
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		while(it.hasNext()){
			
			Object obj=it.next();
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;
				List<ICDKMolecule> newmols;
				try {
					newmols = cdk.loadMolecules(file);
					if (newmols!=null && newmols.size()>0){
						mols.addAll(newmols);
						System.out.println("Added " + newmols.size() + " mols from file: " + file.getName());
					}
				} catch (Exception e) {
					System.out.println("Could not load and parse file: " + file.getName() + ": " + e.getMessage());
				}
			}
			if (obj instanceof IMolecule) {
				IMolecule imol = (IMolecule) obj;
				try {
					ICDKMolecule cdkmol = cdk.create(imol);
					if (cdkmol!=null){
						mols.add(cdkmol);
						System.out.println("Added " + cdkmol.toString() + " as IMolecule");
					}
				} catch (Exception e) {
					System.out.println("Could not parse molecule: " + imol + ": " + e.getMessage());
				}
			}
			
		}
				
		return mols;
		
	}
	
}
