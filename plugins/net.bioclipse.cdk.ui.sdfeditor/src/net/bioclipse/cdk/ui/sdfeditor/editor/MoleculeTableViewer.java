/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.bioclipse.cdk.domain.CDKMoleculePropertySource;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.Activator;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.BodyLayerStack;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.ColorProviderPainter;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.ColumnHeaderLayerStack;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.MolTableBodyMenuConfigurator;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.MolTableHeaderMenuConfigurator;
import net.bioclipse.cdk.ui.sdfeditor.editor.nattable.RowHeaderLayerStack;
import net.bioclipse.cdk.ui.sdfeditor.editor.painter.JmolCellPainter;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.DefaultNatTableStyleConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.data.DefaultRowHeaderDataProvider;
import net.sourceforge.nattable.grid.layer.CornerLayer;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.BodyOverrideConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.StructuralRefreshEvent;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.selection.Range;
import net.sourceforge.nattable.selection.config.DefaultSelectionStyleConfiguration;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.IPropertySource;


public class MoleculeTableViewer extends ContentViewer {

    public final static String STRUCTURE_LABEL = "StructureLable";
    public final static String COLOR_PROVIDER_LABEL = "ColorProived";
    Logger logger = Logger.getLogger( MoleculeTableViewer.class );

    NatTable table;
    JCPCellPainter cellPainter;
    JmolCellPainter jmolCellPainter;
    private BodyLayerStack bodyLayer;

    private int currentSelected;

    private Runnable dblClickHook;
    private ColumnHeaderLayerStack columnHeaderLayer;
    private GridLayer gridLayer;

    public MoleculeTableViewer(Composite parent, int style) {
       this( parent, style, null, null );
    }

    public MoleculeTableViewer( Composite parent, int style,
                                MenuManager headerMenuManager,
                                MenuManager bodyMenuManager) {
        // Data providers
        IDataProvider columnHeaderDataProvider = new IDataProvider() {

            public void setDataValue( int columnIndex, int rowIndex, Object newValue ) {
                throw new UnsupportedOperationException();
            }

            public int getRowCount() {
                return 1;
            }

            public Object getDataValue( int columnIndex, int rowIndex ) {
                return getColumnHeaderLabel(columnIndex);
            }

            public int getColumnCount() {
                //if(true) return 2;
                IMoleculeTableColumnHandler handler = getColumnHandler();
                if(handler!=null)
                    return getColumnHandler().getProperties().size()+1;
                else return 0;
            }

            private String getColumnHeaderLabel( int col ) {
                List<Object> prop = getColumnHandler().getProperties();
                if(col == 0)
                    return "2D-structure";
                if(col<prop.size()+1 )
                    return prop.get(col-1).toString();
                return "xxx";
            }
        };

        IDataProvider bodyDataProvider = new IDataProvider() {

            public int getColumnCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getColumnCount()+1;
            }

            public int getRowCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getRowCount();
            }

            public Object getDataValue( int columnIndex, int rowIndex ) {
                if(getDataProvider()==null) return "123";
                return getDataProvider().getDataValue( columnIndex, rowIndex  );
            }

            public void setDataValue( int columnIndex, int rowIndex,
                                      Object newValue ) {
                throw new UnsupportedOperationException();
            }

        };
        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider( bodyDataProvider );

        // Layers
        bodyLayer = new BodyLayerStack( bodyDataProvider );
        columnHeaderLayer = new ColumnHeaderLayerStack(
                                              columnHeaderDataProvider,
                                              bodyLayer,
                                              bodyLayer.getSelectionLayer() );
        RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(
                                              rowHeaderDataProvider,
                                              bodyLayer,
                                              bodyLayer.getSelectionLayer() );
        DefaultCornerDataProvider cornerDataProvider
                    = new DefaultCornerDataProvider( columnHeaderDataProvider,
                                                     rowHeaderDataProvider );
        CornerLayer cornerLayer = new CornerLayer( new DataLayer( cornerDataProvider ),
                                                   rowHeaderLayer,
                                                   columnHeaderLayer );
        gridLayer = new GridLayer( bodyLayer, columnHeaderLayer,
                                             rowHeaderLayer, cornerLayer );

     // Cell painting
        cellPainter = new JCPCellPainter();
        jmolCellPainter = new JmolCellPainter();
        TextPainter textPainter = new TextPainter();
        textPainter.setWrappedPainter( new ColorProviderPainter() );

        IConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute( EditConfigAttributes.CELL_EDITABLE_RULE,
                                                IEditableRule.NEVER_EDITABLE );

        configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER,
