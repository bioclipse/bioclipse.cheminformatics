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

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertySource;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;
import org.jmol.modelsetbio.Monomer;

/**
 * A class wrapping a Chain in JmolContentOutline
 * @author ola
 */
public class JmolChain extends JmolObject{

    Chain chain;
    ChainPropertySource chainPropSrc;
    
    /**
     * Construct a JmolChain for a Chain. Set name to ChainID
     * @param chain
     */
    public JmolChain(Chain chain) {
        this.chain=chain;
        if (chain.getChainID()>0){
            setName("Chain " + String.valueOf(chain.getChainID()));
        }
    }

    public Object getObject() {
        return chain;
    }

    public void setObject(Object object) {
        chain=(Chain)object;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            if (chainPropSrc == null) {
                // cache the chainPropSource
                chainPropSrc = new ChainPropertySource(this);
            }
            return chainPropSrc;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Create the children as all Monomers of this Chain
     */
    public void createChildren() {

        ArrayList<IJmolObject> newChildren=new ArrayList<IJmolObject>();

        for (int i=0; i< chain.getGroupCount(); i++){
            Group group=chain.getGroup(i);
            if (group instanceof Monomer) {
                Monomer monomer = (Monomer) group;
                JmolMonomer m=new JmolMonomer(monomer);
                newChildren.add(m);
            }
            else {
            	JmolGroup jgroup=new JmolGroup(group,chain);
                newChildren.add(jgroup);
            }
        }
        setChildren(newChildren);
    }

    /**
     * Returns "*:" + chainID to select entire chain
     */
    public String getSelectString() {
        if (chain==null) return null;
        if (chain.getChainID()<=0) return "*";
        String ret="*:" + chain.getChainID();
        return ret;
    }


    /**
     * @return the sequence of this chain (aminoacid or nucleotide)
     */
    public String getSequence(){

        if (chain==null) return "";
        String seq="";
        
        for (int i=0; i<chain.getGroupCount();i++){
            if (chain.getGroup(i) instanceof Monomer) {
                Monomer monomer = (Monomer) chain.getGroup(i);
                seq+=monomer.getGroup1();
            }
        }
        return seq;
    }

}
