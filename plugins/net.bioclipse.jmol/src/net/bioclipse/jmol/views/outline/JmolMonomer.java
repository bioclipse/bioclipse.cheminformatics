/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
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

import org.jmol.modelsetbio.Monomer;

/**
 * A class wrapping a Monomer in JmolContentOutline
 * @author ola
 */
public class JmolMonomer extends JmolObject{

    Monomer monomer;
    
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
        // TODO Auto-generated method stub
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

}
