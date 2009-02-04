package net.bioclipse.cdk.ui.sdfeditor.editor;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.nebula.widgets.compositetable.AbstractSelectableRow;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.GridRowLayout;
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
        table.setRunTime( true );
    }

    @Override
    public Control getControl() {

        return table;
    }

    @Override
    public ISelection getSelection() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {

        table.refreshAllRows();
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {

        // TODO Auto-generated method stub

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
