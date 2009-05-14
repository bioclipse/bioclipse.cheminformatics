package net.bioclipse.cdk.ui.domain;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

import org.eclipse.core.expressions.PropertyTester;

public class CDKMoleculeTester extends PropertyTester{

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		if (!(receiver instanceof ICDKMolecule)) return false;

		ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
		ICDKMolecule cdkmol = (ICDKMolecule) receiver;
		

		try {
			if ("has3d".equalsIgnoreCase(property)){
				boolean has3d=cdk.has3d(cdkmol);
				if (expectedValue instanceof Boolean) {
					boolean expected=(Boolean)expectedValue;
					if (has3d==expected) return true;
				}
				else return false;
			}

			if ("has2d".equalsIgnoreCase(property)){
				boolean has2d=cdk.has2d(cdkmol);
				if (expectedValue instanceof Boolean) {
					boolean expected=(Boolean)expectedValue;
					if (has2d==expected) return true;
				}
				else return false;
			}

		} catch (BioclipseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}



}
