/* *****************************************************************************
 * Copyright (c) 2007-2012 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *    
 ******************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The main page of the SDF properties wizard.
 * 
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class SDFPropertiesImportWizardPage extends WizardPage {

    private Logger logger = Logger.getLogger( this.getClass() );
    
    // An array to exclude data columns
    private boolean[] isExcluded;
    private boolean dataFileIncludeName =  true;
    private ArrayList<String> excludedProperties;
    
    private Composite mainComposite, dataComposite, settingsComposite;
    private ScrolledComposite dataFrame;
    
    // Components for the file composite
    private Text fromFileTxt, toFileTxt;
    private Button fromFileButton, toFileButton;

    // Components for the data composite
    private Text[] headerText, dataText;
    private Button[] excludeButtons;
    private Combo[] headerCombo;

    // Components for the settings composite
    private Button[] noPropName;
    private Button[] decideOrder;
    private Combo txtCombo, sdfCombo;

    private ArrayList<ArrayList<String>> propertiesData;
    private ArrayList<String> sdfPropertyList, headers, names;

    private int columns;
    
    private PropertiesImportFileHandler fileHandler = 
            new PropertiesImportFileHandler();

    /**
     * A constructor to use if there's one or several files selected.
     * 
     * @param pageName The name of the page
     * @param selection The selections
     */
    protected SDFPropertiesImportWizardPage(String pageName, 
                                            IStructuredSelection selection) {
        super(pageName);
        columns = 0;
        propertiesData = new ArrayList<ArrayList<String>>();
        excludedProperties = new ArrayList<String>();
        setTitle(pageName); //NON-NLS-1
        setDescription("Import properties to a SDF-file from a txt- or csv-file."); 
        init(selection);
    }

    /**
     * A constructor to use if there's no file selected.
     * 
     * @param pageName The name of the page
     */
    protected SDFPropertiesImportWizardPage(String pageName) {
        super(pageName);
        setTitle(pageName); //NON-NLS-1
        setDescription("Import properties to a SDF-file from a txt- or csv-file."); 
        columns = 0;
        propertiesData = new ArrayList<ArrayList<String>>();
        excludedProperties = new ArrayList<String>();
    }

    /**
     * The main method for putting all the composites together.
     */
    @Override
    public void createControl(Composite parent) {
        mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, true));

        getFileComposit( mainComposite );
        createDataComposite( mainComposite );
        settingsComposite = settingsComposite( mainComposite );
        updateComponents();
        mainComposite.pack();
        setControl( mainComposite );
    }

    /**
     * Create the composite that handles the loading of the files.
     * 
     * @param parent The composite where to put it
     * @return A composite containing the components needed.
     */
    private Composite getFileComposit(Composite parent) {
        GridData txtGridData = new GridData();
        txtGridData.horizontalAlignment = GridData.FILL;
        txtGridData.grabExcessHorizontalSpace = true;
        GridData labelGridData = new GridData();
        labelGridData.horizontalSpan = 2;
        labelGridData.horizontalAlignment = GridData.FILL;
        Composite fileComposite = new Composite(parent, SWT.NONE);
        fileComposite.setLayout(new GridLayout(2, false));
        fileComposite.setLayoutData( txtGridData );

        // The components for the sdf-file...
        Label toFileLabel = new Label(fileComposite, SWT.NONE);
        toFileLabel.setText("SDF-file:");
        toFileLabel.setLayoutData( labelGridData );
        toFileTxt = new Text(fileComposite, SWT.BORDER);
        toFileTxt.setText("");
        toFileTxt.setLayoutData( txtGridData );
        toFileTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updatePageComplite();   
            }   
        });
        toFileButton = new Button(fileComposite, SWT.PUSH);
        toFileButton.setText("Browse...");
        toFileButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(mainComposite.getShell(), 
                                                SWT.OPEN);
                String pathStr = dlg.open();
                try {
                    Path path = new Path(pathStr);
                    IFile file = ResourcesPlugin.getWorkspace().getRoot()
                            .getFile(path);
                    fileHandler.setSDFile( file );
                    sdfPropertyList = fileHandler.getPropertiesFromSDFile();
                } catch ( FileNotFoundException e1 ) {
                    logger.error( e1 );
                }
                updateComponents();
            }
        });

        // The components for the txt-file...
        Label fromFileLabel = new Label(fileComposite, SWT.NONE);
        fromFileLabel.setText("File with the propertis:");
        fromFileLabel.setLayoutData( labelGridData );
        fromFileTxt = new Text(fileComposite, SWT.BORDER);
        fromFileTxt.setText("");
        fromFileTxt.setLayoutData( txtGridData );
        fromFileTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updatePageComplite();   
            }   
        });
        fromFileButton = new Button(fileComposite, SWT.PUSH);
        fromFileButton.setText("Browse...");
        fromFileButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(mainComposite.getShell(),
                                                SWT.OPEN);
                String pathStr = dlg.open();
                updatePropertiesData(pathStr);
            }
        });
        
        return fileComposite;
    }

    /**
     * The only demands to complete the page are that we have both the files.
     * 
     *  @return <code>True</code> if the both files are known
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /**
     * A get method to get the excluded properties, if non of the properties
     * are excluded it returns an empty {@link ArrayList}.
     * 
     * @return An <code>ArrayList</code> with the excluded properties
     */
    protected ArrayList<String> getExcludedProerties() {
        return excludedProperties;
    }

    /**
     * This method check if all necessary fields are filled in.
     */
    protected void updatePageComplite() {    
        String message = "";
        boolean complete = true, headersFilledIn = true;
        int unFilledHeaders = 0;
        if (fromFileTxt.getText().isEmpty()) {
            complete = false;
            message = "Please add the file with the proerties";
        }
        if (toFileTxt.getText().isEmpty()) {
            if (!complete)
                message += " and the sd-file";
            else
                message = "Please add the sd-file";
            complete = false;
        }
        if (!dataFileIncludeName) {
            for (int i = 0; i < headerCombo.length; i++) {
                if (headerCombo[i].getText().isEmpty() && headerCombo[i]
                        .isEnabled() ) {
                    headersFilledIn = false;
                    unFilledHeaders++;
                }
            }
            if ( !headersFilledIn && message.isEmpty() ) {
                message = "Please fill in the " + unFilledHeaders + 
                        " missing properties names.";
                complete = false;
            }
        }
          
        if (!complete)
            setErrorMessage(message);
        else
            setErrorMessage(null);
        
        setPageComplete(complete);
    }

    /**
     * A method used to take care of a selection.
     * 
     * @param selection The selection
     */
    protected void init(ISelection selection) {
        if (!(selection instanceof IStructuredSelection) || selection.isEmpty())
            return;

        Iterator<?> itr = ((IStructuredSelection) selection).iterator();
        Object item;
        IFile file;
        while (itr.hasNext()) {
            item = itr.next();
            if (item instanceof IFile) {
                file = (IFile) item;
                if (file.getFileExtension().toLowerCase().equals( "sdf" ) ||
                        file.getFileExtension().toLowerCase().equals( "sd" ) ) {
                    try {
                        fileHandler.setSDFile( file );
                        fileHandler.setPathToNewSDFile( file.getLocation()
                                                        .toOSString() );
                    } catch ( FileNotFoundException e ) {
                        logger.error( e );
                    }

                } else { 
                    /* TODO Here I just assumes that if it's not an sdf-file 
                     * then its the txt-file with properties, that is probably 
                     * not good...*/
                    try {
                        fileHandler.setDataFile( file );
                        fileHandler.setPathToNewSDFile( file.getLocation()
                                                        .toOSString() );
                    } catch ( FileNotFoundException e ) {
                        logger.error( e );
                    }
                } 
            }
        }
    }

    /**
     * This method is the first to be called when a new txt-file with data is 
     * read.
     * 
     * @param pathStr The path to the txt-file
     */
    private void updatePropertiesData(String pathStr) {
        try {
            Path path = new Path(pathStr);
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            fileHandler.setDataFile( file );
        } catch ( FileNotFoundException e1 ) {
            logger.error( e1 );
        }
        headers = fileHandler.getPropertiesIDFromDataFile();
        try {
            propertiesData = fileHandler.getTopValuesFromDataFile( 5 );
        } catch ( FileNotFoundException e ) {
            logger.error( e );
        }

        updateDataCompocite();
        updateComponents();
    }

    /**
     * This method creates the composite that shows (some of) the data in the 
     * txt-file with the data that shall be added to the sd-file.
     * 
     * @param parent The parent composite, i.e. mainComposite
     * @return A composite for showing the data in the data file 
     */
    private Composite createDataComposite(Composite parent) {
        GridData headerGridData = new GridData();
        headerGridData.horizontalAlignment = GridData.FILL;
        headerGridData.grabExcessHorizontalSpace = true;
        
        dataFrame = new ScrolledComposite( parent, SWT.BORDER | SWT.V_SCROLL |
                                           SWT.H_SCROLL );
        dataFrame.setLayout( new GridLayout( 1, true ) );
        dataFrame.setLayoutData( headerGridData );
        
        updateDataCompocite();
        
        return dataFrame;
    }

    /**
     * Update the components that are related to the properties.
     */
    private void updateDataCompocite() {
        int rows;
        String textInDataField = "";
        if (fileHandler.dataFileExists()) {
            try {
                propertiesData = fileHandler.getTopValuesFromDataFile( 5 );
                columns = propertiesData.size();
                rows = propertiesData.get( 0 ).size();
            } catch ( FileNotFoundException e ) {
                rows = 0;
                columns = 0;
                e.printStackTrace();
            }

            if (headerCombo == null || dataFileIncludeName ) {       
                headers = fileHandler.getPropertiesIDFromDataFile();            
            } else {
                if (headers == null)
                    headers = new ArrayList<String>();
                for ( int i = 0; i < columns; i++ )
                    headers.add( "" );
            }

        }
        else {
            columns = 3;
            rows = 3;
            if (headers == null)
                headers = new ArrayList<String>();
            if (headerCombo == null || dataFileIncludeName ) {
                headers.add( "N/A" );
                headers.add( "N/A" );
                headers.add( "N/A" );
                textInDataField = "n/a\nn/a\nn/a\nn/a\nn/a";
            } else {
                for ( int i = 0; i < columns; i++ )
                    headers.add( "" );
            }
        }

        if (dataComposite != null)
            dataComposite.dispose();
        dataComposite = new Composite( dataFrame, SWT.NONE );
        dataComposite.setLayout( new GridLayout( columns + 1, true ) );
        dataFrame.setContent( dataComposite );
        
        new Label(dataComposite, SWT.NONE).setText( "Name" );
        GridData headersGridData = new GridData();
        headersGridData.horizontalAlignment = GridData.FILL;
        headersGridData.grabExcessHorizontalSpace = true;
        headersGridData.grabExcessVerticalSpace = true;
        headerText = new Text[columns];
        headerCombo = new Combo[columns];
        for (int i = 0; i < columns; i++) {
            if (noPropName != null && !dataFileIncludeName ) {
                headerCombo[i] = new Combo( dataComposite, SWT.DROP_DOWN |
                                            SWT.BORDER);
                headerCombo[i].addSelectionListener( new SelectionListener() {

                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        for (int i = 0; i < columns; i++)
                            if (e.equals( headerCombo[i] ) )
                                headers.set( i, headerCombo[i].getItem( i ) );
                        updatePageComplite();
                    }

                    @Override
                    public void widgetDefaultSelected( SelectionEvent e ) {  }
                    
                } );
                headerCombo[i].addKeyListener( new KeyListener() {
                    
                    @Override
                    public void keyReleased( KeyEvent e ) {
                        for (int i = 0; i < columns; i++)
                            if (e.equals( headerCombo[i] ) )
                                headers.set( i, headerCombo[i].getText() );
                        updatePageComplite();                   
                    }
                    
                    @Override
                    public void keyPressed( KeyEvent e ) {  }
                    
                } );
                
                if (sdfPropertyList != null && sdfPropertyList.size() > 0) {
                    headerCombo[i].add( "" );
                    for (int j = 0; j < sdfPropertyList.size(); j++)
                        headerCombo[i].add( sdfPropertyList.get( j ) );
                    if ( columns >= sdfPropertyList.size() ) {
                        headerCombo[i].select( i );
                        headers.add( headerCombo[i].getItem( i ) );
                    } else {
                        headerCombo[i].select( 0 );
                        headers.add( headerCombo[i].getItem( 0 ) );
                    }
                } 
                else {
                    // It should not end-up here
                    headers.clear();
                    headerCombo[i].clearSelection();
                }
            } else {
                headerText[i] = new Text( dataComposite, SWT.READ_ONLY | 
                                          SWT.BORDER );
                if ( i < headers.size() )
                    headerText[i].setText( headers.get( i ) );
                else
                    headerText[i].setText( "" );
                headerText[i].setLayoutData( headersGridData );
            }
        }

        new Label(dataComposite, SWT.NONE).setText( "Values" );
        GridData valuesGridData = new GridData();
        valuesGridData.horizontalAlignment = GridData.FILL;
        valuesGridData.grabExcessHorizontalSpace = true;
        valuesGridData.grabExcessVerticalSpace = true;
        dataText = new Text[columns];

        for (int i = 0; i < columns; i++) {	
            dataText[i] = new Text( dataComposite, SWT.READ_ONLY | SWT.BORDER |
                                    SWT.MULTI);
            dataText[i].setLayoutData( valuesGridData );
            if ( textInDataField.isEmpty() && fileHandler.dataFileExists() )
                for (int j = 0; j < rows; j++) {
                    textInDataField += propertiesData.get( i ).get( j ) + "\n";
                }
            dataText[i].setText( textInDataField );
            if ( fileHandler.dataFileExists() )
                textInDataField = "";
        }    

        new Label(dataComposite, SWT.NONE).setText( "Exclude" );
        GridData excludeGridData = new GridData();
        excludeGridData.horizontalAlignment = GridData.CENTER;
        isExcluded = new boolean[columns];
        excludeButtons = new Button[columns];
        for (int i = 0; i < columns; i++) {
            excludeButtons[i] = new Button( dataComposite, SWT.CHECK );
            excludeButtons[i].setLayoutData( excludeGridData );
            excludeButtons[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    for (int i = 0; i < excludeButtons.length; i++)
                        if ( e.getSource().equals( excludeButtons[i] ) )
                            if (excludeButtons[i].getSelection())
                                excludedProperties.add( headers.get( i ) );
                            else
                                excludedProperties.remove( headers.get( i ) );

                    updateComponents();
                }
            });
        }

        if ( txtCombo != null ) {
            txtCombo.removeAll();
            for (int i = 0; i < headers.size(); i++)
                txtCombo.add( headers.get( i ) );
            txtCombo.select( 0 );
        }

        dataComposite.pack();
        dataFrame.update();
        dataFrame.redraw();
    }

    /**
     * This method creates the component containing the two radio-buttons that 
     * are used to choose how to add the properties to the sd-file.
     * 
     * @param parent The parent composite, i.e. mainComposite 
     * @return A composite containing the two radio-buttons mention above
     */
    private Composite settingsComposite(Composite parent) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        Composite view = new Composite(parent, SWT.NONE);
        view.setLayout( new GridLayout(1, true) );
        view.setLayoutData( gridData );

        Group propName = new Group(view, SWT.SHADOW_ETCHED_OUT);
        propName.setText( "Properties name" );
        propName.setLayout( new GridLayout( 1, true) );
        noPropName = new Button[2];
        noPropName[0] = new Button(propName, SWT.RADIO);
        noPropName[0].setText("Data file includes properties name in row one");
        noPropName[0].setSelection( true );
        noPropName[0].addSelectionListener( propNameListener );
        noPropName[1] = new Button(propName, SWT.RADIO);
        noPropName[1].setText( "Data file does not includes properties name" );
        noPropName[1].addSelectionListener( propNameListener );

        Group dataConn = new Group(view, SWT.SHADOW_ETCHED_OUT);
        dataConn.setText( "Data connection" );
        dataConn.setLayout( new GridLayout(4, false) );
        decideOrder = new Button[2];
        decideOrder[0] = new Button(dataConn, SWT.RADIO);
        decideOrder[0].setText( "The order of the data file is as in the SD-" +
        		"file" );
        decideOrder[0].setSelection( true );
        GridData decideOrder0 = new GridData();
        decideOrder0.grabExcessHorizontalSpace = true;
        decideOrder0.horizontalSpan = 4;
        decideOrder[0].setLayoutData( decideOrder0 );
        decideOrder[0].addSelectionListener( dataConnListener );
        
        decideOrder[1] = new Button(dataConn, SWT.RADIO);
        decideOrder[1].setText("Link data by column ");
        decideOrder[1].setSelection( false );
        decideOrder[1].addSelectionListener( dataConnListener );
        txtCombo = new Combo(dataConn, SWT.DROP_DOWN | SWT.BORDER | 
                             SWT.READ_ONLY);
        for (int i = 0; i < headers.size(); i++)
            txtCombo.add( headers.get( i ) );
        txtCombo.select( 0 );
        txtCombo.addSelectionListener( dataConnListener );
        new Label(dataConn, SWT.NONE).setText( " to the SD file property " );
        sdfCombo = new Combo(dataConn, SWT.DROP_DOWN | SWT.BORDER | 
                             SWT.READ_ONLY);
        sdfPropertyList = fileHandler.getPropertiesFromSDFile();
        if (sdfPropertyList != null && sdfPropertyList.size() > 0) {
            for (int i = 0; i < sdfPropertyList.size(); i++)
                sdfCombo.add( sdfPropertyList.get( i ) );
        }
        sdfCombo.addSelectionListener( dataConnListener );
        
        return view;
    }

    /**
     * This method update the different components.
     */
    private void updateComponents() {
        for (int i = 0; i < columns; i++) {
            isExcluded[i] = excludeButtons[i].getSelection();
            if (dataFileIncludeName) {
                headerText[i].setEnabled( !isExcluded[i] );
                headerText[i].setText( headers.get( i ) );
            } else {
                headerCombo[i].setEnabled( !isExcluded[i] );
                headerCombo[i].removeAll();
                for ( int j =0; j < headers.size(); j++ )
                    headerCombo[i].add( headers.get( j ) );
            }
            dataText[i].setEnabled( !isExcluded[i] );   
        }

        if (fileHandler.dataFileExists())
            fromFileTxt.setText( fileHandler.getDataFilePath() );
        else {
            fromFileTxt.setText( "" );
        }

        if (fileHandler.sdFileExists())
            toFileTxt.setText( fileHandler.getSDFilePath() );
        else {
            toFileTxt.setText( "" ); 
        }

        sdfCombo.removeAll();
        for (int i = 0; i < sdfPropertyList.size(); i++)
            sdfCombo.add( sdfPropertyList.get( i ) );
        sdfCombo.select( 0 );
        sdfCombo.pack();

        updatePageComplite();
        
        settingsComposite.pack();
        mainComposite.redraw();
        mainComposite.update();
    }
    
    /**
     * This method initialize the the process of adding the properties from the 
     * txt-file to the sd-file.
     */
    protected void meargeFiles(IProgressMonitor monitor) {
        try {
            fileHandler.meargeFiles( isExcluded, names, dataFileIncludeName, monitor );
        } catch ( FileNotFoundException e ) {
            logger.error( e );
        }
        /* TODO Here I would like to update Bioclipse navigator field, so the 
         * new file becomes visibly. How do I do that? */
    }
    
    public void updateNameArray() {
        names = new ArrayList<String>();
        if ( dataFileIncludeName ) {
            for ( int i = 0; i < headerText.length; i++ ) {
                names.add( headerText[i].getText() );
            }
        } else {
            for ( int i = 0; i < headerCombo.length; i++ ) {
                names.add( headerCombo[i].getText() );
            }
        }  
    }
    
    /**
     * This is the implementation of the listener for the two top radio-buttons 
     * in the settings composite.
     */
    private SelectionListener propNameListener = new SelectionListener() {

        @Override
        public void widgetSelected( SelectionEvent e ) {
            if ( noPropName[0].getSelection() && e.getSource()
                    .equals( noPropName[0] ) ) {
                fileHandler.propertiesNameInDataFile( true );
                dataFileIncludeName = true;
                updateDataCompocite();
            }
            if ( noPropName[1].getSelection() && e.getSource()
                    .equals( noPropName[1] ) ) {
                fileHandler.propertiesNameInDataFile( false );
                dataFileIncludeName = false;
                updateDataCompocite();
            }
            updatePageComplite();
        }

        @Override
        public void widgetDefaultSelected( SelectionEvent e ) {  }
        
    };
    
    /**
     * This is the implementation of the listener for the two bottom radio-
     * buttons in the settings composite.
     */
    private SelectionListener dataConnListener = new SelectionListener() {

        @Override
        public void widgetSelected( SelectionEvent e ) {
            if ( decideOrder[0].getSelection() && e.getSource()
                    .equals( decideOrder[0] ) ) {
                fileHandler.setLinkProperties( false, null, null );
            }
            if ( decideOrder[1].getSelection() && e.getSource()
                    .equals( decideOrder[1] ) ) {
                fileHandler.setLinkProperties(true, 
                                               txtCombo.getItem
                                               (txtCombo.getSelectionIndex()),
                                               sdfCombo.getItem
                                               (sdfCombo.getSelectionIndex()));                
            }
            if ( e.getSource().equals( txtCombo ) ) {
                fileHandler.setdataFileLink( txtCombo
                                             .getItem( txtCombo
                                                       .getSelectionIndex() ) );
            }
            if ( e.getSource().equals( sdfCombo ) ) {
                fileHandler.setsdFileLink( sdfCombo
                                           .getItem( sdfCombo
                                                     .getSelectionIndex() ) );
            }
        }

        @Override
        public void widgetDefaultSelected( SelectionEvent e ) {  }
        
    };
    
}
