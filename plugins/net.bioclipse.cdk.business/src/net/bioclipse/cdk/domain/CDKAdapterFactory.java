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
                molecule = BioclipseStore.get( file, 
                                               ICDKMolecule.class ); 
                if ( molecule == null ) {
                    try {
                        molecule = Activator.getDefault()
                                            .getCDKManager()
                                            .loadMolecule( file );
                    } catch ( Exception e ) {
                        LogUtils.traceStringOf( e );
                        return null;
                    }
                    BioclipseStore.put( molecule, 
                                        file,
                                        ICDKMolecule.class );
                }
            }
        }
        return molecule;
    }

    public Class[] getAdapterList() {

        return new Class[] { ICDKMolecule.class };
    }
}
