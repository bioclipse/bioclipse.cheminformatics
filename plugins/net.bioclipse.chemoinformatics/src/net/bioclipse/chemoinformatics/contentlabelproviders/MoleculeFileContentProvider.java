/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.chemoinformatics.contentlabelproviders;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
/** 
 * A class implementing ITreeContentProvider and only returning child elements 
 * which are molecule files. This can be used to build TreeViewers for browsing 
 * for molecules.
 *
 */
public class MoleculeFileContentProvider implements ITreeContentProvider {
    private static final Logger logger 
        = Logger.getLogger(MoleculeFileContentProvider.class);
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
        if ( parentElement instanceof IContainer 
             && ( (IContainer)parentElement ).isAccessible() ) {
            IContainer container = (IContainer)parentElement;
            try {
                for ( int i=0 ; i < container.members().length ; i++ ) {
                    IResource resource = container.members()[i];
                    if ( resource instanceof IFile  ) {
                        
                        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
                        InputStream stream = ((IFile)resource).getContents();
                        IContentType contentType = contentTypeManager.findContentTypeFor(stream, ((IFile)resource).getName());
                        stream.close();
                        if(contentType!=null 
                                && (contentType.getId().equals( "net.bioclipse.contenttypes.mdlMolFile2D" )
                                || contentType.getId().equals( "net.bioclipse.contenttypes.mdlMolFile3D" )
                                || contentType.getId().equals( "net.bioclipse.contenttypes.cml.singleMolecule2d" )
                                || contentType.getId().equals( "net.bioclipse.contenttypes.cml.singleMolecule3d" )
                                || contentType.getId().equals( "net.bioclipse.contenttypes.pdb" ))
                        ){
                               childElements.add(resource);
                        }
                    }
                    if ( resource instanceof IContainer 
                         && resource.isAccessible() && resource.getName().charAt(0) != '.' ) {
                        childElements.add(resource);
                    }
                }
            } 
            catch (CoreException e) {
                LogUtils.handleException(e,logger);
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return childElements.toArray();
    }
    public Object getParent(Object element) {
        return ( (IFolder)element ).getParent();
    }
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }
}