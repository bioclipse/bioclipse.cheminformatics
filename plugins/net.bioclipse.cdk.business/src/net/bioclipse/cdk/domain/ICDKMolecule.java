package net.bioclipse.cdk.domain;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.openscience.cdk.interfaces.IAtomContainer;

//TODO: Should this interface extend cdk.interfaces.IMoleule?
public interface ICDKMolecule extends IMolecule{

	public String getCML() throws BioclipseException;
	public String getFingerprint() throws BioclipseException;
	public IAtomContainer getAtomContainer();

}
