package net.bioclipse.cdk.domain;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.util.LogUtils;

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
                molecule = BioclipseStore.get( file, ICDKMolecule.class ); 
                if ( molecule == null ) {
                    try {
                        molecule = Activator.getDefault()
                                            .getCDKManager()
                                            .loadMolecule( file );
                    } catch ( Exception e ) {
                        logger.debug( LogUtils.traceStringOf( e ));
                        return null;
                    }
                    BioclipseStore.put( file,ICDKMolecule.class,molecule);
                }
            }
            else if (adapterType.equals( Node.class )) {
                molecule = BioclipseStore.get( file, Node.class );
                if(! (molecule instanceof Node) )
                    molecule = null;
            }
        }
        if(molecule !=null &&adapterType.isAssignableFrom( molecule.getClass()))
            return molecule;
        return null;
    }

    public Class[] getAdapterList() {

        return new Class[] { ICDKMolecule.class,
                             Node.class };
    }
}
