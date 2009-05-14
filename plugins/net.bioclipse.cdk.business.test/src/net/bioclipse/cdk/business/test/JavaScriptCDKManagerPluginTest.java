package net.bioclipse.cdk.business.test;

import net.bioclipse.cdk.business.Activator;

import org.junit.BeforeClass;

public class JavaScriptCDKManagerPluginTest 
       extends AbstractCDKManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
            cdk = Activator.getDefault().getJavaScriptCDKManager();
    	debug = net.bioclipse.cdkdebug.Activator.getDefault()
    	    .getJavaScriptManager();
    }
}
