package net.bioclipse.cdk.ui.sdfeditor.editor;

import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.GridRowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


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
                    STRUCTURE_COLUMN_WIDTH, 100 }, false ) );
            new Label( this, SWT.NULL ).setText( "Index" );
            new Label( this, SWT.NULL ).setText( "2D-Structure" );
            // new Label(this,SWT.NULL).setText( "Properties" );
        }
    }

    public static class Row extends AbstractSelectableRow {

        public Row(Composite parent, int style) {

            super( parent, style );
            setLayout( new GridRowLayout( new int[]
                       { 10,
                         STRUCTURE_COLUMN_WIDTH,
                         100 },
                    false ) );

            index = new Text( this, SWT.NULL );
            index.setEditable( false );
            structure = new JChemPaintWidget( this, SWT.NULL );
            structure.getRenderer2DModel().setShowExplicitHydrogens( false );
            structure.setMargin( 3 );
            structure.getRenderer2DModel().setIsCompact( true );

            this.add( index );
            this.add( structure );
            initialize();
        }

        public final Text             index;
        public final JChemPaintWidget structure;
    }
}
