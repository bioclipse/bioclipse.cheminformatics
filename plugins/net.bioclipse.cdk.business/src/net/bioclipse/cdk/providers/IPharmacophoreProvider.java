package net.bioclipse.cdk.providers;

import org.openscience.cdk.pharmacophore.PharmacophoreBond;


public interface IPharmacophoreProvider {

    PharmacophoreBond getConstraint();
    
}
