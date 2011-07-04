package net.bioclipse.cdk.ui.filters;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * A filter implementation to remove unique entries from a collection.
 * 
 * @author ola
 *
 */
public class FilterOutUnique extends BaseFilter {

	private static Logger logger = Logger.getLogger( FilterOutUnique.class );

	@Override
	protected String getFilterName() {
		return "unique";
	}

	@Override
	protected List<ICDKMolecule> applyFilter(List<ICDKMolecule> mols,
			SubProgressMonitor monitor) {

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		monitor.beginTask("Filtering for unique entries", mols.size());
		
		Map<BitSet, ICDKMolecule> processed = new HashMap<BitSet, ICDKMolecule>();
		List<ICDKMolecule> newMols = new ArrayList<ICDKMolecule>();

		int cnt=0;
		int filteredNo=0;
		for (ICDKMolecule mol : mols){
				BitSet fp=null;
				try {
					fp = mol.getFingerprint(IMolecule.Property.USE_CACHED_OR_CALCULATED);
				} catch (BioclipseException e) {
					e.printStackTrace();
					logger.error("Could not calculate FP for mol: " + cnt + " so no filter applied for this.");
					continue;
				}
				ICDKMolecule retrieved = processed.get(fp);
				if (retrieved!=null){
					//Exists, could be same by FP, do full isomorph test
					if (cdk.areIsomorphic(mol, retrieved)){
						filteredNo++;
						logger.debug("Filtered out index " + cnt);
						continue;
					}
				}
				
				processed.put(fp, mol);
				newMols.add(mol);

			cnt++;
			monitor.worked(1);
		}

		logger.debug("Filtered out: " + filteredNo + " entries");
		monitor.done();

		return newMols;

	}





}
