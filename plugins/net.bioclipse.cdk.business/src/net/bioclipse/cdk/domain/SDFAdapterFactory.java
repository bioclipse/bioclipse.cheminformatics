package net.bioclipse.cdk.domain;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
public class SDFAdapterFactory implements IAdapterFactory {
    Logger logger = Logger.getLogger(this.getClass());
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        ICDKMolecule molecule=null;
        if(MoleculesIndexEditorInput.class.isAssignableFrom( adapterType )) {
          if(adaptableObject instanceof SDFElement) {
              SDFElement element = (SDFElement) adaptableObject;
              return new MoleculesIndexEditorInput(element);
          }            
        } else
        if(adaptableObject instanceof SDFElement){
            SDFElement element=(SDFElement)adaptableObject;
            if(element.getResource() !=null )
                molecule=(ICDKMolecule)BioclipseStore.get( 
                                                         element.getResource(),
                                                         element);
            if(molecule==null){
                molecule=loadSDFPart( element);
                if(molecule!=null)
                    BioclipseStore.put( element.getResource(),element,molecule);
        }
        }
        return molecule;
    }
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
       return new Class[]{ICDKMolecule.class,MoleculesIndexEditorInput.class};
    }
    /* 
     * Extract and load a part of an SDFile by creating a new Virtual resource
     */
    private ICDKMolecule loadSDFPart(SDFElement element){
        int index=element.getNumber();
        IFile file=null;
        IFolder folder = null;
        IFile sourceFile=(IFile)element.getResource();
        try{
            logger.debug( "Loading " + sourceFile.getName() + "#"
                          + element.getNumber() + ", " + element.getPosition() );
            String data = getSDFPart( element );
            folder =
                    net.bioclipse.core.Activator.getVirtualProject()
                            .getFolder( "SDFTemp" );
            if ( !folder.exists() )
                folder.create( true, false, null );
            file =
                    folder.getFile( sourceFile.getName() + "_"
                                    + Integer.toString( index ) + ".sdf" );
            file.create( new ByteArrayInputStream( data.getBytes() ), true,
                         new NullProgressMonitor() );
            ICDKMolecule result =
                    Activator.getDefault().getCDKManager().loadMolecule( file );
            return result;
        }catch(CoreException e){
            LogUtils.debugTrace( logger,e);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger,e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger,e);
        } catch (BioclipseException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger,e);
        }finally{
            try {                
                if(file!=null) file.delete(true,null);
                if(folder!=null) folder.delete(true,null );
            }catch (CoreException e) {
                // TODO Auto-generated catch block
                LogUtils.debugTrace( logger,e);
            } 
        }
        return null;
    }  
    public static String getSDFPart(SDFElement element) throws CoreException, 
                                                        IOException {
        InputStream is=null;
        try {
        IFile sourceFile = (IFile) element.getResource();
        is=sourceFile.getContents();
        is.skip(element.getPosition());
        Scanner sc=new Scanner(is);
        sc.useDelimiter("\\${3}");        
        return sc.next();
        }
        finally {
            is.close();
        }
    }
}
