/* *****************************************************************************
 * Copyright (c) 2007-2012 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *    
 ******************************************************************************/
package net.bioclipse.cdk.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class handle the different things that concerns reading from / writing
 *  to the data-file (the file that contains the properties to be added to the 
 *  sd-file, e.g. a txt- or csv-file) and the sd-file.
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
    // The row separator for the data in the data file, i.e. a tab.
    private final static String DELIMITER = "\t";
    // The number of rows read into topValues at initiation 
    private final static int ROWS_IN_TOPVALUES = 5;
    private boolean hasFoundLastRowInFile = false;
    private DataFileFormart dataFileFormart;
    
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
        dataFileFormart = DataFileFormart.NO_FILE_SELECTED;
    }
    
    /**
     * An enumeration of the supported formats of the data file.
     * TXT is a simple txt-file with the values separated by a tab
     * CSV is a file with standard csv-file, i.e. with comma separated values
     * NO_FILE_SELECTED is used if there's no data file selected
     * 
     * @author klasjonsson
     *
     */
    private enum DataFileFormart {
        TXT,
        CSV,
        NO_FILE_SELECTED
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
        IteratingMDLReader sdfItr = 
                new IteratingMDLReader(getSDFileContents(),
                                        SilentChemObjectBuilder.getInstance());
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
     * @param dataFile The IFile containing the data-file
     * @throws FileNotFoundException Thrown if there's no file found
     */
    public void setDataFile(IFile dataFile) throws FileNotFoundException {
        this.dataFile = dataFile;
        hasFoundLastRowInFile = false;
        if (dataFile.getFileExtension().toLowerCase().matches( "csv" )) {
            dataFileFormart = DataFileFormart.CSV;
            readFromCSV( 0, ROWS_IN_TOPVALUES );
        } else {
            dataFileFormart = DataFileFormart.TXT;
            readFromTXT( 0, ROWS_IN_TOPVALUES );
        }
    }
    
    /**
     * To check if the data-file with data has been loaded.
     * 
     * @return True if the file has been loaded.
     */
    public boolean dataFileExists() {
        return ( dataFileFormart != DataFileFormart.NO_FILE_SELECTED );
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
    
    /**
     * Returns the path to the data file as a String.
     * 
     * @return The path to the data file
     */
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
        if (hasFoundLastRowInFile)
            return topValues;
        
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
            switch (dataFileFormart) {
                case TXT:    
                    readFromTXT( rows, numberOfRows );
                    break;
                    
                case CSV:
                    readFromCSV( rows, numberOfRows  );
                    break;
                    
                case NO_FILE_SELECTED:
                    topValues.clear();
                    break;
                    
                default:
                    throw new FileNotFoundException( "Support of enlargement" +
                    		" of the loaded values for data when using " +
                    		dataFileFormart + " needs to be implemented." );
            }
            
            return topValues;
        }
    }
    
    /**
     * This method reads the txt-file. It assumes that the top row contains
     * the names of the properties, If not: After loading the file use the 
     * method "propertiesNameInDataFile(boolean)".
     *  
     * @param startRow The row to start read from
     * @param endRow The last row to read
     * @throws FileNotFoundException Thrown if the file can't be found
     */
    private void readFromTXT(int startRow, int endRow) 
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
            lineScanner = new Scanner( line );
            lineScanner.useDelimiter( DELIMITER ); // Data is separated by a tab
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
        
        if (row<endRow)
            hasFoundLastRowInFile = true;
    }
    
    /**
     * To be used if the data is in a csv-file
     * @param startRow The row to start reading from, 0 is considered as the 
     * first row in a file.
     * @param endRow The last row to be read, set to -1 to read all rows in the 
     * file.
     * @throws FileNotFoundException 
     */
    private void readFromCSV(int startRow, int endRow) 
            throws FileNotFoundException { 
        String filePath = dataFile.getFullPath().toString();
        CSVReader csvReader = new  CSVReader( new FileReader( filePath ) );
        String[] nextRow;
        propertiesID.clear();
        topValues.clear();
        
        int rowNumber = 0;
        
        if (startRow != 0)
            for (int i = 0; i < startRow; i++)
                try {
                    csvReader.readNext();
                } catch ( IOException e ) {
                    logger.error( e.getMessage() );
                    return;
                }
        
        if (endRow == -1) {
            while (true) {
                try {
                    nextRow = csvReader.readNext();
                    for (int i = 0; i < nextRow.length; i++) {
                        if (topRowContainsPropName) {
                            if (rowNumber == 0)
                               propertiesID.add( nextRow[i] );
                            else {
                                if (rowNumber == 1)
                                    topValues.add( new ArrayList<String>() );
                                topValues.get( i ).add( nextRow[i] );
                            }
                        } else {
                            if (rowNumber == 0)
                                topValues.add( new ArrayList<String>() );
                            topValues.get( i ).add( nextRow[i] );
                        }
                    }
                    rowNumber++;
                } catch ( IOException e ) {
                    /* It seams that this is the only way to know that we 
                     * reached the end of the file */
                    break;
                }
                
            }
        } else {
            for (int j=0;j<endRow;j++) {
                try {
                    nextRow = csvReader.readNext();
                    if (nextRow != null) {
                        for (int i = 0; i < nextRow.length; i++) {
                            if (topRowContainsPropName) {
                                if (j == 0)
                                    propertiesID.add( nextRow[i] );
                                else {
                                    if (j == 1)
                                        topValues.add( new ArrayList<String>() );
                                    topValues.get( i ).add( nextRow[i] );
                                }
                            } else {
                                if (j == 0)
                                    topValues.add( new ArrayList<String>() );
                                topValues.get( i ).add( nextRow[i] ); 
                            }
                        }
                    } else {
                        hasFoundLastRowInFile = true;
                        break;
                    }
                } catch ( IOException e ) {
                    /* It seams that this is the only way to know that we 
                     * reached the end of the file */
                    break;
                }  
            }
        }
        
        try {
            csvReader.close();
        } catch ( IOException e ) {
            logger.error( e.getMessage() );
        }
    }
    
    /** 
     * This method adds the properties from the data-file to the molecules from
     * the sd-file and save them in a new file. The name of the new file is on
     * the form:<br> 
     * [name of sd-file]_new.sdf. 
     * 
     * @param propertiesName An ArrayList with the name of the properties as 
     *          Strings
     * @param propNameInDataFile Set to <code>true</code> if the data-file with 
     *          properties contains the names of the properties
     * @throws FileNotFoundException
     */
    public void meargeFiles(ArrayList<String> propertiesName, 
                            boolean propNameInDataFile) 
                                    throws FileNotFoundException {
        /* In this case we don't want to remove any properties, so lets send in 
         * an empty ArrayList */
        meargeFiles( new boolean[0], propertiesName,
                     propNameInDataFile, null );
    }
    
    /** 
     * This method adds the properties from the data-file to the molecules from
     * the sd-file and save them in a new file. The name of the new file is on
     * the form:<br> 
     * [name of sd-file]_new.sdf. 
     * 
     * @param excludedProerties An ArrayList with the name of the properties 
     *          that has been excluded
     * @param propertiesName An ArrayList with the name of the properties as 
     *          Strings
     * @param propNameInDataFile Set to <code>true</code> if the data-file with 
     *          properties contains the names of the properties
     * @throws FileNotFoundException
     */
    public void meargeFiles(boolean[] excludedProerties, 
                            ArrayList<String> propertiesName,
                            boolean propNameInDataFile) 
                                    throws FileNotFoundException {
        
        meargeFiles( excludedProerties, propertiesName, propNameInDataFile, 
                     null );
    }
    
    /**
     This method adds the properties from the data-file to the molecules from
     * the sd-file and save them in a new file. The name of the new file is on
     * the form:<br> 
     * [name of sd-file]_new.sdf. 
     * 
     * @param excludedProerties An ArrayList with the name of the properties 
     *          that has been excluded
     * @param propertiesName An ArrayList with the name of the properties as 
     *          Strings
     * @param propNameInDataFile Set to <code>true</code> if the data-file with 
     *          properties contains the names of the properties
     * @param monitor The progress monitor for this class
     * @throws FileNotFoundException
     */
    public void meargeFiles(boolean[] includedProerties, 
                            ArrayList<String> propertiesName,
                            boolean propNameInDataFile, 
                            IProgressMonitor monitor) 
                                    throws FileNotFoundException {
        
        if ( !sdFileExists() || !dataFileExists() )
                throw 
                new FileNotFoundException("Can't find one or both files...");
        
        /* TODO This goes thru the hole sd-file and counts the mols, 
         * is it some better way to do this? */
        int molsInSdf = 0;
        IteratingMDLReader sdfMolCounter = 
                new IteratingMDLReader(getSDFileContents(), 
                                        SilentChemObjectBuilder.getInstance());
        while (sdfMolCounter.hasNext()) {
            molsInSdf++;
            sdfMolCounter.next();
        }
        monitor.beginTask( "Mearging", molsInSdf );
        int work = 0;
        
        IteratingMDLReader sdfItr = 
                new IteratingMDLReader(getSDFileContents(), 
                                       SilentChemObjectBuilder.getInstance());
        Scanner fileScanner;
        ArrayList<String> values, names;
        names = propertiesName;
        int index;
        
        /* We can't write to a file we are reading from, so we create an new 
         * sd-file where we save the data. If the path to where to save the new 
         * file isn't set we save it where the other sd-file is */
        if ( newSDFilePath == null || newSDFilePath.isEmpty() )
            setPathToNewSDFile( sdFile.getLocation().toOSString() );       
        String newFile = sdFile.getName();
        index = newFile.indexOf( "\u002E" ); // the Unicode for for '.'
        newFile = newSDFilePath + newFile.substring( 0, index ) +
                "\u005Fnew\u002E" + sdFile.getFileExtension();
        
        FileOutputStream out = new FileOutputStream(newFile);
        SDFWriter writer = new SDFWriter( out );
        IChemObject mol;
        
        if (propLinkedBy) {
            /* This option only add the properties to the molecules where 
             * the linked properties has the same value.*/
            index = names.indexOf( dataFileLink );
            names.set( index, sdFileLink );
            while ( sdfItr.hasNext() ) {
                mol = sdfItr.next();
                String molProp = mol.getProperty( sdFileLink ).toString();
                /* If this molecule don't have a property with this name
                 * there's no need to do any more with this molecule*/
                if ( molProp != null && !molProp.isEmpty() ) {
                    switch (dataFileFormart) {
                        case CSV:
                            String filePath = dataFile.getFullPath().toString();
                            CSVReader csvReader = 
                                    new CSVReader( new FileReader( filePath ) );
                            try {
                                String[] nextRow = csvReader.readNext();
                                values = new ArrayList<String>();
                                for (String value:nextRow)
                                    values.add( value );
                                mol = sdfItr.next();
                                if ( values.get( index ).equals( molProp ) ) {
                                    addPropToMol( mol, names, values, 
                                                  includedProerties );
                                    break;
                                }
                            } catch ( IOException e ) {
                                /* It seams that this is the only way to know 
                                 * that we reached the end of the file */
                                try {
                                    csvReader.close();
                                } catch ( IOException e1 ) {
                                    logger.error( e1.getMessage() );
                                }
                                break;
                            }
                            break;

                        case TXT:
                            fileScanner = new Scanner( getDataFileContents() );
                            while ( fileScanner.hasNext() ) {
                                values = readNextLine( fileScanner.nextLine() );
                                if ( values.get( index ).equals( molProp ) ) {
                                    addPropToMol(mol, names, values, 
                                                 includedProerties);
                                    break;
                                }
                            }
                            fileScanner.close();
                            break;
                        case NO_FILE_SELECTED:
                            /* It shouldn't end up here, 'cos the file has to be 
                             * selected before it starts to merge */
                            throw new FileNotFoundException("Can't find the " +
                            		"data-file");

                        default:
                            logger.debug( "Support for "+dataFileFormart+" " +
                            		"needs to be implemented when the " +
                            		"properties are linked by some value." );
                            break;
                    }
                }
                try {
                    writer.write( mol );
                } catch ( CDKException e ) {
                    logger.error( e.getMessage() );
                }

                monitor.worked( work++ );
            }
        } else {
            switch (dataFileFormart) {
                case CSV:
                String filePath = dataFile.getFullPath().toString();
                CSVReader csvReader = new CSVReader( new FileReader(filePath) );
                while (sdfItr.hasNext()) {
                    try {
                        String[] nextRow = csvReader.readNext();
                        values = new ArrayList<String>();
                        for (String value:nextRow)
                            values.add( value );
                        mol = sdfItr.next();
                        addPropToMol( mol, names, values, includedProerties );
                        writer.write( mol );
                    } catch ( IOException e ) {
                        /* It seams that this is the only way to know that we 
                         * reached the end of the file */
                        try {
                            csvReader.close();
                        } catch ( IOException e1 ) {
                            logger.error( e1.getMessage() );
                        }
                        break;
                    } catch ( CDKException e ) {
                        logger.error( e.getMessage() );
                    }
                    monitor.worked( work++ );
                }
                case TXT:
                    fileScanner = new Scanner( getDataFileContents() );
                    /* We uses the names provided from the wizard, so if the 
                     * file contains names we just throw them away... */
                    if ( propNameInDataFile && fileScanner.hasNextLine() )
                        fileScanner.nextLine();
                    while ( sdfItr.hasNext() && fileScanner.hasNextLine() ) {
                        mol = sdfItr.next();
                        values = readNextLine( fileScanner.nextLine() );
                        addPropToMol( mol, names, values, includedProerties );
                        try {
                            writer.write( mol );
                        } catch ( CDKException e ) {
                            logger.error( e.getMessage() );
                        }
                        monitor.worked( work++ );
                    }
                    break;
                    
                case NO_FILE_SELECTED:
                    /* It shouldn't end up here, 'cos the file has to be 
                     * selected before it starts to merge */
                    throw new FileNotFoundException("Can't find the data-file");
                    
                default:
                    logger.debug( "Support for "+dataFileFormart+" " +
                            "needs to be implemented for unlinked properties");
                    break;
            }
        }
        try {
            writer.close();
            out.close();
        } catch ( IOException e ) {
            logger.error( e );
        }
        monitor.done();
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
                               boolean[] includedProp) {
         Iterator<String> namesItr = propNames.iterator();
         Iterator<String> valueItr = propValues.iterator();         
         String name = "";
         int index = 0;
         
         while ( namesItr.hasNext() ) {
             name = namesItr.next();
             if (!(includedProp.length == 0) && includedProp[index] )
                 mol.setProperty( name, valueItr.next() );
             else
                 valueItr.next();
             index++;
         }   
        
    }

    /**
     * This method sets the path to where the new sd-file will be saved. If it 
     * contains a file name that file name is removed.
     * 
     * @param path The path to where the new sd-file will be saved
     */
    public void setPathToNewSDFile(String path) {
        String separator = System.getProperty( "file.separator" );
        int index = path.lastIndexOf( separator );
        newSDFilePath = path.substring( 0, index + 1 );
    }
    
    private ArrayList<String> readNextLine(String nextLine) {
        ArrayList<String> properties = new ArrayList<String>(); 
        Scanner lineScanner = new Scanner( nextLine );
        lineScanner.useDelimiter( DELIMITER ); // Data is separated by a tab
        while ( lineScanner.hasNext() ) {
            properties.add( lineScanner.next() );
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
    
    /**
     * This method is used to link between a properties in the data file and one
     * in the sd-file. Or to unlink them.
     * 
     * @param linkedBy <code>True</code> if there's a link
     * @param dataFileProp The name of the property in the data file
     * @param sdFileProp The name of the property in the sd-file
     */
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
    
    /**
     * This method is to be able to only chance the name of the linked property
     * in the data file.
     * 
     * @param dataFileProp The name of the property in the data file
     */
    public void setdataFileLink(String dataFileProp) {
        dataFileLink = dataFileProp;
    }
    
    /**
     * This metod is to be able to only chance the name of the linked property
     * in the Sd-file.
     * 
     * @param dataFileProp The name of the property in the sd-file
     */
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