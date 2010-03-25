/*******************************************************************************
* Copyright (c) 2009-2010 Arvid Berg <goglepox@users.sourceforge.net
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
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.properties.IPropertySource2;

public class MolTableSelection implements IStructuredSelection, IAdaptable{

    IMoleculesEditorModel model;
    int[] selection;
    IPropertySource2 propertySource;

    public MolTableSelection(int[] selection,IMoleculesEditorModel model) {
        this.selection = selection;
        this.model = model;
    }

    public boolean isEmpty() {
        return selection.length==0;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {

        if(adapter.isAssignableFrom( IMoleculesEditorModel.class ) ) {
            final IMoleculesEditorModel editorModel = new IMoleculesEditorModel() {

                public ICDKMolecule getMoleculeAt( int index ) {
                    return model.getMoleculeAt( selection[index] );
                }

                public int getNumberOfMolecules() {
                    return selection.length;
                }

                public void markDirty( int index,
                                       ICDKMolecule moleculeToSave ) {

                    throw new UnsupportedOperationException();

                }

                public void save() {
                    throw new UnsupportedOperationException();
                }
                public Collection<Object> getAvailableProperties() {

                    return model.getAvailableProperties();
                }

                public <T> void setPropertyFor( int moleculeIndex,
                                                String property, T value ) {

                    model.setPropertyFor( moleculeIndex, property, value );

                }
                public void instert( ICDKMolecule... molecules ) {
                    throw new UnsupportedOperationException();
                }
                public void delete( int index ) {
                    throw new UnsupportedOperationException();
                }
            };
            return editorModel;
        }
        return null;
    }

    public Object getFirstElement() {
        return model.getMoleculeAt( selection[0] );
    }

    public Iterator<ICDKMolecule> iterator() {
        return new Iterator<ICDKMolecule>() {
            int index = 0;
            
            public boolean hasNext() {
                return index < selection.length;
            }

            public ICDKMolecule next() {
                return model.getMoleculeAt( selection[++index] );
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return selection.length;
    }

    public Object[] toArray() {
        return toListG().toArray();
    }

    private List<ICDKMolecule> toListG() {
        List<ICDKMolecule> molecules = new ArrayList<ICDKMolecule>(selection.length);
        for(int i:selection) {
            molecules.add( model.getMoleculeAt( i ) );
        }
        return molecules;
    }

    @SuppressWarnings("unchecked")
    public List toList() {
        return toListG();
    }
}