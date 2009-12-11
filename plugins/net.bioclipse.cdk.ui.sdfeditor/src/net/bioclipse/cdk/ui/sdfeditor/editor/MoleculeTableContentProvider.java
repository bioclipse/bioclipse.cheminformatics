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
/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.MappingEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.ISortable;
import net.bioclipse.cdk.ui.views.ISortable.SortProperty;
import net.bioclipse.core.domain.IMolecule.Property;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.model.DefaultNatTableModel;
import net.sourceforge.nattable.model.INatTableModel;
import net.sourceforge.nattable.sorting.ISortingDirectionChangeListener;
import net.sourceforge.nattable.sorting.SortingDirection;
import net.sourceforge.nattable.sorting.SortingDirection.DirectionEnum;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * @author arvid
 */
public class MoleculeTableContentProvider implements
        ILazyContentProvider, IDataProvider ,IMoleculeTableColumnHandler
        {

    class PropertyOrder {
        boolean isTheMolecule;
        Object propertyKey;
        String propertyName;
        int row;
        int col;
        
        public PropertyOrder( boolean isTheMolecule,
                              Object propertyKey,
                              String propertyName,
                              int row,
                              int col ) {
            this.isTheMolecule = isTheMolecule;
            this.propertyKey = propertyKey;
            this.propertyName = propertyName;
            this.row = row;
            this.col = col;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result 
                           + ((propertyKey == null) ? 0
                                                    : propertyKey.hashCode());
            return result;
        }

        public boolean equals( Object obj ) {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass() != obj.getClass() ) return false;
            PropertyOrder other = (PropertyOrder) obj;
            if ( !getOuterType().equals( other.getOuterType() ) ) return false;
            if ( propertyKey == null ) {
                if ( other.propertyKey != null )
                    return false;
            }
            else if ( !propertyKey.equals( other.propertyKey ) )
                return false;
            return true;
        }

        private MoleculeTableContentProvider getOuterType() {

            return MoleculeTableContentProvider.this;
        }
    }
    
    class Worker implements Runnable {
        
        private IMoleculesEditorModel tModel;
        
        public Worker(IMoleculesEditorModel tModel) {
            this.tModel = tModel;
        }

        public void run() {
            
            while ( true ) {
                try {
                    synchronized (propertyOrders) {
                        while ( propertyOrders.isEmpty() ) {
                                propertyOrders.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    logger.debug( "Moleculetable worker thread interrupted " +
                    		      "and will run out now." );
                    return;
                }
                
                //only process 5 latest asked for rows
                int toRemove = propertyOrders.size() - (properties.size()+1)* 5;
                if(toRemove > 0) propertyOrders.subList( 0, toRemove ).clear();

                PropertyOrder order = propertyOrders.get( 0 );
                
                logger.debug( "Handling order for: " + order.propertyKey );
                
                ICDKMolecule molecule = (ICDKMolecule) 
                                        tModel.getMoleculeAt( order.row );
                
                if (order.col == 0) {
                    cache(order, molecule);
                    
                    Display.getDefault().asyncExec( 
                        new Runnable() {
                            public void run() {
                                viewer.refresh();
                            }
                        }
                    );
                    continue;
                }
                
                Object p = molecule.getProperty( order.propertyName,
                                                 Property.USE_CACHED );
                
                if (p == null) {
                    if ( model instanceof SDFIndexEditorModel) {
                        // FIXME a general interface to access 
                        // properties
                        p = ( (SDFIndexEditorModel) model)
                                .getPropertyFor( order.row,
                                                 order.propertyName );
                    }
                    else {
                        p = molecule.getAtomContainer().getProperty( 
                                                          order.propertyName );
                    }
                }
                
                cache( order, p != null ? p : "?" );
            }
        }

        private void cache(PropertyOrder order, Object o) {

            if ( moleculeProperties.size() > 10 * properties.size() ) {
                Object k = moleculePropertiesQueue.remove( 0 );
                moleculeProperties.remove( k );
            }
            moleculeProperties.put( order.propertyKey, o );
            moleculePropertiesQueue.add( order.propertyKey );
            logger.debug( "Put: " + order.propertyKey );
            
            propertyOrders.remove( order );
        }
    }

    private final List<PropertyOrder> propertyOrders 
        = Collections.synchronizedList( new LinkedList<PropertyOrder>() );
    private final Map<Object, Object> moleculeProperties 
        = Collections.synchronizedMap( new HashMap<Object, Object>() );
    private final List<Object> moleculePropertiesQueue
        = Collections.synchronizedList( new LinkedList<Object>() );

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    public static int READ_AHEAD = 100;
    static final int NUMBER_OF_PROPERTIES =10;

    private MoleculeTableViewer viewer;
    IMoleculesEditorModel   model       = null;
    List<Object> properties = new ArrayList<Object>(NUMBER_OF_PROPERTIES);
//    Collection<Object> availableProperties = new HashSet<Object>();

    MoleculesEditorLabelProvider melp = new MoleculesEditorLabelProvider(
                                    MoleculeTableViewer.STRUCTURE_COLUMN_WIDTH);

    private ISortingDirectionChangeListener sortDirListener;

    private Thread thread;

    public MoleculesEditorLabelProvider getLabelProvider() {
        return melp;
    }

    public List<Object> getProperties() {

        return new ArrayList<Object>(properties);
    }

    public Collection<Object> getAvailableProperties() {
        return model.getAvailableProperties();
    }

    public void removeColumn(Object key) {
        if(model instanceof MappingEditorModel) {
            model = ((MappingEditorModel)model).getModel();
        }
        if(model instanceof SDFIndexEditorModel) {
            SDFIndexEditorModel sdModel = (SDFIndexEditorModel)model;
            sdModel.removePropertyKey( key );
            properties.retainAll( sdModel.getAvailableProperties() );
            updateHeaders();
        }
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

    public void inputChanged( final Viewer viewer,
                              Object oldInput,
                              Object newInput ) {

        if(newInput == null) {
            return;
        }

        if(viewer != this.viewer) {
            this.viewer = (MoleculeTableViewer)viewer;
        }
        if(newInput instanceof SDFIndexEditorModel) {
            setModel( new MappingEditorModel( 
                          (IFileMoleculesEditorModel )newInput) );
        }else
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

        NatTable table = this.viewer.table;
        if ( model instanceof ISortable ) {
            table.removeSortingDirectionChangeListener( sortDirListener );
            final ISortable sortModel = (ISortable) model;

            ISortingDirectionChangeListener listener
                                    = new ISortingDirectionChangeListener() {

          public void sortingDirectionChanged( SortingDirection[] directions ) {

                    List<SortProperty<?>> sortOrder;
                    if ( directions.length <= 0 ) {
                        sortOrder = Collections.emptyList();
                        sortModel.setSortingProperties( sortOrder );
                        return;
                    }
                    sortOrder = new ArrayList<SortProperty<?>>();
                    for(SortingDirection sDir:directions) {
                        if(sDir.getColumn() == 0) continue;
                        sortOrder.add( new SortProperty<Object>(
                                        properties.get(sDir.getColumn()-1),
                                        sDir.getDirection()==DirectionEnum.UP
                                         ?ISortable.SortDirection.Ascending
                                         :ISortable.SortDirection.Descending) );
                    }
                    sortModel.setSortingProperties( sortOrder );
               }
            };
            setSortListener( listener );
        }else {
            setSortListener( null );
        }

    }

    private void setSortListener( ISortingDirectionChangeListener listener) {
        NatTable table = this.viewer.table;
        if(listener==null)
            table.removeSortingDirectionChangeListener( sortDirListener );
        else
            table.addSortingDirectionChangeListener( listener );
        sortDirListener = listener;
        INatTableModel mod = table.getNatTableModel();
        if(mod instanceof DefaultNatTableModel)
            ((DefaultNatTableModel)mod)
                .setSortingEnabled( listener==null?false:true );
    }

    public void updateHeaders() {

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
        thread.interrupt();
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

    public Object getValue( final int row, final int col ) {
        if ( row >= getNumberOfMolecules() ) return "";
        
        final IMoleculesEditorModel tModel = model;
        final int i = col;
        if ( properties != null && i<properties.size()+1) {
            final Object propertyKey;
            if ( col == 0 ) {
                propertyKey = "the-molecule" + "|" + row + "|" + col;
            }
            else {
                propertyKey = row + "|" + col + "|" 
                              + properties.get( i-1 );
            }

            logger.debug( "Looking for: " + propertyKey );
            Object p = moleculeProperties.get( propertyKey ); 
            if ( p != null ) {
                logger.debug( "Found " + propertyKey );
                return p;
            }

            if ( thread == null ) {
                logger.debug( "Creating thread" );
                thread = new Thread( new Worker(tModel) );
                thread.start();
            }

            PropertyOrder order 
                = new PropertyOrder( col == 0,
                                     propertyKey,
                                     col == 0 ? null 
                                              : (String) 
                                                properties.get( i - 1 ),
                                     row,
                                     col );
            if ( !propertyOrders.contains( order ) ) {
                propertyOrders.add( order );
                logger.debug("Created order for " + order.propertyKey );
            }
             

            }
            synchronized ( propertyOrders ) {
                propertyOrders.notifyAll();
            }
            if ( col == 0 ) {
                // TODO: Make sure that if null (or something else) is returned
                // here "loading" is written in the molecule column.
                return null;
            }
            return "[ Loading... ]";
    }

    public int getNumberOfMolecules() {
        return getRowCount();
    }
}
