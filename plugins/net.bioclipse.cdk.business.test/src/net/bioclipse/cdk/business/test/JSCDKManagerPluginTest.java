package net.bioclipse.cdk.business.test;

import static org.junit.Assert.fail;
import net.bioclipse.cdk.business.Activator;

import org.junit.BeforeClass;


public class JSCDKManagerPluginTest extends AbstractCDKManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() {
        
        AbstractCDKManagerPluginTest.setupCDKManagerPluginTest();
        
        try {
            cdk = Activator.getDefault().getJSCDKManager();
        } 
        catch (RuntimeException exception) {
            fail("Failed to instantiate the CDK managers.");
        }
    }
}
