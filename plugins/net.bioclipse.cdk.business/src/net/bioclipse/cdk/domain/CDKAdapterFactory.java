package net.bioclipse.cdk.domain;

import java.io.IOException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

@SuppressWarnings("unchecked")
public class CDKAdapterFactory implements IAdapterFactory {

    
    public Object getAdapter( Object adaptableObject, 
                              Class adapterType ) {

        Object molecule = null;
        if ( adaptableObject instanceof IFile ) {
            IFile file = (IFile) adaptableObject;
            if ( adapterType.equals( ICDKMolecule.class ) ) {
                molecule = BioclipseStore.get( file, file ); 
                if ( molecule == null ) {
                    try {
                        molecule = Activator.getDefault()
                                            .getCDKManager()
                                            .loadMolecule( file );
                    } catch ( Exception e ) {
                        LogUtils.traceStringOf( e );
                        return null;
                    }
                    BioclipseStore.put( file,file,molecule); 
//                                        file,
//                                        ICDKMolecule.class );
                }
            }
            else if (adapterType.equals( Node.class )) {
                molecule = BioclipseStore.get( file, file );
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
