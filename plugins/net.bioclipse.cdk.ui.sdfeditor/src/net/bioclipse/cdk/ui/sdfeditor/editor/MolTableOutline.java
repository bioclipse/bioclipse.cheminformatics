/*******************************************************************************
* Copyright (c) 2010 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class MolTableOutline extends ContentOutlinePage {
    public static class MolTableModel implements IWorkbenchAdapter{
        IMoleculesEditorModel model;
        
        public MolTableModel(IMoleculesEditorModel model) {
            this.model = model;
        }

        public Object[] getChildren( Object o ) {
            return new Object[] { model.getNumberOfMolecules()};
        }

        public ImageDescriptor getImageDescriptor( Object object ) {
            return null;
        }

        public String getLabel( Object o ) {
            return o.toString();
        }

        public Object getParent( Object o ) {
            return model;
        }
        
        @Override
        public String toString() {
            return "Number Of Molecules";
        }
    }
    
    IMoleculesEditorModel model;
    MolTableModel data;
    
    @Override
    public void createControl( Composite parent ) {
        super.createControl( parent );
        TreeViewer tree = getTreeViewer();
        tree.setContentProvider( new ITreeContentProvider() {
            
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        
                if(newInput instanceof IMoleculesEditorModel)
                    model = (IMoleculesEditorModel) newInput;
            }
            
            public void dispose() {
        
            }
            
            public Object[] getElements( Object inputElement ) {
                if(inputElement instanceof IMoleculesEditorModel) {
                    if(data == null) data = new MolTableModel( model );
                    List<Object> properties = new ArrayList<Object>(model
                                                    .getAvailableProperties()) {
                        public String toString() { return "Properties";}
                    };
                    return new Object[] {data, properties};
                }
                return new Object[0];
            }

            public Object[] getChildren( Object parentElement ) {
                if( parentElement instanceof Collection<?>) {
                        return ((Collection<?>)parentElement).toArray();
                    }
                    else if(parentElement instanceof MolTableModel) {
                        return ((IWorkbenchAdapter)parentElement).getChildren( null );
                    }
                return new Object[0];
            }

            public Object getParent( Object element ) {
                return null;
            }

            public boolean hasChildren( Object element ) {
                return element instanceof Collection<?> 
                        || element instanceof MolTableModel;
            }
        });
        tree.setInput( model );
    }
    
    public void setInput(IMoleculesEditorModel model) {
        this.model = model;
        getTreeViewer().setInput( model );
        getTreeViewer().expandAll();
    }
}