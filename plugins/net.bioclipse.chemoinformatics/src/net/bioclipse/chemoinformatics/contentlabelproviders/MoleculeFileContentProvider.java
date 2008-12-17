/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.chemoinformatics.contentlabelproviders;

import java.util.ArrayList;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/** 
 * A class implementing ITreeContentProvider and only returning child elements which are 
 * molecule files. This can be used to build TreeViewers for browsing for molecules.
 *
 */
public class MoleculeFileContentProvider implements ITreeContentProvider {

	private static final Logger logger = Logger.getLogger(MoleculeFileContentProvider.class);
	
	public MoleculeFileContentProvider() {
	}
	
	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<IResource> childElements = new ArrayList<IResource>();
		if(parentElement instanceof IContainer && ((IContainer)parentElement).isAccessible()){
			try {
				for(int i=0;i<((IContainer)parentElement).members().length;i++){
					if(((IContainer)parentElement).members()[i] instanceof IFile 
					  && (((IFile)((IContainer)parentElement).members()[i]).getFileExtension()!=null 
					  && ((((IFile)((IContainer)parentElement).members()[i]).getFileExtension().equals(ICDKManager.mol)) 
					  || ((IFile)((IContainer)parentElement).members()[i]).getFileExtension().equals(ICDKManager.cml)))){
						    childElements.add(((IContainer)parentElement).members()[i]);
					}
					if(((IContainer)parentElement).members()[i] instanceof IContainer && ((IContainer)parentElement).members()[i].isAccessible()){
						childElements.add(((IContainer)parentElement).members()[i]);
					}
				}
			} catch (CoreException e) {
				LogUtils.handleException(e,logger);
			}
		}
		return childElements.toArray();
	}

	public Object getParent(Object element) {
		return ((IFolder)element).getParent();
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length>0;
	}

}