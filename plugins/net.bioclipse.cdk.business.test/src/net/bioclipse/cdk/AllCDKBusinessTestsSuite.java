package net.bioclipse.cdk;

import net.bioclipse.cdk.business.test.CDKManagerTest;
import net.bioclipse.cdk.domain.tests.TestCDKMolecule;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(value=Suite.class)
@SuiteClasses( value = { CDKManagerTest.class,
                         TestCDKMolecule.class } )
public class AllCDKBusinessTestsSuite {

}
