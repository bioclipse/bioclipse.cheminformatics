package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.FileHandler;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;

import org.eclipse.core.resources.IFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

public class PropertiesImportFileHandler {

    private IFile sdFile;
    private IFile dataFile;
    private ArrayList<String> propertiesID;
    private ArrayList<String> sdFilePropertiesID;
    private ArrayList<ArrayList<String>> topValues;
    private boolean topRowContainsPropName;
    // The number of rows read in to topValues at initiation 
    private final static int ROWS_IN_TOPVALUES = 5;
    
    /**
     * A constructor to use if non, or only one, of the files are known. 
     */
    public PropertiesImportFileHandler() { 
        propertiesID = new ArrayList<String>();
        sdFilePropertiesID = new ArrayList<String>();
        topValues = new ArrayList<ArrayList<String>>();
        topRowContainsPropName = true;
    }
    
    /**
     * A constructor to use if both the files are known.
     * 
     * @param sdFile The IFile containing the sd-file
     * @param dataFile The IFile containing the data file
     * @throws FileNotFoundException Thrown if some, or both, of the files are 
     *      not found
     */
    public PropertiesImportFileHandler(IFile sdFile, IFile dataFile) throws FileNotFoundException {
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
        /* FIXME I would love to use MoleculesFromSDF, but there's three obstacles:
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
        IteratingMDLReader sdfItr = new IteratingMDLReader( getSDFileContents(), DefaultChemObjectBuilder.getInstance() );
        Map<Object, Object> propertiesMap = sdfItr.next().getProperties();
        Set<Object> propSet = propertiesMap.keySet();
        Iterator<Object> propSetItr = propSet.iterator();
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
            return null;
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
                if (topRowContainsPropName) {
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
                } else {
                    /* If we are reading the first row with data then we create a new ArrayList
                     *  for each column*/
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
            int elements = propertiesID.size();
            for (int i = 0; i < elements; i++) {
                // Zero 'cos we remove the element... 
                topValues.get( i ).add( 0, propertiesID.remove( 0 ) );
            }
        }
    }
}
