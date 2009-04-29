package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
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
        setLayout(new GridLayout(1,true));
        setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        createControl( this );
    }

    List<Object> viewProperties = new ArrayList<Object>(10);
    Collection<Object> moleculeTableProperties = new HashSet<Object>();

    boolean useGenerators = false;

    TableViewer pTable;
    TableViewer vTable;

    Button removeAll;
    Button addAll;

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

        pTable = createTable( leftComposite, "Avaliable properties",
                              moleculeTableProperties );
        vTable = createTable( rightCompsite, "Visible properties",
                              viewProperties);

        vTable.setInput( viewProperties );
        pTable.setInput(moleculeTableProperties);
        pTable.refresh( true );
        vTable.refresh(true);

        createButtonBar( centerComposite );

        Composite comp = new Composite(parent,SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.BOTTOM, true,false);
        gridData.heightHint = 20;
        comp.setLayoutData( gridData );

        final Button generatorButton = new Button(parent, SWT.CHECK);
        generatorButton.setLayoutData( new GridData(SWT.FILL,SWT.BOTTOM,true,false) );
        generatorButton.setText( "Use external generators" );
        generatorButton.setSelection( useGenerators);

        generatorButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                useGenerators = generatorButton.getSelection();
            }
        });

        pTable.getTable().setFocus();
    }

    public void setInitialData(List<Object> visible,Collection<Object> available) {
        // TODO get data from current table properties and all create the
        // two sets
        viewProperties.clear();
        viewProperties.addAll( visible );
        moleculeTableProperties.clear();
        moleculeTableProperties.addAll(available);
        moleculeTableProperties.removeAll( viewProperties );
        pTable.refresh();
        vTable.refresh();
        addAll.setEnabled( pTable.getTable().getItemCount() >0 );
        removeAll.setEnabled( vTable.getTable().getItemCount() >0 );
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
            Collection<?> model;
            public Object[] getElements( Object inputElement ) {
                return model.toArray();
            }

            public void dispose() {
            }

            public void inputChanged( Viewer viewer, Object oldInput,
                                      Object newInput ) {
                model = (Collection<?>) newInput;
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
        removeAllButton.setEnabled( vTable.getTable().getItemCount() >0 );



        pTable.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent event ) {
                addButton.setEnabled( !event.getSelection().isEmpty() );
            }
        });

        addButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                addSelection();

                removeAllButton.setEnabled( true );
                addAllButton.setEnabled( pTable.getTable().getItems().length >0 );
            }
        });

        pTable.addDoubleClickListener( new IDoubleClickListener() {

            public void doubleClick( DoubleClickEvent event ) {
                addSelection();

                removeAllButton.setEnabled( vTable.getTable().getItemCount() >0 );
            }

        });

        vTable.addDoubleClickListener( new IDoubleClickListener() {

            public void doubleClick( DoubleClickEvent event ) {
                removeSelection();

                addAllButton.setEnabled( vTable.getTable().getItemCount() >0 );
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

        removeAllButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection =
                                (IStructuredSelection) vTable.getSelection();
                moleculeTableProperties.addAll( viewProperties );
                viewProperties.clear();
                vTable.refresh();
                pTable.refresh();
                pTable.setSelection( selection );
                pTable.getControl().setFocus();

                removeAllButton.setEnabled( false );
                addAllButton.setEnabled( true );
            }
        });

        addAllButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection =
                                (IStructuredSelection) vTable.getSelection();
                viewProperties.addAll( moleculeTableProperties );
                moleculeTableProperties.clear();
                vTable.refresh();
                pTable.refresh();
                vTable.setSelection( selection );
                vTable.getControl().setFocus();

                removeAllButton.setEnabled( true );
                addAllButton.setEnabled( false );
            }
        });

        addAll = addAllButton;
        removeAll = removeAllButton;
    }

    private void addSelection() {
        IStructuredSelection selection = (IStructuredSelection) pTable.getSelection();
        viewProperties.addAll(selection.toList());
        moleculeTableProperties.removeAll( selection.toList() );
        vTable.refresh();
        pTable.refresh();
        vTable.setSelection( selection );
        vTable.getControl().setFocus();
    }

    private void removeSelection() {
        IStructuredSelection selection = (IStructuredSelection) vTable.getSelection();
        viewProperties.removeAll( selection.toList());
        moleculeTableProperties.addAll( selection.toList() );
        pTable.refresh();
        vTable.refresh();
        pTable.setSelection( selection );
        pTable.getControl().setFocus();
    }

    private void configureTable( TableViewer table ) {

        table.setLabelProvider( new LabelProvider());

    }

    public List<Object> getVisibleProperties() {
        return new ArrayList<Object>(viewProperties);
    }


    public boolean isUseGenerators() {

        return useGenerators;
    }


}
