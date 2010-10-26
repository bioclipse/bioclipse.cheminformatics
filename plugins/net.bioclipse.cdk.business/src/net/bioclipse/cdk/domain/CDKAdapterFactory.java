package net.bioclipse.cdk.domain;

import net.bioclipse.cdk.business.Activator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;

@SuppressWarnings("unchecked")
public class CDKAdapterFactory implements IAdapterFactory {

    Logger logger = Logger.getLogger( CDKAdapterFactory.class );
    
    public Object getAdapter( Object adaptableObject, 
                              Class adapterType ) {

        Object molecule = null;
        if ( adaptableObject instanceof IFile ) {
            IFile file = (IFile) adaptableObject;
            if ( adapterType.equals( ICDKMolecule.class ) ) {
                if ( molecule == null ) {
                    try {
                        molecule = Activator.getDefault()
                                            .getJavaCDKManager()
                                            .loadMolecule( file );
                    } catch ( Exception e ) {
                        logger.log( Level.DEBUG, "Caught exception", e );
                        return null;
                    }
                }
            }
        }
        if(molecule !=null &&adapterType.isAssignableFrom( molecule.getClass()))
            return molecule;
        return null;
    }

    public Class[] getAdapterList() {

        return new Class[] { ICDKMolecule.class };
    }
}
