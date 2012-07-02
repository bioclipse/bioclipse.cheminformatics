package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.eclipse.core.resources.IFile;

public class PropertiesImportFileHandler {

    private IFile sdFile;
    private IFile dataFile;
    private ArrayList<String> propertiesID;
    private ArrayList<String> sdFilePropertiesID;
    private ArrayList<ArrayList<String>> topValues;
    // The number of rows read in to topValues at initiation 
    private final static int ROWS_IN_TOPVALUES = 5;
    
    public PropertiesImportFileHandler() { 
        propertiesID = new ArrayList<String>();
        sdFilePropertiesID = new ArrayList<String>();
        topValues = new ArrayList<ArrayList<String>>();
    }
    
    public PropertiesImportFileHandler(IFile sdFile, IFile dataFile) throws FileNotFoundException {
        setSDFile( sdFile );
        setDataFile( dataFile );
    }
    
    public void setSDFile(IFile sdFile) throws FileNotFoundException {
        this.sdFile = sdFile;
        extractSDFProerties();
    }
    
    private void extractSDFProerties() throws FileNotFoundException {
        /* FIXME I would love to use this class, but there's three obstacles:
         * - It's hidden so I can't reach it from here
         * - The method for adding properties isn't implemented get
         * - The method for saving SDFiles isn't implemented get*/
//        MoleculesFromSDF molFrSDF = new MoleculesFromSDF(sdFile);
        /* Or it might be better to use the IteratingMDLReader in CDK...*/
        if (!sdFile.exists() || sdFile == null)
            throw new FileNotFoundException ("Can't find the sd-file.");
//        sdFilePropertiesID = new ArrayList<String>();
        int startPtr, endPtr;
        String nextLine, propName, endMolSequence = "$$$$";      
        Scanner fileScanner = new Scanner(getSDFileContents());
        
        while (fileScanner.hasNextLine()) {
            nextLine = fileScanner.nextLine();
            // Let's just read the properties of the first molecule.
            if (nextLine.startsWith( endMolSequence ))
                break;
            if (nextLine.startsWith( "\u003E" )) {
                // We have fond a line with a property
                //Lets remove the first ">"
                nextLine = nextLine.substring( 1 );
                startPtr = nextLine.indexOf( '\u003C' ) + 1;
                endPtr = nextLine.indexOf( '\u003E' ) - 1;
                propName = nextLine.substring( startPtr, endPtr );
                if (!sdFilePropertiesID.contains( propName ))
                    sdFilePropertiesID.add( propName );
            }
                
        }
    }

    public InputStream getSDFileContents() {
        String path = sdFile.getFullPath().toOSString(); 
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public String getSDFilePath() {
        return sdFile.getProjectRelativePath().toOSString();
    }
    
    public boolean sdFileExists() {
        return (sdFile != null);
    }
    
    public ArrayList<String> getPropertiesFromSDFile() {
        return sdFilePropertiesID;
    }
    
    public void setDataFile(IFile dataFile) throws FileNotFoundException {
        this.dataFile = dataFile;
        readProperiesFile( 0, ROWS_IN_TOPVALUES );
    }
    
    public boolean dataFileExists() {
        return ( dataFile != null );
    }
    
    public InputStream getDataFileContents() {
        String path = dataFile.getFullPath().toOSString(); 
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            return null;
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
    public ArrayList<ArrayList<String>> getTopValuesFromDataFile(int numberOfRows) throws FileNotFoundException {
        if (topValues.isEmpty() || topValues.get( 0 ).isEmpty())
            return new ArrayList<ArrayList<String>>();
        
        int rows = topValues.get( 0 ).size();
        if (numberOfRows == rows)
            return topValues;
        ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
        ArrayList<String> temp2;
        if (numberOfRows < rows) {
            for (int j = 0; j < topValues.size(); j++) {
                temp2 = new ArrayList<String>();
                temp.add( temp2 );
                for (int i = 0; i <= numberOfRows; i++) {
                    temp.get( j ).add( topValues.get( j ).get( i ) );
                }
            }
            return temp;
        } else {
            readProperiesFile( rows, numberOfRows );
            return topValues;
        }
    }
     
    private void readProperiesFile(int startRow, int endRow) throws FileNotFoundException {   
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
                if (row == 0) {
                    propertiesID.add( element );
                } else {   
                    /* If we are reading the first row with data then we create a new ArrayList
                     *  for each column*/
                    if (row == 1) {
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
    
    public void meargeFiles() throws FileNotFoundException {
        /* In this case we don't want to remove any properties, so lets send in 
         * an empty ArrayList */
        meargeFiles( new ArrayList<String>() );
    }
    
    public void meargeFiles(ArrayList<String> exludedProerties) throws FileNotFoundException {
        if (!sdFile.exists() || !dataFile.exists())
                throw new FileNotFoundException ("Can't find one or both files...");
       //TODO Write it... But for this it would be nice if I could use MoleculesFromSDF here...
        throw new UnsupportedOperationException(this.getClass().getName()+
                " does not support this operation yet");
    }
    
}
