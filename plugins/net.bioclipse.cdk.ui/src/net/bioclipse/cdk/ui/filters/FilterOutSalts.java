package net.bioclipse.cdk.ui.filters;

import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * A filter implementation to remove salts from a collection.
 * 
 * @author ola
 *
 */
public class FilterOutSalts extends BaseFilter {

	private static Logger logger = Logger.getLogger( FilterOutSalts.class );

	@Override
	protected String getFilterName() {
		return "noSalts";
	}

	@Override
    public List<ICDKMolecule> applyFilter( List<ICDKMolecule> mols,
                                              IProgressMonitor monitor ) {

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		monitor.beginTask("Filtering salts", mols.size());

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
		
		return mols;

	}





}
