package net.bioclipse.cdk.ui.sdfeditor.editor;

import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableViewer.Header;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculeTableViewer.Row;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.widgets.Composite;

@Deprecated 
public class MoleculeTable extends Composite {
    public final static int STRUCTURE_COLUMN_WIDTH = 200;

    CompositeTable cTable;
    IBaseLabelProvider labelProvider;
    MoleculeTableContentProvider contentProvider;

    public MoleculeTable(Composite parent, int style) {

        super( parent, style );

//        contentProvider= new MoleculeTableContentProvider();
//        contentProvider.inputChanged( null, null, getEditorInput() );

        cTable = new CompositeTable(parent, SWT.NULL);

        // get First element from list to determin Properties
        // use a iterator go get the first element and pass the property list to
        // header and row constructor
        new Header(cTable, SWT.NULL);
        new Row(cTable,SWT.NULL);
        cTable.setRunTime( true );
        // set initial number of rows








        // See what's currently selected and select it
//        ISelection selection =
//                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//                        .getSelectionService().getSelection();
//        if ( selection instanceof IStructuredSelection ) {
//            IStructuredSelection stSelection = (IStructuredSelection) selection;
//            //reactOnSelection( stSelection );
//        }
//
//        setupDragSource();


    }
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        IBaseLabelProvider oldLabelProvider = this.labelProvider;
        if(labelProvider == oldLabelProvider) {
            return;
        }
        // remove listener from old provider
        this.labelProvider = labelProvider;
        // add listener to new provider
        redraw(); // refresh
        //dispose old provider
    }

    public void setContentProvider(MoleculeTableContentProvider contentProvider) {
        Assert.isNotNull( contentProvider );
        MoleculeTableContentProvider oldContentProvider = this.contentProvider;


        this.contentProvider = contentProvider;



        if(oldContentProvider != null) {
            cTable.removeRowContentProvider( oldContentProvider );
            cTable.addRowContentProvider( contentProvider );
            cTable.setNumRowsInCollection(500 );
        }
            //this.contentProvider.inputChanged( null, oldInput, newInput );
        redraw(); // refresh
        //dispose old provider
    }

    protected void setupDragSource() {
        // TODO set up drag source for selected molecule(s)
    }

}
