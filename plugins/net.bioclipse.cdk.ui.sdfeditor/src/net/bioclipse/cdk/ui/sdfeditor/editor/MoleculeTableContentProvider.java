/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *        Arvid Berg <goglepox@users.sf.net>
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author arvid
 */
public class MoleculeTableContentProvider implements
        ILazyContentProvider, IDataProvider ,IMoleculeTableColumnHandler
        {

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    public static int READ_AHEAD = 100;
    static final int NUMBER_OF_PROPERTIES =10;

    private MoleculeTableViewer viewer;
    IMoleculesEditorModel   model       = null;
    List<Object> properties = new ArrayList<Object>(NUMBER_OF_PROPERTIES);
//    Collection<Object> availableProperties = new HashSet<Object>();

    MoleculesEditorLabelProvider melp = new MoleculesEditorLabelProvider(
                                    MoleculeTableViewer.STRUCTURE_COLUMN_WIDTH);

    public MoleculesEditorLabelProvider getLabelProvider() {
        return melp;
    }

    public List<Object> getProperties() {

        return new ArrayList<Object>(properties);
    }

    public Collection<Object> getAvailableProperties() {
        if(model instanceof SDFIndexEditorModel)
            return ((SDFIndexEditorModel)model).getPropertyKeys();
        return Collections.emptySet();
    }


    private void setModel(IMoleculesEditorModel model) {
        this.model = model;
        if(viewer !=null)
            viewer.refresh();
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        ICDKMolecule molecule = null;
        IMoleculesEditorModel lModel;
        synchronized ( this ) {
            lModel = model;
        }
        if ( lModel != null ) {
            molecule = lModel.getMoleculeAt( index );
        }

        return molecule;
    }

    public void inputChanged( final Viewer viewer, Object oldInput, Object newInput ) {

        if(newInput == null) {
            return;
        }

        if(viewer != this.viewer) {
            this.viewer = (MoleculeTableViewer)viewer;
        }

        
        if(newInput instanceof IMoleculesEditorModel)
            setModel((IMoleculesEditorModel) newInput);
        else if(newInput instanceof IAdaptable){
            setModel( (IMoleculesEditorModel)
                      ((IAdaptable)newInput).getAdapter(
                                                 IMoleculesEditorModel.class ));
        }

        if(this.viewer != null)
            updateSize( (model!=null?model.getNumberOfMolecules():0) );

        // fill properties with elements from availableProperties
        properties.clear();
        Iterator<Object> iter = getAvailableProperties().iterator();
        for(int i=0;i<NUMBER_OF_PROPERTIES;i++) {
            if(iter.hasNext())
                properties.add(iter.next());
        }
        updateHeaders();
    }

    void updateHeaders() {

        viewer.refresh();
    }

    private NatTable getCompositeTable(Viewer viewer) {

        return (NatTable)viewer.getControl();
    }

    enum FileType {
        SDF,SMI
    }

    private void updateSize(int size) {
        getCompositeTable( viewer ).redraw();
    }


    public void updateElement( int index ) {

    }

    public void dispose() {

    }

    public void setVisibleProperties( List<Object> visibleProperties ) {
        properties.clear();
        properties.addAll( visibleProperties );
        updateHeaders();
    }

    public int getColumnCount() {
        return properties.size()+1;
    }

    public int getRowCount() {
        IMoleculesEditorModel tModel = model;
        if(tModel != null)
            return tModel.getNumberOfMolecules();
        return 0;
    }

    public Object getValue( int row, int col ) {
        IMoleculesEditorModel tModel = model;
        ICDKMolecule molecule =  (ICDKMolecule) tModel.getMoleculeAt( row );
        if(col == 0) {
            return molecule;
        }
        int i = col;
        if( properties != null && i<properties.size()+1) {
            Object value = molecule.getAtomContainer()
            .getProperty( properties.get(i-1));
            return  value!=null?value.toString():"";
        } else
            return "";
    }

    public int getNumberOfMolecules() {
        return getRowCount();
    }
}
