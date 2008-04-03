package net.bioclipse.cdk.domain;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public interface ICDKMolecule extends IMolecule{

	public String getCML() throws BioclipseException;
	public String getFingerprint() throws BioclipseException;

}
