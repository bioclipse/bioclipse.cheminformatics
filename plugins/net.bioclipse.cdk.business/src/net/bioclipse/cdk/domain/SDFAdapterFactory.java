package net.bioclipse.cdk.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SDFAdapterFactory implements IAdapterFactory {
    Logger logger = Logger.getLogger(this.getClass());
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        ICDKMolecule molecule=null;
        if(adaptableObject instanceof SDFElement){
            SDFElement element=(SDFElement)adaptableObject;
            URI elementURI;
            URI fileURI=element.getResource().getLocationURI();
            try{
                elementURI = new URI( fileURI.getScheme().toString(),
                                      fileURI.getSchemeSpecificPart(),
                                      Integer.toString(element.getNumber()));
            }catch(URISyntaxException x){
                logger.debug(x);
                return null;
            }
            molecule=(ICDKMolecule)BioclipseStore.get(elementURI,adapterType);
            if(molecule==null){
                molecule=loadSDFPart( element);
                if(molecule!=null)
                    BioclipseStore.put( molecule,elementURI,
                                        ICDKMolecule.class);
            
        }
        }
        return molecule;
    }

    public Class[] getAdapterList() {
       return new Class[]{ICDKMolecule.class};
    }
    /* 
     * Extract and load a part of an SDFile by creating a new Virtual resource
     */
    private ICDKMolecule loadSDFPart(SDFElement element){
        int index=element.getNumber();
        IFile sourceFile=(IFile)element.getResource();
        InputStream is=null;
        IFile file=null;
        IFolder folder = null;
        try{
        logger.debug( "Loading "
                      + sourceFile.getName()
                      +"#"+index+", "
                      +element.getPosition());
        is=sourceFile.getContents();
        is.skip(element.getPosition());
        Scanner sc=new Scanner(is);
        sc.useDelimiter("\\${3}");        
        String data=sc.next();
        
        folder=net.bioclipse.core.Activator.getVirtualProject().
                                                           getFolder("SDFTemp");
        if(!folder.exists())
           folder.create(true,false,null);
        file=folder.getFile(
                       sourceFile.getName()+"_"+Integer.toString(index)+".sdf");
        
        file.create( new ByteArrayInputStream(data.getBytes()),
                     true,
                     new NullProgressMonitor());
        List<ICDKMolecule> result=
            Activator.getDefault().getCDKManager().loadMolecules(file);
        assert(result.size()<2);
        is.close();
        file.delete(true,null);
        return result.get(0);
        }catch(CoreException e){
            logger.debug(e);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            logger.debug(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.debug(e);
        } catch (BioclipseException e) {
            // TODO Auto-generated catch block
            logger.debug(e);
        }finally{
            try {
                if(is!=null) is.close();
                if(file!=null) file.delete(true,null);
                if(folder!=null) folder.delete(true,null );
            }catch (CoreException e) {
                // TODO Auto-generated catch block
               logger.debug(e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                logger.debug(e);
            } 
               
        }
        
        return null;
    }   
}
