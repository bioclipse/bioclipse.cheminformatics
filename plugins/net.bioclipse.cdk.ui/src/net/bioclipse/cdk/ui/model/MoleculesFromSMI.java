package net.bioclipse.cdk.ui.model;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.BioclipseStore;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;


public class MoleculesFromSMI implements IMoleculesFromFile {
    Logger logger = Logger.getLogger( MoleculesFromSMI.class );
    IFile file;
    List<SDFElement> molecules;
    
    public MoleculesFromSMI(IFile file) {
       this.file = file;
       molecules = Collections.synchronizedList( new LinkedList<SDFElement>());
    }

    public Object getMoleculeAt( int index ) {
        
        if( molecules.size()> index) {
            return molecules.get( index );
        }
        return null;
    }

    public int getNumberOfMolecules() {

        return molecules.size();
    }

    public void save() {

        throw new UnsupportedOperationException("Can't save SMILES yet.");

    }

    public void fetchDeferredChildren( Object object,
                                       IElementCollector collector,
                                       IProgressMonitor monitor ) {
      
       ICDKManager manager = Activator.getDefault().getCDKManager();
       try {
           // TODO : merge with MoleculesFromSMI
        List<ICDKMolecule> mols = manager.loadMolecules( file, monitor );
        // TODO : Builder thread and Node just as MoleculesFromSDF
        for(int i=0;i<mols.size();i++) {
            ICDKMolecule molecule = mols.get( i );
            SDFElement element = new SDFElement( file,
                                                 molecule.getName(),
                                                 -1,
                                                 i);
            // FIXME : maybe problem when file changes resource listener should
            // take care of it
            BioclipseStore.put( file, element, molecule );
            collector.add( element, monitor );
            molecules.add(element);
        }
        
        
    } catch ( IOException e ) {
        // TODO Auto-generated catch block
       LogUtils.debugTrace( logger, e );
    } catch ( BioclipseException e ) {
        // TODO Auto-generated catch block
        LogUtils.debugTrace( logger, e );
    } catch ( CoreException e ) {
        // TODO Auto-generated catch block
        LogUtils.debugTrace( logger, e );
    } finally {
        monitor.done();
    }
        
    }

    public ISchedulingRule getRule( Object object ) {

        // TODO Auto-generated method stub
        return null;
    }

    public boolean isContainer() {
        return true;
    }

    public Object[] getChildren( Object o ) {

        return molecules.toArray();
    }

    public ImageDescriptor getImageDescriptor( Object object ) {
        
   // TODO : Implement for specific SMILES icon and update MoleculeLabelProvider
        return null;
    }

    public String getLabel( Object o ) {
        return "Molecules";
    }

    public Object getParent( Object o ) {

       return file;
    }

}
