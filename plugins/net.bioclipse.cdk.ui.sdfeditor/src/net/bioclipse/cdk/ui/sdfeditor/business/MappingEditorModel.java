/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;


/**
 * @author arvid
 *
 */
public class MappingEditorModel implements IFileMoleculesEditorModel {

    Logger logger  = Logger.getLogger( MappingEditorModel.class.getName() );
    IFileMoleculesEditorModel model;

    List<Integer> mappingIndex;

    public MappingEditorModel( IFileMoleculesEditorModel model){
        this.model = model;
        mappingIndex = new ArrayList<Integer>(model.getNumberOfMolecules()+20);
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            mappingIndex.add( i );
        }
    }

    public IFile getResource() {
        return model.getResource();
    }
    public Collection<Object> getAvailableProperties() {
        return model.getAvailableProperties();
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        if(index<0 || index >= mappingIndex.size()) return null;
        return model.getMoleculeAt( mappingIndex.get( index ) );
    }

    public int getNumberOfMolecules() {
        return mappingIndex.size();
    }

    public void insert(int index, ICDKMolecule... molecules) {
        int insert = mappingIndex.size();
        List<Integer> indices = new ArrayList<Integer>(molecules.length);
        for(ICDKMolecule molecule:molecules) {
            model.markDirty( insert, molecule );
            indices.add( insert++ );
        }
        mappingIndex.addAll( index, indices);
    }

    public void move( int indexFrom, int indexTo ) {
        Integer mapping = mappingIndex.remove( indexFrom );
        mappingIndex.add( indexTo, mapping );
    }

    public void markDirty( int index, ICDKMolecule moleculeToSave ) {
        model.markDirty( mappingIndex.get( index ), moleculeToSave );
    }

    public void save() {
       throw new UnsupportedOperationException( "Use MolTable manager to save "+
                                                "IFileMoleculeEdiotrModels");
    }

    public <T> void setPropertyFor( int index, String property, T value ) {
        model.setPropertyFor( mappingIndex.get( index ), property, value );
    }

    public void instert( ICDKMolecule... molecules ) {
        insert(mappingIndex.size(),molecules);
    }

    public void delete( int index ) {
        mappingIndex.remove( index );
    }

    public IFileMoleculesEditorModel getModel() { return model;}
}
