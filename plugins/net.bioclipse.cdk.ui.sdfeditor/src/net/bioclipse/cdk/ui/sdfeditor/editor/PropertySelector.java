package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class PropertySelector extends Composite{



    public PropertySelector(Composite parent, int style) {

        super( parent, style );
        Composite composite= new Composite(parent, SWT.NONE);
//        composite.setSize( 300,300 );
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        createControl( composite );
    }

    Collection<Object> properties = new HashSet<Object>();
    List<Object> viewProperties = new ArrayList<Object>(10);
    Collection<Object> moleculeTableProperties = new HashSet<Object>();

    TableViewer pTable;
    TableViewer vTable;



    private void createControl(Composite parent) {


        Composite leftCenterRightCompsite = new Composite(parent,SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true,true);
        gridData.heightHint = 20;
        leftCenterRightCompsite.setLayoutData( gridData );
        GridLayout gridLayout = new GridLayout(3,false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftCenterRightCompsite.setLayout( gridLayout );

        Composite leftComposite = new Composite(leftCenterRightCompsite,SWT.NONE);
        gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = 100;
        leftComposite.setLayoutData( gridData);
        gridLayout = new GridLayout(1,false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout( gridLayout );

        Composite centerComposite = new Composite(leftCenterRightCompsite,SWT.NONE);
        gridLayout = new GridLayout(1,false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        centerComposite.setLayout( gridLayout );
        centerComposite.setLayoutData( new GridData(SWT.CENTER,SWT.TOP,false,false));

        Composite rightCompsite = new Composite(leftCenterRightCompsite,SWT.NONE);
        gridData = new  GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = 100;
        rightCompsite.setLayoutData( gridData );
        gridLayout = new  GridLayout(1,false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        rightCompsite.setLayout( gridLayout );

        pTable = createTable( leftComposite, "Avaliable properties", properties );
        vTable = createTable( rightCompsite, "Visible properties", viewProperties);

        pTable.setInput(moleculeTableProperties);
        pTable.refresh( true );
        vTable.refresh(true);

        createButtonBar( centerComposite );

        pTable.getTable().setFocus();


    }

    private void initialSelectedElements() {

    }

    public void setInitialData(Object[] data) {
        // TODO get data from current table properties and all create the
        // two sets
        properties.addAll( Arrays.asList( data ) );
        pTable.refresh(true);
    }

    private TableViewer createTable( Composite parent, String text,
                              final Collection<Object> set) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText( text );
        label.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false) );

        TableViewer fTable = new TableViewer( parent,  SWT.BORDER
                                          |SWT.H_SCROLL
                                          |SWT.V_SCROLL
                                          |SWT.MULTI);

        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true);
        fTable.getControl().setLayoutData( gridData );

        fTable.setUseHashlookup( true );

        configureTable(fTable);

        fTable.setContentProvider( new IStructuredContentProvider() {

            public Object[] getElements( Object inputElement ) {
                return set.toArray();
            }

            public void dispose() {
            }

            public void inputChanged( Viewer viewer, Object oldInput,
                                      Object newInput ) {
            }
        });
        return fTable;
    }

    private void createButtonBar(Composite parent) {
        Label spacer = new Label (parent,SWT.NONE);
        spacer.setLayoutData( new GridData(SWT.FILL, SWT.TOP, true,false));

        final Button addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData( new GridData(SWT.FILL,SWT.TOP,true,false) );
        addButton.setText( "Add" );
        addButton.setEnabled( !pTable.getSelection().isEmpty() );

        final Button addAllButton = new Button(parent, SWT.PUSH);
        addAllButton.setLayoutData( new GridData(SWT.FILL,SWT.TOP,true,false) );
        addAllButton.setText( "Add All");
        addAllButton.setEnabled( pTable.getTable().getItems().length  >0 );

        final Button removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData( new GridData(SWT.FILL,SWT.TOP,true,false) );
        removeButton.setText( "Remove" );
        removeButton.setEnabled( !vTable.getSelection().isEmpty() );

        final Button removeAllButton = new Button(parent, SWT.PUSH);
        removeAllButton.setLayoutData( new GridData(SWT.FILL,SWT.TOP,true,false) );
        removeAllButton.setText( "Remove All" );
        removeAllButton.setEnabled( !vTable.getSelection().isEmpty() );

        pTable.addSelectionChangedListener( new ISelectionChangedListener() {

            public void selectionChanged( SelectionChangedEvent event ) {
                addButton.setEnabled( !event.getSelection().isEmpty() );
            }
        });

        addButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                // add selection

                removeAllButton.setEnabled( true );
                addAllButton.setEnabled( pTable.getTable().getItems().length >0 );
            }
        });

        pTable.addDoubleClickListener( new IDoubleClickListener() {

            public void doubleClick( DoubleClickEvent event ) {
                addSelection();

                removeAllButton.setEnabled( pTable.getTable().getItems().length >0 );
            }

        });

        vTable.addSelectionChangedListener( new ISelectionChangedListener() {

            public void selectionChanged( SelectionChangedEvent event ) {
                removeButton.setEnabled( !event.getSelection().isEmpty() );
            }
        });

        removeButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                removeSelection();

                addAllButton.setEnabled( true );
                removeAllButton.setEnabled( vTable.getTable().getItemCount() >0 );
            }
        });

    }

    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) pTable.getSelection();
        Object[] selectedElements = selection.toArray();
        vTable.add( selectedElements );
        pTable.remove(selectedElements);
        vTable.setSelection( selection );
        vTable.getControl().setFocus();
        //validate
    }

    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) pTable.getSelection();
        Object[] selectedElements = selection.toArray();
        pTable.add( selectedElements );
        vTable.remove( selectedElements );
        pTable.setSelection( selection );
        pTable.getControl().setFocus();
        // validate
    }

    private void configureTable( TableViewer table ) {

        table.setLabelProvider( new LabelProvider());

    }
}
