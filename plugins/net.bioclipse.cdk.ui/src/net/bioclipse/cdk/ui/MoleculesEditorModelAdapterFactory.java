package net.bioclipse.cdk.ui;

import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IFileEditorInput;

public class MoleculesEditorModelAdapterFactory implements IAdapterFactory {
    
    public Object getAdapter( Object adaptableObject, Class adapterType ) {
        IFile file = null;
        if ( adapterType.isAssignableFrom( IMoleculesEditorModel.class ) ) {
            if ( adaptableObject instanceof IFile ) {
                file = (IFile) adaptableObject;                
            } else if ( adaptableObject instanceof IFileEditorInput ) {
                file = ((IFileEditorInput) adaptableObject).getFile();                
            }
            if ( file!=null && file.getFileExtension().equalsIgnoreCase( "sdf" ) )
                return new MoleculesFromSDF( file );
        }
        return null;
    }

    public Class[] getAdapterList() {

        return new Class[] { IMoleculesEditorModel.class };
    }

}
