package net.bioclipse.cdk.business.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.tests.AbstractManagerTest;


public class CDKManagerPluginTest extends AbstractCDKManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() {
        
        AbstractCDKManagerPluginTest.setupCDKManagerPluginTest();
        
        try {
            cdk = Activator.getDefault().getCDKManager();
        } 
        catch (RuntimeException exception) {
            fail("Failed to instantiate the CDK managers.");
        }
    }
}
