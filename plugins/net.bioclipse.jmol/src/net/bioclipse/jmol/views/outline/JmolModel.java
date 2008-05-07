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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Model;

/**
 * A class wrapping a Model in JmolContentOutline
 * @author ola
 */
public class JmolModel extends JmolObject{

    private Model model;

    /**
     * Construct a JmolModel from a Model. Set name to 'Model ' + modelIndex
     * @param model
     */
    public JmolModel(Model model) {
        this.model=model;
        setName("Model " + model.getModelIndex());
    }

    public Object getObject() {
        return model;
    }

    public void setObject(Object object) {
        if (object instanceof Model) {
            model=(Model)object;
        }
    }

    /**
     * Create the children as all Chains of this Model
     */
    public void createChildren() {

        ArrayList<IJmolObject> newChildren=new ArrayList<IJmolObject>();
        ArrayList<IJmolObject> newEmptyChildren=new ArrayList<IJmolObject>();
        for (int i=0; i< model.getChainCount(); i++){
            Chain chain=model.getChains()[i];
            
            //This will not create Chain if chainID is not available
            if (chain.getChainID()>0){
                JmolChain c=new JmolChain(chain);
                newChildren.add(c);
            }else{
                JmolChain c=new JmolChain(chain);
                c.setName("CHAIN");
                newEmptyChildren.add(c);
            }
        }
        
        //If we have chains with ID, do not add chains without ID
        if (newChildren.isEmpty())
            setChildren(newEmptyChildren);
        else
            setChildren(newChildren);
    }

    public Object getAdapter(Class adapter) {
        return super.getAdapter(adapter);
    }

    /**
     * Models are not selected, hence returns null
     */
    public String getSelectString() {
        return null;
    }


}
