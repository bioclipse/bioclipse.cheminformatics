package net.bioclipse.cdk.domain;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.ConformerContainer;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.core.domain.IMolecule;


public class CDKConformer extends CDKMolecule implements ICDKMolecule {

    private ConformerContainer conformerContainer;

    public CDKConformer(ConformerContainer conformerContainer) {
        super(null);
        this.conformerContainer=conformerContainer;
    }

    
    public ConformerContainer getConformerContainer() {
    
        return conformerContainer;
    }

    
    public void setConformerContainer( ConformerContainer conformerContainer ) {
    
        this.conformerContainer = conformerContainer;
    }

    @Override
    public List<IMolecule> getConformers() {

        List<IMolecule> conformers=new ArrayList<IMolecule>();
        Iterator it=conformerContainer.iterator();
        while ( it.hasNext() ) {
            IAtomContainer ac = (IAtomContainer) it.next();
            ICDKMolecule mol=new CDKMolecule(ac);
            conformers.add( mol );
        }
        return conformers;
    }
    
    
    
}
