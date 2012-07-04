/* *****************************************************************************
 * Copyright (c) 2006, 2008-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openscience.cdk.io.formats.SDFFormat;

/**
 * The main page of the SDF properties wizard.
 * 
 * @author klas jonsson
 *
 */
public class SDFPropertiesImportWizardPage extends WizardPage {

	// An array to exclude data columns
	private boolean[] isExcluded;
	
	private Composite mainComposite, dataComposite, dataFrame;
	
	// Components for the file composite
	private Text fromFileTxt, toFileTxt;
	private Button fromFileButton, toFileButton;
	
	// Components for the data composite
	private Text[] headerText, dataText;
	private Button[] excludeButtons;
	private Combo[] headerCombo;
	
	// Components for the settings composite
	private Button noPropName;
	private Button[] decideOrder;
	private Combo txtCombo, sdfCombo;
	
	private ArrayList<ArrayList<String>> propertiesData;
	private ArrayList<String> sdfPropertyList, excludedProperties, headers;
	private int columns;
	private PropertiesImportFileHandler fileHandler = new PropertiesImportFileHandler();
	
	/**
	 * A constructor to use if there's one or several files selected.
	 * 
	 * @param pageName The name of the page
	 * @param selection The selections
	 */
	protected SDFPropertiesImportWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		columns = 0;
		propertiesData = new ArrayList<ArrayList<String>>();
		setTitle(pageName); //NON-NLS-1
		setDescription("Import properties to a SDF-file from a txt-file."); 
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
		setDescription("Import properties to a SDF-file from a txt-file."); 
		columns = 0;
		propertiesData = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * The main method for putting all the composites together.
	 */
	@Override
	public void createControl(Composite parent) {
		// mm, do I need this composite?
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, true));
		
		getFileComposit( mainComposite );
		createDataComposite( mainComposite );
		settingsComposite( mainComposite );
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
                FileDialog dlg = new FileDialog(mainComposite.getShell(), SWT.OPEN);
                String pathStr = dlg.open();
                updatePropertiesData(pathStr);
            }
        });
        
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
                FileDialog dlg = new FileDialog(mainComposite.getShell(), SWT.OPEN);
                String pathStr = dlg.open();
                try {
                    Path path = new Path(pathStr);
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                    fileHandler.setSDFile( file );
                    sdfPropertyList = fileHandler.getPropertiesFromSDFile();
                } catch ( FileNotFoundException e1 ) {
                    // TODO Add a log entry
                    e1.printStackTrace();
                }
                updateComponents();
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
	    return (fileHandler.sdFileExists() && fileHandler.dataFileExists());
	}
	
    /**
     * A get method to get the excluded properties, if non of the properties
     * are excluded it returns an empty {@link ArrayList}.
     * 
     * @return An <code>ArrayList</code> with the excluded properties
     */
	protected ArrayList<String> getExludedProerties() {
	    return excludedProperties;
	}
	
	/**
	 * This method check if all necessary fields are filled in.
	 */
	protected void updatePageComplite() {    
		String message = "";
		boolean complete = true;
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
				if (file instanceof SDFFormat) {
				    try {
                        fileHandler.setSDFile( file );
                    } catch ( FileNotFoundException e ) {
                        // TODO Add a log entry
                        e.printStackTrace();
                    }

				} else { //if (extention.toLowerCase().equals(".txt")) {
				    /* FIXME Here I just assumes that if it's not an sdf-file then its 
				     * the txt-file with properties, that is probably not good...*/
				    updatePropertiesData(file.getFullPath().toOSString());
				} 
			}
		}
	}
	
	/**
	 * This method is the first to be called when a new txt-file whith data is 
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
            // TODO Add a log entry
            e1.printStackTrace();
        }
	    headers = fileHandler.getPropertiesIDFromDataFile();
	    try {
            propertiesData = fileHandler.getTopValuesFromDataFile( 5 );
        } catch ( FileNotFoundException e ) {
            // TODO Add a log entry or what to do if this happens?
            e.printStackTrace();
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
	    dataFrame = new Composite( parent, SWT.BORDER );
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
	    // TODO Adapt to the new combo-box
        if (fileHandler.dataFileExists()) {
            headers = fileHandler.getPropertiesIDFromDataFile();
            try {
                propertiesData = fileHandler.getTopValuesFromDataFile( 5 );
            } catch ( FileNotFoundException e ) {
                e.printStackTrace();
            }
            columns = headers.size();
            rows = propertiesData.get( 0 ).size();
        }
        else {
            columns = 3;
            rows = 3;
            headers = new ArrayList<String>();
            headers.add( "N/A" );
            headers.add( "N/A" );
            headers.add( "N/A" );
            textInDataField = "n/a\nn/a\nn/a\nn/a\nn/a";
        }
        if (dataComposite != null)
            dataComposite.dispose();
        dataComposite = new Composite( dataFrame, SWT.NONE );
        dataComposite.setLayout( new GridLayout( columns + 1, true ) );
       
        new Label(dataComposite, SWT.NONE).setText( "Name" );
        GridData headersGridData = new GridData();
        headersGridData.horizontalAlignment = GridData.FILL;
        headersGridData.grabExcessHorizontalSpace = true;
        headersGridData.grabExcessVerticalSpace = true;
        headerText = new Text[columns];
        headerCombo = new Combo[columns];
        for (int i = 0; i < columns; i++) {
            if (noPropName != null && noPropName.getSelection()) {
                headerCombo[i] = new Combo( dataComposite, SWT.DROP_DOWN | SWT.BORDER);
                // TODO Add listerner
                if (sdfPropertyList != null && sdfPropertyList.size() > 0) {
                    for (int j = 0; j < sdfPropertyList.size(); j++)
                        headerCombo[i].add( sdfPropertyList.get( j ) );
                }               
            } else {
                headerText[i] = new Text( dataComposite, SWT.READ_ONLY | SWT.BORDER );
                headerText[i].setText( headers.get( i ) );
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
	        dataText[i] = new Text( dataComposite, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI);
	        dataText[i].setLayoutData( valuesGridData );
	        if ( textInDataField.isEmpty() )
	            for (int j = 1; j < rows; j++) {
	                textInDataField += propertiesData.get( i ).get( j ) + "\n";
	            }
	        dataText[i].setText( textInDataField );
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
	                    if ( e.equals( excludeButtons[i] ) )
	                        if (excludeButtons[i].getSelection())
	                            excludedProperties.add( propertiesData.get( i ).get( 0 ) );
	                        else
	                            excludedProperties.remove( propertiesData.get( i ).get( 0 ) );

	                updateComponents();
	            }
	        });
	    }
	       
	    if ( txtCombo != null ) {
	        txtCombo.removeAll();
	        for (int i = 0; i < headers.size(); i++)
	            txtCombo.add( headers.get( i ) );
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
	    
//	    Composite choosePropName = new Composite(view, SWT.NONE);
//	    choosePropName.setLayout( new GridLayout(2, false) );
	    
	    noPropName = new Button(view, SWT.CHECK);
	    noPropName.setText( "There is no properties name in the data file." );
	    noPropName.addSelectionListener( new SelectionListener() {
            
            @Override
            public void widgetSelected( SelectionEvent e ) {
                updateDataCompocite();
            }
            
            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {
                updateDataCompocite();
            }
        } );
	    
	    decideOrder = new Button[2];
	    
	    decideOrder[0] = new Button(view, SWT.RADIO);
	    decideOrder[0].setText("The order of the data file is as in the SDF file");
	    decideOrder[0].setSelection( true );
	    
	    Composite rbComposite = new Composite(view, SWT.NONE);
	    rbComposite.setLayout( new GridLayout(4, false) );
	    
	    decideOrder[1] = new Button(rbComposite, SWT.RADIO);
        decideOrder[1].setText("Link data by column ");
        decideOrder[1].setSelection( false );
	    txtCombo = new Combo(rbComposite, SWT.DROP_DOWN | SWT.BORDER);
	    for (int i = 0; i < headers.size(); i++)
	        txtCombo.add( headers.get( i ) );
	    new Label(rbComposite, SWT.NONE).setText( " to SDF-property " );
	    sdfCombo = new Combo(rbComposite, SWT.DROP_DOWN | SWT.BORDER);
	    sdfPropertyList = fileHandler.getPropertiesFromSDFile();
	    if (sdfPropertyList != null && sdfPropertyList.size() > 0) {
	        for (int i = 0; i < sdfPropertyList.size(); i++)
	            sdfCombo.add( sdfPropertyList.get( i ) );
	    }
	    
        return view;
	}
	
	/**
	 * This method update the different components.
	 */
	private void updateComponents() {
	    for (int i = 0; i < columns; i++) {
            isExcluded[i] = excludeButtons[i].getSelection();
            headerText[i].setEnabled( !isExcluded[i] ); 
            dataText[i].setEnabled( !isExcluded[i] );
	    }
	    
	    if (fileHandler.dataFileExists())
	        fromFileTxt.setText( fileHandler.getDataFilePath() );
	    else {
	        fromFileTxt.setText( "" );//"Add the file with the properties data here" );
	    }
	    
	    if (fileHandler.sdFileExists())
	        toFileTxt.setText( fileHandler.getSDFilePath() );
	    else {
	        toFileTxt.setText( "" );//"Add the SD-file here" ); 
	    }
	    
	    sdfCombo.removeAll();
	    for (int i = 0; i < propertiesData.size(); i++)
	        sdfCombo.add( propertiesData.get( i ).get( 0 ) );
	    
	    mainComposite.redraw();
	    mainComposite.update();
	}

}
