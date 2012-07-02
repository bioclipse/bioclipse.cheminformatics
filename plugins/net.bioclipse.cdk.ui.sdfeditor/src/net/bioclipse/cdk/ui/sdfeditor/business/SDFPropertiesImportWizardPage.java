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
	
	Composite mainComposite;
	
	// Components for the file composite
	private Text fromFileTxt, toFileTxt;
	private Button fromFileButton, toFileButton;
	
	// Components for the data composite
	private Label headerLabel, dataLable, excludeLable;
	private Text[] headerText, dataText;
	private Button[] excludeButtons;
	
	// Components for the settings composite
	private Button[] decideOrder;
	private Combo txtCombo, sdfCombo;
	
//	private String sdFilePath = "", txtFilePath = "";
	private ArrayList<ArrayList<String>> propertiesData;
	private ArrayList<String> sdfPropertyList, excludedProperties, headers;
	private int columns;
	private PropertiesImportFileHandler fileHandler = new PropertiesImportFileHandler();
	
	protected SDFPropertiesImportWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		columns = 0;
		propertiesData = new ArrayList<ArrayList<String>>();
//		fileHandler = new PropertiesImportFileHandler();
		setTitle(pageName); //NON-NLS-1
		setDescription("Import properties to a SDF-file from a txt-file."); 
		init(selection);
	}

	protected SDFPropertiesImportWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName); //NON-NLS-1
		setDescription("Import properties to a SDF-file from a txt-file."); 
		columns = 0;
		propertiesData = new ArrayList<ArrayList<String>>();
//		fileHandler = new PropertiesImportFileHandler();
//		fileHandler = new PropertiesImportFileHandler();
	}
	
	@Override
	public void createControl(Composite parent) {
		// mm, do I need this composite?
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, true));
		
		getFileComposit( mainComposite );
		getDataComposite( mainComposite );
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
               
//                updateComponents();
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
     * A get method for the to get the path to the SD-file, if non exist it 
     * returns a empty {@link String}.
     * 
     * @return The path the the SD-file.
     */
//	protected String getSDFilePath() {
//	    return sdFilePath;
//	}
	
