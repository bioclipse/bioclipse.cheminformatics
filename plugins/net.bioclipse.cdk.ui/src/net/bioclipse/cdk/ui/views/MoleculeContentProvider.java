 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeList;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * This ContentProvider hooks into the CNF to list if IResource contains one
 * or many Molecules.
 * @author ola
 *
 */
public class MoleculeContentProvider implements ITreeContentProvider,
IResourceChangeListener, IResourceDeltaVisitor {

    private static final Logger logger = Logger.getLogger(MoleculeContentProvider.class);

    private static final Object[] NO_CHILDREN = new Object[0];

    private final List<String> MOLECULE_EXT;

    private final Map<IFile, CDKMoleculeList> cachedModelMap;

    private StructuredViewer viewer;

    private ICDKManager cdk;


    //Register us as listener for resource changes
    public MoleculeContentProvider() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        cachedModelMap = new HashMap<IFile, CDKMoleculeList>();
        MOLECULE_EXT=new ArrayList<String>();
        MOLECULE_EXT.add("PDB");
        MOLECULE_EXT.add("CML");
        MOLECULE_EXT.add("MOL");
        MOLECULE_EXT.add("SDF");
        MOLECULE_EXT.add("XYZ");

        cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
    }


    public Object[] getChildren(Object parentElement) {
        Object[] children = null;
        if(parentElement instanceof IFile) {
            /* possible model file */
            IFile modelFile = (IFile) parentElement;
            if(MOLECULE_EXT.contains(modelFile.getFileExtension().toUpperCase())) {
                CDKMoleculeList col=cachedModelMap.get(modelFile);
                if (col!=null){
                    children = col.toArray(new IMolecule[0]);
                    return children != null ? children : NO_CHILDREN;
                }else{
                    if (updateModel(modelFile)!=null){
                        CDKMoleculeList col2=cachedModelMap.get(modelFile);
                        if (col2!=null){
                            children = col2.toArray();
//                            if (children!=null){
//                                if (children[0] instanceof IDNASequence) {
//                                    System.out.println("child is DNASeq");
//                                }
//                                if (children[0] instanceof IRNASequence) {
//                                    System.out.println("child is RNASeq");
//                                }
//                                if (children[0] instanceof IAASequence) {
//                                    System.out.println("child is AASeq");
//                                }
//                            }
                            return children != null ? children : NO_CHILDREN;
                        }
                    }
                }
            }
        }
        return children != null ? children : NO_CHILDREN;
    }
/*
    public Object[] getChildren(Object parentElement) {
        Object[] children = null;
        if(parentElement instanceof IFile) {
            IFile modelFile = (IFile) parentElement;
            if(MOLECULE_EXT.contains(modelFile.getFileExtension().toUpperCase())) {
                children = (Molecule[]) cachedModelMap.get(modelFile);
                if(children == null && updateModel(modelFile) != null) {
                    children = (Molecule[]) cachedModelMap.get(modelFile);
                }
            }
        }
        return children != null ? children : NO_CHILDREN;
    }
    */

    public Object getParent(Object element) {
        if (element instanceof CDKMolecule) {
            CDKMolecule mol = (CDKMolecule) element;
            return mol.getResource();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element instanceof IMolecule) {
            return false;
        } else if(element instanceof IFile) {
            return MOLECULE_EXT.equals(((IFile) element).getFileExtension());
        }
        return false;
    }

    public Object[] getElements(Object parentElement) {
        return getChildren(parentElement);
    }

    /**
     * We need to remove listener and dispose of cache on exit
     */
    public void dispose() {
        cachedModelMap.clear();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    /**
     * When input changes, clear cache so that we will reload content later
     */
    public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
        if (oldInput != null && !oldInput.equals(newInput))
            cachedModelMap.clear();
        viewer = (StructuredViewer) aViewer;
    }

    /**
     * If resources changed
     */
    public void resourceChanged(IResourceChangeEvent event) {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    public boolean visit(IResourceDelta delta) throws CoreException {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * Load the model from the given file, if possible.
     * @param modelFile The IFile which contains the persisted model
     */
    private synchronized CDKMoleculeList updateModel(IFile modelFile) {

        if(MOLECULE_EXT.contains(modelFile.getFileExtension().toUpperCase()) ) {
            CDKMoleculeList model;
            if (modelFile.exists()) {

                try {
                    model= cdk.loadMolecules(modelFile.getLocation().toOSString());
                } catch (IOException e) {
                    return null;
                } catch (BioclipseException e) {
                    return null;
                }

                if (model==null) return null;
                System.out.println("File: " + modelFile + " contained: " + model.size() + " IMolecules");

                cachedModelMap.put(modelFile, model);
                return model;

            } else {
                cachedModelMap.remove(modelFile);
            }
        }
        return null;
    }


}
