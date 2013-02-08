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

import static net.bioclipse.cdk.ui.sdfeditor.editor.properties.PropertyOrder.createPropertyKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.MappingEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFIndexEditorModel;
import net.bioclipse.cdk.ui.sdfeditor.editor.properties.PropertyOrder;
import net.bioclipse.cdk.ui.views.IFileMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author arvid
 */
public class MoleculeTableContentProvider implements
        ILazyContentProvider, IDataProvider ,IMoleculeTableColumnHandler
        {

    private final Map<Object, Future<Object>> moleculeProperties
        = new HashMap<Object, Future<Object>>();
    private final List<Object> moleculePropertiesQueue 
        = new LinkedList<Object>();  

    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );

    public static int READ_AHEAD = 100;
    static final int NUMBER_OF_PROPERTIES =10;

    private MoleculeTableViewer viewer;
    IMoleculesEditorModel   model       = null;
    List<Object> properties = new ArrayList<Object>(NUMBER_OF_PROPERTIES);

//    private ISortingDirectionChangeListener sortDirListener;

    private ExecutorService executorService;


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
        moleculeProperties.clear();

//        NatTable table = this.viewer.table;
//        if ( model instanceof ISortable ) {
//            table.removeSortingDirectionChangeListener( sortDirListener );
//            final ISortable sortModel = (ISortable) model;
//
//            ISortingDirectionChangeListener listener
//                                    = new ISortingDirectionChangeListener() {
//
//          public void sortingDirectionChanged( SortingDirection[] directions ) {
//
//                    List<SortProperty<?>> sortOrder;
//                    if ( directions.length <= 0 ) {
//                        sortOrder = Collections.emptyList();
//                        sortModel.setSortingProperties( sortOrder );
//                        return;
//                    }
//                    sortOrder = new ArrayList<SortProperty<?>>();
//                    for(SortingDirection sDir:directions) {
//                        if(sDir.getColumn() == 0) continue;
//                        sortOrder.add( new SortProperty<Object>(
//                                        properties.get(sDir.getColumn()-1),
//                                        sDir.getDirection()==DirectionEnum.UP
//                                         ?ISortable.SortDirection.Ascending
//                                         :ISortable.SortDirection.Descending) );
//                    }
//                    sortModel.setSortingProperties( sortOrder );
//               }
//            };
//            setSortListener( listener );
//        }else {
//            setSortListener( null );
//        }

    }

//    private void setSortListener( ISortingDirectionChangeListener listener) {
//        NatTable table = this.viewer.table;
//        if(listener==null)
//            table.removeSortingDirectionChangeListener( sortDirListener );
//        else
//            table.addSortingDirectionChangeListener( listener );
//        sortDirListener = listener;
//        INatTableModel mod = table.getNatTableModel();
//        if(mod instanceof DefaultNatTableModel)
//            ((DefaultNatTableModel)mod)
//                .setSortingEnabled( listener==null?false:true );
//    }

    public void updateHeaders() {
        viewer.resizeStructureColumn();
        viewer.refresh();
    }

    private NatTable getCompositeTable(Viewer viewer) {

        return (NatTable)viewer.getControl();
    }

    enum FileType {
        SDF,SMI
    }

    private void updateSize(int size) {
        Control control = getCompositeTable( viewer );
        if(!control.isDisposed()) control.redraw();
    }


    public void updateElement( int index ) {

    }

    public void dispose() {
        executorService.shutdown();
    }

    public void setVisibleProperties( List<Object> visibleProperties ) {
        properties.clear();
        properties.addAll( visibleProperties );
        updateHeaders();
    }

    public int getColumnCount() {
        return properties.size();
    }

    public int getRowCount() {
        IMoleculesEditorModel tModel = model;
        if(tModel != null)
            return tModel.getNumberOfMolecules();
        return 0;
    }

    private void initExecutorService() {
        logger.debug( "Creating ExecutorService" );
        executorService = new ThreadPoolExecutor(1, 1,
                                                 0L, TimeUnit.MILLISECONDS,
                                                 new LinkedBlockingQueue<Runnable>()) {
            @Override
            protected void afterExecute( Runnable r,
                                         Throwable t ) {
                super.afterExecute( r, t );
                Display.getDefault().asyncExec( new Runnable() {
                    public void run() {
                        viewer.refresh();
                    }
                });
            }
        };
    }

    public Object getDataValue( final int col, final int row ) {
        if ( row >= getNumberOfMolecules() ) return "";
        
        final int i = col - 1;
        if ( properties == null || i >= properties.size() ) {
            return null;
        }
        if ( executorService == null ) initExecutorService();

        String propertyName = col==0?null:(String) properties.get( i );
        String propertyKey = PropertyOrder.createPropertyKey( propertyName, 
                                                              row);
        Future<Object> p = moleculeProperties.get( propertyKey );
        if ( p == null ) {
            if(col == 0) {
                PropertyOrder order = new PropertyOrder(model,null,row);
                Future<Object> future = executorService.submit( order );
                cacheFuture( propertyKey, future );
            }
            for ( Object propertyObject : properties ) {
                if ( propertyObject instanceof String ) {
                    propertyName = (String)propertyObject;
                    propertyKey = createPropertyKey( propertyName ,row);
                    if ( !moleculeProperties.containsKey( propertyKey ) ) {
                        PropertyOrder order = new PropertyOrder( model,
                                                                 propertyName,
                                                                 row);
                        Future<Object> future = executorService.submit( order );
                        cacheFuture( propertyKey, future );
                    }
                }
            }
        } else {
            if(p.isDone() ) {
                try{
                    return p.get();
                } catch( Exception ex) {
                    moleculeProperties.remove( propertyKey );
                    return "[ Failed ]";
                }
            }
        }
        return "[ Loading... ]";
    }
    
    private void cacheFuture(String propertyKey, Future<Object> future) {
        int visibleRows = ((NatTable)viewer.getControl()).getRowCount();
        if( moleculeProperties.size() > 3 *visibleRows *(properties.size()+1)){
            Object key = moleculePropertiesQueue.remove( 0 );
            Future<Object> value = moleculeProperties.remove( key );
            value.cancel( false );
        }
        moleculeProperties.put( propertyKey, future );
        moleculePropertiesQueue.add( propertyKey );
    }

    public void setDataValue( int columnIndex, int rowIndex, Object newValue ) {
        throw new UnsupportedOperationException();
    }

    public int getNumberOfMolecules() {
        return getRowCount();
    }
}
