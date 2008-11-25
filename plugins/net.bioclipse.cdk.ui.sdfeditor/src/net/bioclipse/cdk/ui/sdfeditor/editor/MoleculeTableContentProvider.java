/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg goglepox@users.sf.net
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Scanner;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;


/**
 * @author arvid
 *
 */
public class MoleculeTableContentProvider implements ILazyContentProvider {

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    TableViewer viewer;
    IFile file = null;
    IMoleculesEditorModel model = null;
    int childCount;
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
     */
    public void updateElement( int index ) {
        if(model != null) {
            viewer.replace( model.getMoleculeAt( index ), index );
            int count = model.getNumberOfMolecules();
            if( count > childCount) {
                viewer.setItemCount( count );
                childCount = count;
            }
            return;
        }
        Object element = null;
        try {
            Iterator<ICDKMolecule> iter = Activator.getDefault()
            .getCDKManager().createMoleculeIterator( file );
            ICDKMolecule molecule;
            int count = 0;
            while ( iter.hasNext() ) {
                molecule = iter.next();
                if ( count++ == index ) {
                    element = molecule;
                    break;
                }
            }
            for(int i=0;i<10 &&iter.hasNext();i++,count++) {
                iter.next();
            }
//            if ( iter.hasNext() ) {
//                count += 1;
//            }
            if ( element != null )
                viewer.replace( element, index );

            if ( count > childCount ) {
                viewer.setItemCount( count );
                childCount = count;
//                Display.getCurrent().asyncExec( new Runnable() {
//                    public void run() {
//                        viewer.refresh();
//                    }
//                });

            }

        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {

        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {

        if(viewer != this.viewer)
            this.viewer = (TableViewer)viewer;
        if ( newInput != oldInput ) {
            if ( newInput instanceof IEditorInput ) {
                IEditorInput input = (IEditorInput) newInput;
                file = (IFile)input.getAdapter( IFile.class );
                if(file !=null) {
                    model = null;
                    return;
                }

                 model = (IMoleculesEditorModel)
                                input.getAdapter( IMoleculesEditorModel.class );
            }
        }
    }

    private int calculateChildCount() {

        try {
            int count = 0;
            BufferedReader reader = new BufferedReader(
                         new InputStreamReader( file.getContents() ) );
            String line;
            while ( (line = reader.readLine()) != null ) {
                if ( line.contains( "$$$$" ) )
                    count++;
            }
            return count;

        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
        return -1;
    }

    public String getSDFPart( IFile file, int index ) throws CoreException,
                                                             IOException {

        InputStream is = null;
        try {
            IFile sourceFile = file;
            int count = 0;
            is = sourceFile.getContents();
            Scanner sc = new Scanner( is );
            sc.useDelimiter( "\\${4}" );
            String data;
            while ( sc.hasNext() ) {
                data = sc.next();
                if ( count == index ) {
                    if ( sc.hasNext() )
                        childCount = count + 2;
                    else
                        childCount = count + 1;
                    return data;
                }
                count++;
            }
            childCount = count + 1;

        } finally {
            is.close();
        }
        return null;
    }

}
