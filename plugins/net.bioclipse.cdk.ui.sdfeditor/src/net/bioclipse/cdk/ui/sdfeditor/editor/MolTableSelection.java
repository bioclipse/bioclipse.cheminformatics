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
import java.util.LinkedList;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.eclipse.core.expressions.IIterable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.properties.IPropertySource2;

public class MolTableSelection implements IStructuredSelection, IIterable,IAdaptable{

    IMoleculesEditorModel model;
    List<Integer> selection;
    IPropertySource2 propertySource;

    public MolTableSelection(int[] selection,IMoleculesEditorModel model) {
        this.selection = new ArrayList<Integer>(selection.length);
        for(int i:selection) {
        	this.selection.add(i);
        }
        this.model = model;
        List<Integer> sel = new LinkedList<Integer>(this.selection);
        for(Iterator<Integer> iter=sel.iterator();iter.hasNext();) {
        	int index = iter.next();
        	if(index > model.getNumberOfMolecules()) {
        		iter.remove();
        	}
        }
        this.selection = new ArrayList<Integer>(sel);
    }

    public boolean isEmpty() {
        return selection.size()==0;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {

        if(adapter.isAssignableFrom( IMoleculesEditorModel.class ) ) {
            final IMoleculesEditorModel editorModel = new IMoleculesEditorModel() {

                public ICDKMolecule getMoleculeAt( int index ) {
                		return model.getMoleculeAt( selection.get(index) );
                }

                public int getNumberOfMolecules() {
                    return selection.size();
                }

                public void markDirty( int index,
                                       ICDKMolecule moleculeToSave ) {

                    throw new UnsupportedOperationException();

                }
                
                @Override
                public boolean isDirty( int index ) {
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
        return model.getMoleculeAt( selection.get(0) );
    }

    public Iterator<ICDKMolecule> iterator() {
        return new Iterator<ICDKMolecule>() {
            int index = 0;
            
            public boolean hasNext() {
                return index + 1 < selection.size();
            }

            public ICDKMolecule next() {
                return model.getMoleculeAt( selection.get(index++) );
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return selection.size();
    }

    public Object[] toArray() {
        return toListG().toArray();
    }

    private List<ICDKMolecule> toListG() {
        List<ICDKMolecule> molecules = new ArrayList<ICDKMolecule>(selection.size());
        for(int i:selection) {
        	if(i > 0 && i< model.getNumberOfMolecules())
        		molecules.add( model.getMoleculeAt( i ) );
        }
        return molecules;
    }

    @SuppressWarnings("unchecked")
    public List toList() {
        return toListG();
    }
}