//                                              jmolCellPainter,
                                                cellPainter,
                                                DisplayMode.NORMAL,
                                                STRUCTURE_LABEL);
        configRegistry.registerConfigAttribute( CellConfigAttributes.CELL_PAINTER,
                                                textPainter,
                                                DisplayMode.NORMAL,
                                                COLOR_PROVIDER_LABEL);

        DataLayer bodyDataLayer = bodyLayer.getDataLayer();
        ColumnOverrideLabelAccumulator firstColumnLabelAccumulator
                = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        firstColumnLabelAccumulator.registerColumnOverrides( 0, STRUCTURE_LABEL );

        BodyOverrideConfigLabelAccumulator otherColumnAccumulator =
            new BodyOverrideConfigLabelAccumulator();
        otherColumnAccumulator.registerOverrides( COLOR_PROVIDER_LABEL );

        AggregrateConfigLabelAccumulator configLabelAccumulator =
            new AggregrateConfigLabelAccumulator();
        configLabelAccumulator.add( firstColumnLabelAccumulator, otherColumnAccumulator );
        bodyDataLayer.setConfigLabelAccumulator( configLabelAccumulator );

        // NatTable
        table = new NatTable(parent, gridLayer,false);
        table.setConfigRegistry( configRegistry );

        table.addConfiguration( new DefaultNatTableStyleConfiguration() );
        table.addConfiguration(new DefaultSelectionStyleConfiguration());

        if(headerMenuManager != null)
            table.addConfiguration(new MolTableHeaderMenuConfigurator(table,headerMenuManager));
        if(bodyMenuManager != null)
            table.addConfiguration( new MolTableBodyMenuConfigurator( table, bodyMenuManager ) );
        table.addConfiguration( new AbstractUiBindingConfiguration() {
            public void configureUiBindings( UiBindingRegistry uiBindingRegistry ) {
                uiBindingRegistry.registerDoubleClickBinding(
                  new MouseEventMatcher(SWT.NONE, GridRegion.BODY, 1) ,
                      new IMouseAction() {
                          public void run( NatTable natTable,
                                       MouseEvent event ) {
                          doubleClickHook();
                      }
                  });
            }
        });
        table.configure();
        table.addLayerListener(new ILayerListener(){
            public void handleLayerEvent(ILayerEvent event) {
                if(event instanceof RowSelectionEvent
                                || event instanceof CellSelectionEvent){
                    updateSelection( getSelection() );
                }
            }
        });

