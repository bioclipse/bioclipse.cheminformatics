package net.bioclipse.cdk.ui.sdfeditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;

/**
 * A test to verify MolTable reading and parsing of structures and properties
 * 
 * @author ola
 *
 */
public class MolTablePluginTest {

    
    private static final String SDF_FILE = "/testFiles/232mols.sdf";
    private static final String FP_PROPERTY_KEY="net.bioclipse.cdk.fingerprint";
 
    SDFIndexEditorModel moleculesmodel;

    
    @Test
    public void TestReadParse232mols() throws InterruptedException, 
                                        URISyntaxException, 
                                        MalformedURLException, 
                                        IOException, 
                                        CoreException {

        URI uri = getClass().getResource(SDF_FILE).toURI();
        URL url=FileLocator.toFileURL(uri.toURL());

        IMoleculeTableManager moltable = net.bioclipse.cdk.ui.sdfeditor.
                               Activator.getDefault().getMoleculeTableManager();

        //Read index and parse properties
        IFile file = net.bioclipse.core.Activator.getVirtualProject()
        .getFile( "dbLookup.sdf" );
        if(!file.exists()) {
            InputStream is = url.openStream();
            file.create( is, true, null );
        }

        moleculesmodel=null;

        BioclipseJob<SDFIndexEditorModel> job1 = 
            moltable.createSDFIndex(file, 
                        new BioclipseJobUpdateHook<SDFIndexEditorModel>("job") {

                @Override
                public void completeReturn( SDFIndexEditorModel object ) {
                    moleculesmodel = object;
                }
            } );

        job1.join();
        assertNotNull( moleculesmodel );
        System.out.println("Number of mols: " + moleculesmodel
                                                .getNumberOfMolecules());
        assertEquals( 232, moleculesmodel.getNumberOfMolecules() );

        //We need to define that we want to read extra properties as well
        List<String> extraProps=new ArrayList<String>();
        extraProps.add( FP_PROPERTY_KEY );

        BioclipseJob<Void> job = moltable.
        parseProperties( moleculesmodel, 
                         extraProps, 
                         new BioclipseJobUpdateHook<Void>(
                         "Parsing SDFile for FP props"));

        //Wait for job to finish
        job.join();

        //Verify all entries
        for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){

            System.out.println("Processing index: " + i);
            
            ICDKMolecule mol=moleculesmodel.getMoleculeAt( i );
            //Verify we can get cdkmolecule
            assertNotNull("Could not get ICDKMolecule at index " + i, mol);

            //Verify FP property
            BitSet fp = moleculesmodel.getPropertyFor( i, FP_PROPERTY_KEY );
            assertNotNull("FP property is null for index " + i, fp );

            //Recalculate for mol to test that impl has not changed
            System.out.println("   molecule: " + mol);

        }
        
    }
    
}