//    protected InputStream getSDFileContents() {
//        try {
//            return new FileInputStream(new File(sdFilePath));
//        } catch (FileNotFoundException e) {
//            return null;
//        }
//    }
	
    /**
     * A get method to get the excluded properties, if non of the properties
     * are excluded it returns an empty {@link ArrayList}.
     * 
     * @return An <code>ArrayList</code> with the excluded properties
     */
	protected ArrayList<String> getExludedProerties() {
	    return excludedProperties;
	}
	
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
	
	protected void init(ISelection selection) {
		if (!(selection instanceof IStructuredSelection) || selection.isEmpty())
			return;

		Iterator itr = ((IStructuredSelection) selection).iterator();
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
//					try {
//                       fileHandler.setDataFile( file );
                       updatePropertiesData(file.getFullPath().toOSString());
//                    } catch ( FileNotFoundException e ) {
////                        updateComponents();
//                        // TODO Add a log entry
//                        e.printStackTrace();
//                    }
				} 
			}
		}
	}
	
	
	private void updatePropertiesData(String pathStr) {
	    try {
            Path path = new Path(pathStr);
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            fileHandler.setDataFile( file );
        } catch ( FileNotFoundException e1 ) {
//            txtFilePath = "";
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
	    updateComponents();
	}
		
	private Composite getDataComposite(Composite parent) {
	    Boolean noPropertiesData;
	    int rows;
	    if (propertiesData.size() == 0){
	        noPropertiesData = true;
	        columns = 1;
	        rows = 3;
	    } else {
	        noPropertiesData = false;
	        columns = propertiesData.size();
	        rows = propertiesData.get( 0 ).size();
	    }
	    String textInDataField;
	    isExcluded = new boolean[columns];
	    
	    headerText = new Text[columns];
	    dataText = new Text[columns];
	    excludeButtons = new Button[columns];
	    Composite view = new Composite(parent, SWT.BORDER);
	    view.setLayout( new GridLayout(columns + 1, false) );
	    
	    headerLabel = new Label(view, SWT.NONE);
	    headerLabel.setText( "Name" );
	    for (int i = 0; i < columns; i++) {
	        headerText[i] = new Text(view, SWT.READ_ONLY | SWT.BORDER);
	        if (noPropertiesData)
	            headerText[i].setText( "No data loaded" );
	        else
	            headerText[i].setText( propertiesData.get( i ).get( 0 ) );
	    }
	    
	    dataLable = new Label(view, SWT.NONE);
	    if (rows > 5) {
	        dataLable.setText( "First five values" );
	        rows = 5;
	    } else {
	        dataLable.setText( "Values" );
	    }
	    for (int i = 0; i < columns; i++) {
	        dataText[i] = new Text(view, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI);
	        textInDataField =  "";
	        for (int j = 1; j < rows; j++) {
	            if (noPropertiesData)
	                textInDataField += "n/a\n"; 
	            else
	                textInDataField += propertiesData.get( i ).get( j ) + "\n"; 
	        }
	        dataText[i].setText( textInDataField );
	    }
	    
	    excludeLable = new Label(view, SWT.NONE);
	    excludeLable.setText( "Exclude" );
	    for (int i = 0; i < columns; i++) {
	        excludeButtons[i] = new Button(view, SWT.CHECK | SWT.CENTER);
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
	    
//	    view.pack();
//	    updateComponents();
	    
	    return view;
	}
	
	private void updateDataCompocite() {
	    for (int i = 0; i < columns; i++) {
            if (propertiesData.isEmpty())
                headerText[i].setText( "No data loaded" );
            else
                headerText[i].setText( headers.get( i ) );
        }
	}
	
	private Composite settingsComposite(Composite parent) {
	    Composite view = new Composite(parent, SWT.NONE);
	    view.setLayout( new GridLayout(1, true) );
	    decideOrder = new Button[2];
	    
	    decideOrder[0] = new Button(view, SWT.RADIO);
	    decideOrder[0].setText("The order is the data file as in the SDF file");
	    decideOrder[0].setSelection( true );
	    
	    Composite rbComposite = new Composite(view, SWT.NONE);
	    rbComposite.setLayout( new GridLayout(4, false) );
	    
	    decideOrder[1] = new Button(rbComposite, SWT.RADIO);
        decideOrder[1].setText("Link data by column ");
        decideOrder[1].setSelection( false );
	    txtCombo = new Combo(rbComposite, SWT.DROP_DOWN | SWT.BORDER);
	    for (int i = 0; i < propertiesData.size(); i++)
	        txtCombo.add( propertiesData.get( i ).get( 0 ) );
	    new Label(rbComposite, SWT.NONE).setText( " to SDF-property " );
	    sdfCombo = new Combo(rbComposite, SWT.DROP_DOWN | SWT.BORDER);
	    sdfPropertyList = fileHandler.getPropertiesFromSDFile();
	    if (sdfPropertyList != null && sdfPropertyList.size() > 0) {
	        for (int i = 0; i < sdfPropertyList.size(); i++)
	            sdfCombo.add( sdfPropertyList.get( i ) );
	    }
	    
        return view;
	}
	
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
//	    fromFileTxt.setSize( 300, 15 );
	    
	    if (fileHandler.sdFileExists())
	        toFileTxt.setText( fileHandler.getSDFilePath() );
	    else {
	        toFileTxt.setText( "" );//"Add the SD-file here" ); 
	    }
	    
//	    updatePropertiesData();
	    if (fileHandler.dataFileExists()) {
	        txtCombo.removeAll();
	        for (int i = 0; i < headers.size(); i++)
	            if (!isExcluded[i])
	                txtCombo.add( headers.get( i ) );
	    }
	    sdfCombo.removeAll();
	    for (int i = 0; i < propertiesData.size(); i++)
	        sdfCombo.add( propertiesData.get( i ).get( 0 ) );

	    mainComposite.redraw();
	    mainComposite.update();
	}

}
