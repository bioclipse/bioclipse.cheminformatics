package net.bioclipse.cdk.ui.filters;

import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;

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

/**
 * A base handler to remove entries from molecules 
 * in a single SDFile based on a criteria.
 * 
 * @author ola
 *
 */
public abstract class BaseFilter extends AbstractHandler {

	private static Logger logger = Logger.getLogger( BaseFilter.class );

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
						+ " entries");
				monitor.worked(1);

				mols = applyFilter(mols, new SubProgressMonitor(monitor, 2));

				//Create output filename
				String newPath = file.getFullPath().toOSString()
				.replace(".sdf", "_" + getFilterName() + ".sdf");

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

	protected abstract String getFilterName();

	protected abstract List<ICDKMolecule> applyFilter(List<ICDKMolecule> mols,
			SubProgressMonitor subProgressMonitor);


}

