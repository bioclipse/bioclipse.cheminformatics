package net.bioclipse.cdk.ui.filters;

import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * A handler to remove salts from molecules in a single SDFile.
 * 
 * @author ola
 *
 */
public class FilterOutSalts extends AbstractHandler {

	private static Logger logger = Logger.getLogger( FilterOutSalts.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (!(sel instanceof IStructuredSelection)) {
			logger.error("Filter action did not have a File in Selection.");
		}
		IStructuredSelection ssel = (IStructuredSelection) sel;

		//Only operate on one SDF for now
		Object obj = ssel.getFirstElement();
		if (!(obj instanceof IFile)) {
			logger.error("Filter action did not have a File in Selection.");
		}
		final IFile file = (IFile) obj;

		Job job = new Job("Filtering SDF"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {


				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

				monitor.beginTask("Filtering SDF", 10);
				monitor.subTask("Reading file");

				List<ICDKMolecule> mols = null;
				try {
					mols=cdk.loadMolecules(file, new SubProgressMonitor(monitor, 3));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

				monitor.subTask("Filtering " + mols.size() 
						+ " entries for salts.");
				monitor.worked(1);

				filterOutSalts(mols, new SubProgressMonitor(monitor, 2));

				//Create output filename
				String newPath = file.getFullPath().toOSString()
				.replace(".sdf", "_nosalts.sdf");

				try {
					cdk.saveSDFile(newPath, mols, new SubProgressMonitor(monitor, 3));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				monitor.done();

				return Status.OK_STATUS;
			}

		};

		job.setUser(true);
		job.schedule();

		return null;
	}

	
	
	public static void filterOutSalts(List<ICDKMolecule> mols,
			IProgressMonitor monitor) {

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		monitor.beginTask("Filtering mols", mols.size());

		int cnt=0;
		int nosalts=0;
		for (ICDKMolecule mol : mols){
			try {
				List<IAtomContainer> containers = cdk.partition(mol);
				if (containers.size()!=1){
					IAtomContainer biggestAC=null;
					for (IAtomContainer ac : containers){
						if (biggestAC==null)
							biggestAC=ac;
						else if (ac.getAtomCount()>biggestAC.getAtomCount())
							biggestAC=ac;
					}
					//Copy properties to new AC from old
					biggestAC.setProperties(mol.getAtomContainer().getProperties());

					//Replace with largest AC
					int ix=mols.indexOf(mol);
					CDKMolecule newMol = new CDKMolecule(biggestAC);
					mols.set(ix, newMol);

					logger.debug("Filtered salts from entry: " + cnt);
					nosalts++;
				}
			} catch (BioclipseException e) {
				e.printStackTrace();
			}

			cnt++;
			monitor.worked(1);
		}

		logger.debug("Filtered out: " + nosalts + " salts");
		monitor.done();

	}

}
