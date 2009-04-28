package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.DefaultBodyConfig;
import net.sourceforge.nattable.config.DefaultColumnHeaderConfig;
import net.sourceforge.nattable.config.DefaultRowHeaderConfig;
import net.sourceforge.nattable.config.SizeConfig;
import net.sourceforge.nattable.data.IColumnHeaderLabelProvider;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.model.DefaultNatTableModel;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.renderer.AbstractCellRenderer;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class MoleculeTableViewer extends ContentViewer {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    NatTable table;
    Control headerControl;
    JCPCellPainter cellPainter;

    public MoleculeTableViewer(Composite parent, int style) {

        cellPainter = new JCPCellPainter();

        DefaultNatTableModel model = new DefaultNatTableModel();

        IColumnHeaderLabelProvider columnHeaderLabelProvider = new IColumnHeaderLabelProvider() {

            public String getColumnHeaderLabel( int col ) {
                List<Object> prop = getColumnHandler().getProperties();
                if(col == 0)
                    return "2D-structure";
                if(col<prop.size()+1 )
                    return prop.get(col-1).toString();
                return "";
            }
        };

        DefaultRowHeaderConfig rowHeaderConfig = new DefaultRowHeaderConfig();
        rowHeaderConfig.setRowHeaderColumnCount(1);
        SizeConfig rowHeaderColumnWidthConfig = new SizeConfig();
        rowHeaderColumnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/3);
        //              columnWidthConfig.setDefaultSize(150);
        rowHeaderColumnWidthConfig.setDefaultResizable(true);
        rowHeaderColumnWidthConfig.setIndexResizable( 0, true );
        rowHeaderConfig.setRowHeaderColumnWidthConfig( rowHeaderColumnWidthConfig );

        DefaultBodyConfig bodyConfig = new DefaultBodyConfig(new IDataProvider() {

            public int getColumnCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getColumnCount();
            }

            public int getRowCount() {
                if(getDataProvider()==null) return 0;
                return getDataProvider().getRowCount();
            }

            public Object getValue( int row, int col ) {
                if(getDataProvider()==null) return null;
                return getDataProvider().getValue( row, col );
            }

        });

        bodyConfig.setCellRenderer( new AbstractCellRenderer() {

            @Override
            public ICellPainter getCellPainter( int row, int col ) {

                if(col == 0)
                    return cellPainter;
                return super.getCellPainter( row, col );
            }

            public String getDisplayText( int row, int col ) {

                return getDataProvider().getValue( row, col ).toString();
            }

            public Object getValue( int row, int col ) {

                // TODO Auto-generated method stub
                return getDataProvider().getValue( row, col );
            }

        });

        model.setBodyConfig(bodyConfig);
        model.setRowHeaderConfig(rowHeaderConfig);
        model.setColumnHeaderConfig( new DefaultColumnHeaderConfig(columnHeaderLabelProvider));

        model.setSingleCellSelection( false );
        model.setMultipleSelection( true );
//        model.setMultipleSelection( true );
//        model.


        SizeConfig columnWidthConfig = model.getBodyConfig().getColumnWidthConfig();
        columnWidthConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH/2);
        columnWidthConfig.setInitialSize( 0, STRUCTURE_COLUMN_WIDTH );
        //              columnWidthConfig.setDefaultSize(150);
        columnWidthConfig.setDefaultResizable(true);
        columnWidthConfig.setIndexResizable(0, true);

        // Row heights
        SizeConfig rowHeightConfig = model.getBodyConfig().getRowHeightConfig();
        rowHeightConfig.setDefaultSize(STRUCTURE_COLUMN_WIDTH);
        rowHeightConfig.setDefaultResizable(true);
        //                rowHeightConfig.setIndexResizable(1, false);

        // NatTable
        table = new NatTable(parent,
                     SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE
                     | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL,
                     model
        );
        Listener listener = new Listener() {

            public void handleEvent( Event event ) {

                updateSelection( getSelection() );
            }
        };
        table.addListener( SWT.SELECTED, listener);
        table.addListener( SWT.MouseUp, listener );
    }

    @Override
    public Control getControl() {

        return table;
    }

    @Override
    public ISelection getSelection() {

        if(getContentProvider() instanceof MoleculeTableContentProvider) {

            int[] selected = table.getSelectionModel().getSelectedRows();

            if(selected.length==0) return StructuredSelection.EMPTY;

            IMoleculesEditorModel contentProvider =
                            getMoleculesEditorModel();

            List<ICDKMolecule> mols = new ArrayList<ICDKMolecule>(selected.length);
            for(int i:selected) {
                mols.add( contentProvider.getMoleculeAt( i ));
            }
            return new StructuredSelection(mols);
        }

        return StructuredSelection.EMPTY;
    }

    // FIXME Better handling to get data from the contentprovider or store it somewhere else
    private IMoleculesEditorModel getMoleculesEditorModel() {
        return (IMoleculesEditorModel)getContentProvider();
    }
    private IDataProvider getDataProvider() {
        return (IDataProvider)getContentProvider();
    }
    private IMoleculeTableColumnHandler getColumnHandler() {
        return (IMoleculeTableColumnHandler) getContentProvider();
    }

    @Override
    public void refresh() {
        if(!table.isDisposed()) {
            table.reset();
            table.redraw();
            table.updateResize();
            table.update();
        }
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
}
