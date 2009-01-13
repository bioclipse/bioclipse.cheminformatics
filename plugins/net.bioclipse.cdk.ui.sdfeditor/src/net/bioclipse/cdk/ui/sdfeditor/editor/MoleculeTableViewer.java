package net.bioclipse.cdk.ui.sdfeditor.editor;

import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor.Header;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor.Row;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class MoleculeTableViewer extends ContentViewer {
    
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
}
