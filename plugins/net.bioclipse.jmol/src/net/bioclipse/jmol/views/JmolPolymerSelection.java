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
package net.bioclipse.jmol.views;
import java.util.HashSet;
/**
 * A selection of polymers in Jmol that can be propagated to the workbench
 * @author ola
 *
 */
public class JmolPolymerSelection extends JmolSelection {
        String monomer;
        String chain;
    public String getMonomer() {
                return monomer;
        }
        public void setMonomer(String monomer) {
                this.monomer = monomer;
        }
        public String getChain() {
                return chain;
        }
        public void setChain(String chain) {
                this.chain = chain;
        }
        public JmolPolymerSelection(String monomer, String chain) {
    	this.monomer=monomer;
    	this.chain=chain;
        selectionSet=new HashSet<String>();
        selectionSet.add(monomer + ":" + chain);
    }
}
