 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/

//TODO: Add support for more file formats than sdf

package net.bioclipse.cdk.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBioObject;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * This ContentProvider hooks into the CNF to list if IResource contains 
 * one or many Molecules.
 * @author ola, jonalv
 *
 */
public class MoleculeContentProvider implements ITreeContentProvider,
                                                IResourceChangeListener, 
                                                IResourceDeltaVisitor {

    private static final Logger logger 
        = Logger.getLogger(MoleculeContentProvider.class);

    private static final Object[] NO_CHILDREN = new Object[0];

    private final List<String> MOLECULE_EXT;

    private final Map<IFile, MoleculesFromSDF> cachedModelMap;

    private ICDKManager cdk;
    private DeferredTreeContentManager contentManager; 

    //Register us as listener for resource changes
    @SuppressWarnings("serial")
    public MoleculeContentProvider() {
        ResourcesPlugin.getWorkspace()
                       .addResourceChangeListener( this, 
                                                   IResourceChangeEvent
                                                   .POST_CHANGE );
        cachedModelMap = new HashMap<IFile, MoleculesFromSDF>();
        
        MOLECULE_EXT = new ArrayList<String>() {
            { add("SDF"); }
        };

        cdk = net.bioclipse.cdk.business.Activator
                 .getDefault().getCDKManager();
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IFile) {
            /* possible model file */
            IFile modelFile = (IFile) parentElement;
            if ( MOLECULE_EXT.contains( modelFile.getFileExtension()
                                                 .toUpperCase() ) ) {
                if ( !cachedModelMap.containsKey( modelFile ) ) {
                    updateModel(modelFile);
                }
                return cachedModelMap.containsKey( modelFile ) 
                       ? new Object[] {cachedModelMap.get( modelFile )}
                       : NO_CHILDREN;
            }
        }
        if (parentElement instanceof MoleculesFromSDF) {
            contentManager.getChildren( parentElement );
        }
        return NO_CHILDREN;
    }
        
    public Object getParent(Object element) {
        if (element instanceof SDFElement) {
            return ( (SDFElement)element ).getResource();
        }
        if (element instanceof MoleculesFromSDF) {
            return ( (MoleculesFromSDF) element).getParent( element );
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if ( element instanceof IFile ) {
            return MOLECULE_EXT.equals(
                ( (IFile) element ).getFileExtension() );
        }
        if ( element instanceof MoleculesFromSDF ) {
            return contentManager.mayHaveChildren( element );
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
        ResourcesPlugin.getWorkspace()
                       .removeResourceChangeListener(this);
    }

    /**
     * When input changes, clear cache so that we will reload content later
     */
    public void inputChanged( Viewer viewer, 
                              Object oldInput, 
                              Object newInput ) {
        
        if ( oldInput != null && !oldInput.equals(newInput) ) {
            cachedModelMap.clear();
        }
        if (viewer instanceof AbstractTreeViewer) {
            contentManager = new DeferredTreeContentManager(
                (AbstractTreeViewer) viewer );
        }
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
    private synchronized void updateModel(IFile modelFile) {

        if ( MOLECULE_EXT.contains( 
                modelFile.getFileExtension().toUpperCase() ) ) {
            
            MoleculesFromSDF model;
            if (modelFile.exists()) {

                try {
                    model = new MoleculesFromSDF(modelFile);
                } 
                catch (Exception e) {
                    return;
                }
                cachedModelMap.put(modelFile, model);
            } 
            else {
                cachedModelMap.remove(modelFile);
            }
        }
    }
}
