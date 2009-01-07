/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.outline;

import org.eclipse.ui.views.properties.IPropertySource;
import org.jmol.modelsetbio.Monomer;
import org.jmol.modelsetbio.ProteinStructure;
import org.jmol.viewer.JmolConstants;

/**
 * A class wrapping a Monomer in JmolContentOutline
 * @author ola
 */
public class JmolMonomer extends JmolObject{

    Monomer monomer;
	private MonomerPropertySource monomerPropSrc;
    
    /**
     * Construct a JmolChain for a Chain. Set name to ChainID
     * @param chain
     */
    public JmolMonomer(Monomer monomer) {
        this.monomer=monomer;
        if (monomer.getSeqcodeString()!=null){
            setName(monomer.toString());
        }
//        monomer.getGroup3(monomer.getGroupID());
    }

    
    public Object getObject() {
        return monomer;
    }

    public void setObject(Object object) {
        monomer=(Monomer)object;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            if (monomerPropSrc == null) {
                // cache the chainPropSource
                monomerPropSrc = new MonomerPropertySource(this);
            }
            return monomerPropSrc;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void createChildren() {
        //No children yet
    }


    /**
     * Return monomerNo + ":" + chainID to select only this monomer 
     * or monomerNo if no chainID exists
     */
    public String getSelectString() {
        if (monomer==null) return null;
        if (monomer.getChainID()<=0) return monomer.getSeqcodeString(); 
        String ret=monomer.getSeqcodeString() + ":" + monomer.getChainID();
        return ret;
    }

    
    public String getProteinStructure() {
    	if (monomer.getProteinStructureType()>0){
    		String str=JmolConstants.getProteinStructureName(
    								monomer.getProteinStructureType());
    		if (str!=null)
    			return "" + str;
    	}

    	return "N/A";
    	
    }

    public String getAtomCount() {
    	int s=monomer.getFirstAtomIndex();
    	int e= monomer.getLastAtomIndex();
    	
    	return "" + (e-s);
    }
    
    
}
