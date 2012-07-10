/* *****************************************************************************
 * Copyright (c) 2006, 2008-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

/**
 * This class handle the different things that concerns reading and writing to 
 * the txt- and the sd-file.
 * 
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class PropertiesImportFileHandler {

    private Logger logger = Logger.getLogger( this.getClass() );
    private IFile sdFile;
    private IFile dataFile;
    private ArrayList<String> propertiesID, choosenPropID;
    private ArrayList<String> sdFilePropertiesID;
    private ArrayList<ArrayList<String>> topValues;
    private boolean topRowContainsPropName, propLinkedBy;
    private String dataFileLink, sdFileLink, newSDFilePath;
    // The number of rows read in to topValues at initiation 
    private final static int ROWS_IN_TOPVALUES = 5;
    
    /**
     * A constructor to use if non, or only one, of the files are known. 
     */
    public PropertiesImportFileHandler() { 
        propertiesID = new ArrayList<String>();
        choosenPropID = new ArrayList<String>();
        sdFilePropertiesID = new ArrayList<String>();
        topValues = new ArrayList<ArrayList<String>>();
        topRowContainsPropName = true;
        sdFileLink = "";
        dataFileLink = "";
        propLinkedBy = false;
    }
    
    /**
     * A constructor to use if both the files are known.
     * 
     * @param sdFile The IFile containing the sd-file
     * @param dataFile The IFile containing the data file
     * @throws FileNotFoundException Thrown if some, or both, of the files are 
     *      not found
     */
    public PropertiesImportFileHandler(IFile sdFile, IFile dataFile) 
            throws FileNotFoundException {
        this();
        setSDFile( sdFile );
        setDataFile( dataFile );
    }
    
    /**
     * This method load the sd-file.
     * 
     * @param dataFile The IFile containing the sd-file
     * @throws FileNotFoundException Thrown if there's no file found
     */
    public void setSDFile(IFile sdFile) throws FileNotFoundException {
        this.sdFile = sdFile;
        sdFilePropertiesID.clear();
        extractSDFProerties();
    }
    
    /**
     * This method read the data in the sd-file.
     * 
     * @throws FileNotFoundException If the sd-file isn't found
     */
    private void extractSDFProerties() throws FileNotFoundException {
        /* Here you might use the MoleculesFromSDF, but there's three obstacles:
         * - The method for adding properties isn't implemented get
         * - The method for saving SDFiles isn't implemented get
         * - The method for getting properties just returns an empty collection,
         *      I tried to implement the function, but I didn't succeeded... */
//        if (!sdFile.exists() || sdFile == null)
//            throw new FileNotFoundException ("Can't find the sd-file.");
//        MoleculesFromSDF molFrSDF = new MoleculesFromSDF(sdFile);
//        sdFilePropertiesID = new ArrayList<String>();
//        Collection<Object> properties = molFrSDF.getAvailableProperties();       
//        Iterator<Object> propItr = properties.iterator();
//        while ( propItr.hasNext() ) 
//            sdFilePropertiesID.add( propItr.next().toString() );
        
        /* This use the IteratingMDLReader in CDK and works*/       
        IteratingMDLReader sdfItr = 
                new IteratingMDLReader(getSDFileContents(),
                                        DefaultChemObjectBuilder.getInstance());
        Map<Object, Object> propertiesMap = sdfItr.next().getProperties();
        Set<Object> propSet = propertiesMap.keySet();
        Iterator<Object> propSetItr = propSet.iterator();
        sdFilePropertiesID.clear();
        while (propSetItr.hasNext())
            sdFilePropertiesID.add( propSetItr.next().toString() );
    }
    
    /**
     * This method opens a stream to the sd-file.
     * 
     * @return A stream to the sd-file
     */
    public InputStream getSDFileContents() {
        String path = sdFile.getFullPath().toOSString(); 
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            path = sdFile.getLocation().toOSString();
            try {
                return new FileInputStream(new File(path));
            } catch ( FileNotFoundException e1 ) {
                return null;
            }
        }
    }
    
    /**
     * A method to get the path to the sd-file. If the sd-file isn't loaded it
     * returns an empty string.
     * 
     * @return The path to the sd-file
     */
    public String getSDFilePath() {
        if (sdFileExists())
            return sdFile.getProjectRelativePath().toOSString();
        else
            return "";
    }
    
    /**
     * To check if the sd-file has been loaded.
     * 
     * @return True if the file has been loaded.
     */
    public boolean sdFileExists() {
        return (sdFile != null);
    }
    
    /**
     * Returns the properties of the first molecule in the sd-file.
     * 
     * @return The properties in the sd-file
     */
    public ArrayList<String> getPropertiesFromSDFile() {
        return sdFilePropertiesID;
    }
    
    /**
     * This method load the data file.
     * 
     * @param dataFile The IFile containing the txt-file
     * @throws FileNotFoundException Thrown if there's no file found
     */
    public void setDataFile(IFile dataFile) throws FileNotFoundException {
        this.dataFile = dataFile;
        propertiesID.clear();
        topValues.clear();
        readProperiesFile( 0, ROWS_IN_TOPVALUES );
    }
    
    /**
     * To check if the txt-file with data has been loaded.
     * 
     * @return True if the file has been loaded.
     */
    public boolean dataFileExists() {
        return ( dataFile != null );
    }
    
    public InputStream getDataFileContents() {
        String path = dataFile.getFullPath().toOSString(); 
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            path = dataFile.getLocation().toOSString();
            try {
                return new FileInputStream(new File(path));
            } catch ( FileNotFoundException e1 ) {
                return null;
            }
        }
    }
    
    public String getDataFilePath() {
        return dataFile.getProjectRelativePath().toOSString();
    }
    
    /**
     * This method returns the properties ID:s of the data file. I.e. the 
     * top row.
     * @return The top row of the data file.
     */
    public ArrayList<String> getPropertiesIDFromDataFile() {
        return propertiesID;
    }
    
    /**
     * This method returns the first number of rows from the data file.<br>
     * This method returns matrix made of two {@link ArrayList}s, where the 
     * outer <code>ArrayList</code> corresponds to the columns of the data
     * file and the inner to the rows, i.e. to get the i:th element of the j:te 
     * column write:<br> <code>[Name of the matrix].get(j).get(i)</code>; <br>
     * The top row of the data file are considered to be the ID:s of the 
     * properties and therefore NOT returned.
     *  
     * @param numberOfRows
     * @return A matrix with the properties in
     * @throws FileNotFoundException 
     */
    public ArrayList<ArrayList<String>> 
     getTopValuesFromDataFile(int numberOfRows) throws FileNotFoundException {
        if (topValues.isEmpty() || topValues.get( 0 ).isEmpty())
            return new ArrayList<ArrayList<String>>();
        int rows;
        if (topRowContainsPropName)
            rows = topValues.get( 0 ).size() + 1;
        else
            rows = topValues.get( 0 ).size();
        if (numberOfRows == rows)
            return topValues;
        ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
        ArrayList<String> temp2;
        if (numberOfRows < rows) {
            for (int j = 0; j < topValues.size(); j++) {
                temp2 = new ArrayList<String>();                
                for (int i = 0; i < topValues.get( j ).size(); i++) {
                    temp2.add( topValues.get( j ).get( i ) );
                }
                temp.add( temp2 );
            }
            return temp;
        } else {
            readProperiesFile( rows, numberOfRows );
            return topValues;
        }
    }
    
    /**
     * This method reads the data file. It assumes that the top row contatins
     * the names of the properties, If not: After loading the file use the 
     * method "propertiesNameInDataFile(boolean)".
     *  
     * @param startRow The row to start read from
     * @param endRow The last row to read
     * @throws FileNotFoundException Thrown if the file can't be found
     */
    private void readProperiesFile(int startRow, int endRow) 
            throws FileNotFoundException {   
        /*
         * Read the first element and add that to an (local) array list and add 
         * that to as the first array list in propertiesData, take the next add 
         * that as the top element in a new (local) array list and add that 
         * array list as the second element of propertiesData etc...
         * 
         * then read the next line and add the first element of that as the 
         * Second element in the first array list of propertiesData, i.e.
         * propertiesData.get(0).add(data);
         * 
         * It might use to much memory if the hole file is read, maybe it should 
         * only read the first e.g. five elements of the file...?  
         */
        if (endRow <= startRow)
            return;
        if (endRow < ROWS_IN_TOPVALUES)
            endRow = ROWS_IN_TOPVALUES;
        propertiesID.clear();
        topValues.clear();
        
        Scanner fileScanner = new Scanner(getDataFileContents());
        Scanner lineScanner;
        ArrayList<String> columns;
        int column, row = 0;
        String line, element;
        while (fileScanner.hasNextLine() && row <= endRow) {
            while (row < startRow) {
                fileScanner.next();
                row++;
            }

            column = 0;
            line = fileScanner.nextLine();
            lineScanner = new Scanner(line);
            lineScanner.useDelimiter("\t"); // Separated by a tab...
            while (lineScanner.hasNext()) {
                element = lineScanner.next();
                if (topRowContainsPropName) {
                    if (row == 0) {
                        propertiesID.add( element );
                    } else {   
                        /* If we are reading the first row with data then we 
                         * create a new ArrayList for each column*/
                        if (row == 1) {
                            columns = new ArrayList<String>();
                            topValues.add( columns );
                        }
                        topValues.get( column ).add( element );
                    }
                } else {
                    /* If we are reading the first row with data then we create
                     *  a new ArrayList for each column*/
                    if (row == 0) {
                        columns = new ArrayList<String>();
                        topValues.add( columns );
                    }
                    topValues.get( column ).add( element );
                }
                column++;
            }
            row++;
        }
    }
    
    /** 
     * This method adds the properties from the txt-file to the molecules from
     * the sd-file and save them in a new file. The name of the new file is on
     * the form:<br> 
     * [name of sd-file]_new.sdf. 
     * 
     * @param propertiesName An ArrayList with the name of the properties as 
     *          Strings
     * @param propNameInDataFile Set to <code>true</code> if the txt-file with 
     *          properties contains the names of the properties
     * @throws FileNotFoundException
     */
    public void meargeFiles(ArrayList<String> propertiesName, 
                            boolean propNameInDataFile) 
                                    throws FileNotFoundException {
        /* In this case we don't want to remove any properties, so lets send in 
         * an empty ArrayList */
        meargeFiles( new ArrayList<String>(), propertiesName,
                     propNameInDataFile );
    }
    
    /** 
     * This method adds the properties from the txt-file to the molecules from
     * the sd-file and save them in a new file. The name of the new file is on
     * the form:<br> 
     * [name of sd-file]_new.sdf. 
     * 
     * @param exludedProerties An ArrayList with the name of the properties that
     *          has been excluded
     * @param propertiesName An ArrayList with the name of the properties as 
     *          Strings
     * @param propNameInDataFile Set to <code>true</code> if the txt-file with 
     *          properties contains the names of the properties
     * @throws FileNotFoundException
     */
    public void meargeFiles(ArrayList<String> exludedProerties, 
                            ArrayList<String> propertiesName,
                            boolean propNameInDataFile) 
                                    throws FileNotFoundException {
        // TODO The method IFile.exixts() seems to always return false.
//        if ( !sdFile.exists() || !dataFile.exists() )
        if ( !sdFileExists() || !dataFileExists() )
                throw 
                new FileNotFoundException("Can't find one or both files...");
        ArrayList<ArrayList<String>> properties =
                readAllData( exludedProerties, propertiesName,
                             propNameInDataFile );
        IteratingMDLReader sdfItr = 
                new IteratingMDLReader(getSDFileContents(), 
                                        DefaultChemObjectBuilder.getInstance());
        int index = 1, propIndex = 0, index2;
        
        /* We can't write to a file we are reading from, so we create an new 
         * sd-file where we save the data. If the path to where to save the new 
         * file isn't set we save it where the other sd-file is */
        if ( newSDFilePath == null || newSDFilePath.isEmpty() )
            setPathToNewSDFile( sdFile.getLocation().toOSString() );
        
        String newFile = sdFile.getName();
        index2 = newFile.indexOf( "\u002E" ); // the Unicode for for '.'
        newFile = newSDFilePath + newFile.substring( 0, index2 ) +
                "\u005Fnew\u002E" + sdFile.getFileExtension();
        
        FileOutputStream out = new FileOutputStream(newFile);
        SDFWriter writer = new SDFWriter( out );
      
        if (propLinkedBy)
            propIndex = properties.get( 0 ).indexOf( sdFileLink );
        while ( sdfItr.hasNext() ||  index < properties.size() ) {
            IChemObject mol = sdfItr.next();
            if (propLinkedBy) {
                /* This option only add the properties to the molecules where 
                 * the linked properties has the same value.*/ 
                String molProp = mol.getProperty( sdFileLink ).toString();
                if ( molProp != null && !molProp.isEmpty() ) {
                    if ( properties.get( index ).get( propIndex )
                            .equals( molProp ) )
                        addPropToMol( mol, properties.get( 0 ),
                                      properties.get( index ),
                                      exludedProerties ); 
                }
            } else {
                addPropToMol( mol, properties.get( 0 ),
                              properties.get( index ), exludedProerties );
            }
            try {
                writer.write( mol );
            } catch ( CDKException e ) {
                logger.error( e );
            }
            index++;
        }
        try {
            writer.close();
            out.close();
        } catch ( IOException e ) {
            logger.error( e );
        }

    }
    
    /**
     * This method do the work with adding the properties to a molecule.
     * 
     * @param mol The molecule that will get the properties
     * @param propNames An ArrayList with the names of the properties
     * @param propValues An ArrayList with the values of the properties
     * @param exludedProp An ArrayList with the names of the excluded properties
     */
    private void addPropToMol( IChemObject mol, ArrayList<String> propNames,
                               ArrayList<String> propValues, 
                               ArrayList<String> exludedProp) {
         Iterator<String> namesItr = propNames.iterator();
         Iterator<String> valueItr = propValues.iterator();         
         String name = "";

         while ( namesItr.hasNext() ) {
             name = namesItr.next();
             if (!exludedProp.contains( name ) )
                 mol.setProperty( name, valueItr.next() );
             else
                 valueItr.next();
         }   
        
    }

    /**
     * This method sets the path to where the new sd-file will be saved. If it 
     * contains a file name that file name is removed.
     * 
     * @param path The path to where the new sd-file will bew saved
     */
    public void setPathToNewSDFile(String path) {
        String separator = System.getProperty( "file.separator" );
        int index = path.lastIndexOf( separator );
        newSDFilePath = path.substring( 0, index + 1 );
    }
    
    /**
     * This method read ALL properties from the txt-file. And stores them in an 
     * matrix made of ArrayLists. The names of the properties are stored in the 
     * first column of this matrix, and the values in the following.
     * 
     * @param exludedProerties An ArrayList with the names of the excluded 
     *          properties 
     * @param propertiesName An ArrayList with the names of the properties
     * @param propNameInDataFile Set to <code>true</code> if the txt-file with 
     *          properties contains the names of the properties
     * @return An matrix with the data from the txt-file
     */
    // TODO Rewrite so it only reads one line at the time...
    private ArrayList<ArrayList<String>> 
     readAllData(ArrayList<String> exludedProerties, 
                 ArrayList<String> propertiesName, boolean propNameInDataFile) {
        ArrayList<ArrayList<String>> properties = 
                new ArrayList<ArrayList<String>>();
        Scanner fileScanner = new Scanner( getDataFileContents() );
        Scanner lineScanner;
        ArrayList<String> columns;
        
        // TODO Don't read the excluded properties
        while ( fileScanner.hasNextLine() ) {
            columns = new ArrayList<String>();
            lineScanner = new Scanner( fileScanner.nextLine() );
            lineScanner.useDelimiter( "\t" ); // Separated by a tab...
            while ( lineScanner.hasNext() ) {
                columns.add( lineScanner.next() );
            }
            properties.add( columns );
        }
        
        if (propLinkedBy) {
            int index = properties.get( 0 ).indexOf( dataFileLink );
            properties.get( 0 ).set( index, sdFileLink );
        }
        
        return properties;
    }
    
    /**
     * Use this method to tell the file-handler if the top row of the data file 
     * contains the names of the properties.
     * 
     * @param exists True if the top row contains the properties names
     */
    public void propertiesNameInDataFile(boolean exists) {
        topRowContainsPropName = exists;
        updatePropertiesLists();
    }
    
    /**
     * Use this to check whether the top row of the data file is said to 
     * contain the name of the properties.
     * 
     * @return True if the top row is the name of the properties
     */
    public boolean isPropertiesNameInDataFile() {
        return topRowContainsPropName;
    }

    /**
     * This method add the top row of topValues to propertiesID (if 
     * topRowContainsPropName is true) or add the propertiesID to the top row of
     * the topValues (if topRowContainsPropName is false).
     */
    private void updatePropertiesLists() {
        if(topRowContainsPropName) {
            // Add the top row of topValues to propertiesID
            propertiesID.clear();
            for (int i = 0; i < topValues.size(); i++) {
                propertiesID.add( topValues.get( i ).remove( 0 ) );
            }
        } else {
            // Add the propertiesID to the top row of the topValues
            int elements = topValues.size();
            for (int i = 0; i < elements; i++) {
                // Zero 'cos we remove the element... 
                topValues.get( i ).add( 0, propertiesID.remove( 0 ) );
                if (choosenPropID.size() == i)
                    choosenPropID.add( i, "" );
            }
        }
    }
    
    public void setLinkProperties(boolean linkedBy, String dataFileProp, 
                                  String sdFileProp) {
        if (linkedBy) {
            propLinkedBy = linkedBy;
            dataFileLink = dataFileProp;
            sdFileLink = sdFileProp;
        } else {
            propLinkedBy = linkedBy;
            dataFileLink = "";
            sdFileLink = "";
        }
    }
    
    public void setdataFileLink(String dataFileProp) {
        dataFileLink = dataFileProp;
    }
    
    public void setsdFileLink(String sdFileProp) {
        sdFileLink = sdFileProp;
    }
    
    /** 
     * A help method to set the names of the properties if they not are in the
     * properties file.
     * 
     * @param index which of the properties to be set, stating from 0
     * @param propName The name of the property
     */
    public void addChoosenPropID(int index, String propName) {
        choosenPropID.add( index, propName );
    }
}