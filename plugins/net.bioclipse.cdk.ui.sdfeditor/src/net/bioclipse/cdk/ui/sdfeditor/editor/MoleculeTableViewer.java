package net.bioclipse.cdk.ui.sdfeditor.editor;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.nebula.widgets.compositetable.AbstractSelectableRow;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.GridRowLayout;
import org.eclipse.swt.nebula.widgets.compositetable.IRowFocusListener;
import org.eclipse.swt.nebula.widgets.compositetable.RowConstructionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


public class MoleculeTableViewer extends ContentViewer {

    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    CompositeTable table;


    public MoleculeTableViewer(Composite parent, int style) {

        table = new CompositeTable(parent,style);
        new Header(table, SWT.NULL);
        new Row(table,SWT.NULL);

        table.addRowFocusListener( new IRowFocusListener() {

            public void arrive( CompositeTable sender, int currentObjectOffset,
                                Control newRow ) {

                updateSelection( getSelection() );

            }

            public void depart( CompositeTable sender, int currentObjectOffset,
                                Control row ) {

                updateSelection( getSelection() );

            }

            public boolean requestRowChange( CompositeTable sender,
                                             int currentObjectOffset,
                                             Control row ) {

                return true;
            }

        });
        table.addRowConstructionListener( new RowConstructionListener() {

            @Override
            public void headerConstructed( Control arg0 ) {

                // TODO Auto-generated method stub

            }

            @Override
            public void rowConstructed( Control arg0 ) {

                arg0.setMenu( table.getMenu() );

            }

        });
        table.setRunTime( true );
    }

    @Override
    public Control getControl() {

        return table;
    }

    @Override
    public ISelection getSelection() {

        if(getContentProvider() instanceof MoleculeTableContentProvider) {

            int selectedIndex = table.getSelection().y+table.getTopRow();
            MoleculeTableContentProvider contentProvider =
                            (MoleculeTableContentProvider)getContentProvider();

            if(selectedIndex >=0 ) {
                Object o = contentProvider.getMoleculeAt( selectedIndex );
                if ( o != null ) {
                  IStructuredSelection selection = new StructuredSelection( o );
                  return selection;
                }
            }
        }

        return StructuredSelection.EMPTY;
    }

    @Override
    public void refresh() {

        table.refreshAllRows();
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {

        // TODO Auto-generated method stub

    }

    protected void updateSelection(ISelection selection) {
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        fireSelectionChanged(event);
      }

    public static class Header extends Composite {

        public Header(Composite parent, int style) {

            super( parent, style );
            setLayout( new GridRowLayout( new int[] { 10,
                    STRUCTURE_COLUMN_WIDTH}, false ) );
            new Label( this, SWT.NULL ).setText( "Index" );
            new Label( this, SWT.NULL ).setText( "2D-Structure" );
        }
    }

    public static class Row extends AbstractSelectableRow {

        public Row(Composite parent, int style) {

            super( parent, style );
            setLayout( new GridRowLayout( new int[]
                       { 10,
                         STRUCTURE_COLUMN_WIDTH,
                         },
                    false ) );
            this.setColumnCount( 2 );
        }
        @Override
        public Point computeSize( int hint, int hint2 ) {
            Point p =super.computeSize( hint, hint2 );
            p.y = STRUCTURE_COLUMN_WIDTH;
            return p;
        }
    }
}
