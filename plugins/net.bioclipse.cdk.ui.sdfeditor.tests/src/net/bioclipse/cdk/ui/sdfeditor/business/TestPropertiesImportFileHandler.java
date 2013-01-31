/* *****************************************************************************
 * Copyright (c) 2007-2012 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *    
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.bioclipse.cdk.ui.wizards.PropertiesImportFileHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This is the test-class for the PropertiesImportFileHandler-class.
 * 
 * @author Klas Jšnsson (klas.joensson@gmail.com)
 *
 */
public class TestPropertiesImportFileHandler {

    private PropertiesImportFileHandler fileHandler;

    @Before
    public void setUp() {
        fileHandler = new PropertiesImportFileHandler();
//        testPage = new SDFPropertiesImportWizardPage( "Test page" );
    }
    
    @Test
    public void testSetDataFile() throws FileNotFoundException {
        
        Path path = new Path("./datafile2.txt");
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        fileHandler.setDataFile( file );
  
        Assert.assertTrue( fileHandler.dataFileExists() );
    }
    
    
    @Test
    public void testReadDataFile() throws FileNotFoundException {
        
        Path path = new Path("./datafile2.txt");
        ArrayList<ArrayList<String>> data;
        ArrayList<String> id;
        
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        fileHandler.setDataFile( file );
        
        id = fileHandler.getPropertiesIDFromDataFile();
        for (int i = 0; i < id.size(); i++)
            System.out.print( id.get( i ) + "\t" );
        System.out.println();
        
        data = fileHandler.getTopValuesFromDataFile( 5 );
        for (int i = 0; i < data.size(); i++){
            for (int j = 0; j < data.get( i ).size(); j++)
                System.out.print( data.get( i ).get( j ) + "\t" );
            System.out.println();
        }
    }
}