//        table.addConfiguration( new AbstractUiBindingConfiguration() {
//
//            public void configureUiBindings( UiBindingRegistry uiBindingRegistry ) {
//
//                uiBindingRegistry.registerFirstMouseDragMode(
//                    new MouseEventMatcher( SWT.NONE, GridRegion.DATAGRID, 1 ),
//                    new IDragMode() {
//
//                        Point start;
//
//                        public void mouseUp( NatTable natTable, MouseEvent event ) {
//
//                           start = null;
//                           logger.debug( "Drag stoped");
//
//                        }
//
//                        public void mouseMove( NatTable natTable, MouseEvent event ) {
//
//                            Point now = new Point(event.x,event.y);
//                            int x = now.x-start.x;
//                            int y = now.y-start.y;
//                            int diff2 = x*x+y*y;
//                            if(diff2 >100) {
//                                logger.debug( "Drag started" );
//                            }
//
//                        }
//
//                        public void mouseDown( NatTable natTable, MouseEvent event ) {
//
//                            start = new Point(event.x,event.y);
//
//                        }
//                    });
//
//            }
//        });

        table.setDragDetect( true );
        // COMMAND click -> select
        // drag select / drag molecule
    }

    public void addDropSupport( int operations, Transfer[] transferTypes,
                                DropTargetListener listener) {
        Control control = getControl();
        DropTarget target = new DropTarget( control, operations );
        target.setTransfer( transferTypes );
        target.addDropListener( listener );
    }

    public void addDragSupport( int operations, Transfer[] transferTypes,
                                DragSourceListener listener) {
        Control control = getControl();
        DragSource target = new DragSource( control, operations );
        target.setTransfer( transferTypes );
        target.addDragListener( listener );
    }

    @Override
    public Control getControl() {

        return table;
    }

    public static class MolTableElement implements IAdaptable {

        final IMoleculesEditorModel model;
        final int index;

        CDKMoleculePropertySource pSource;
        public MolTableElement(int index, IMoleculesEditorModel model) {
            this.model = model;
            this.index = index;
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter( Class adapter ) {

            if(adapter.isAssignableFrom( ICDKMolecule.class )) {
                return model.getMoleculeAt( index );
            }
            if (adapter.isAssignableFrom(IPropertySource.class)) {
                ICDKMolecule mol = model.getMoleculeAt( index );
                if(mol instanceof ICDKMolecule) {
                    if(pSource == null)
                        pSource=  new CDKMoleculePropertySource(mol);
                    return pSource;
                } else
                    return null;
            }
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
    }

    @Override
    public ISelection getSelection() {

        if(getContentProvider() instanceof MoleculeTableContentProvider) {

            Set<Range> selectedSet = bodyLayer.getSelectionLayer().getSelectedRows();
            if(selectedSet.isEmpty()) {
                currentSelected = -1;
                return StructuredSelection.EMPTY;
            }

            Range first = selectedSet.iterator().next();
            currentSelected = first.start;

            IMoleculesEditorModel model;
            if(getInput() instanceof IMoleculesEditorModel) {
                model = (IMoleculesEditorModel) getInput();

                if(selectedSet.size()==1 && first.end-first.start == 1) {
                    return new StructuredSelection(
                               new MolTableElement( first.start, model));
                }else {
                    List<Integer> ints = new ArrayList<Integer>();
                    for(Range range:selectedSet) {
                        ints.addAll( range.getMembers() );
                    }
                    Collections.sort( ints );
                    int[] values = new int[ints.size()];
                    for(int i=0;i<values.length;i++) values[i]=ints.get( i );
                    return new MolTableSelection( values, model);
                }
            }
        }

        return StructuredSelection.EMPTY;
    }

    public int[] getSelectedColumns() {
        return bodyLayer.getSelectionLayer().getSelectedColumns();
    }

    public int[] getSelectedRows() {
        Set<Range> ranges = bodyLayer.getSelectionLayer().getSelectedRows();
        List<Integer> ints = new LinkedList<Integer>();
        for(Range range:ranges) {
            ints.addAll( range.getMembers() );
        }
        Collections.sort( ints );
        int[] result = new int[ints.size()];
        for(int i=0;i<ints.size();i++) {
            result[i] = ints.get( i );
        }
        return result;
    }

    private IDataProvider getDataProvider() {
        return (IDataProvider)getContentProvider();
    }

    private IMoleculeTableColumnHandler getColumnHandler() {
        return (IMoleculeTableColumnHandler) getContentProvider();
    }

    public void resizeStructureColumn() {
        int value = Activator.getDefault().getPreferenceStore().getInt(
                                             Activator.STRUCTURE_COLUMN_WIDTH );
        bodyLayer.getDataLayer().setDefaultRowHeight( value );
        bodyLayer.getDataLayer().setColumnWidthByPosition( 0, value );
        logger.debug( "Resize column" );
        table.updateResize();
    }

    @Override
    public void refresh() {
        if(!table.isDisposed()) {
            refreshTable();
        }
    }

    void refreshTable() {
        AbstractLayer layer = bodyLayer.getDataLayer();
        layer.fireLayerEvent( new StructuralRefreshEvent( layer ) );
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {

        // TODO Auto-generated method stub

    }

     IRenderer2DConfigurator getRenderer2DConfigurator() {
        return cellPainter.getRenderer2DConfigurator();
    }

     void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        cellPainter.setRenderer2DConfigurator( renderer2DConfigurator);
    }


    protected void updateSelection(ISelection selection) {
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        fireSelectionChanged(event);
    }


    public int getFirstSelected() {

        return currentSelected;
    }

    public void setDoubleClickHook(Runnable hook) {
        dblClickHook = hook;
    }
    protected void doubleClickHook() {
        if(dblClickHook!=null) {
            dblClickHook.run();
        }
    }
}
