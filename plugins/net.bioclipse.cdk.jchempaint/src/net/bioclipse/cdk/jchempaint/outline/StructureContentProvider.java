/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.outline;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMoleculeSet;
@SuppressWarnings("serial")
public class StructureContentProvider implements ITreeContentProvider {
    //Use logging
    private static final Logger logger = Logger.getLogger(StructureContentProvider.class);
    public StructureContentProvider() {}
    private static final String[][] symbolsAndNames = {
        { "H",  "Hydrogen"  },
        { "C",  "Carbon"    },
        { "N",  "Nitrogen"  },
        { "O",  "Oxygen"    },
        { "Na", "Sodium"    },
        { "Mg", "Magnesium" },
        { "P",  "Phosphorus"},
        { "S",  "Sulphur"   },
        { "Cl", "Chlorine"  },
        { "Ca", "Calcium"   },
        { "Fe", "Iron"      },
        { "Si", "Silica"    },
        { "Br", "Bromine"   },
    };
    private static final Map<String,String> elementNames
        = new HashMap<String,String>() {{
            for (String[] symbolAndName : symbolsAndNames)
                put(symbolAndName[0], symbolAndName[1]);
        }};
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Container) {
            Container container=(Container)parentElement;
            if (container.getChildren()!=null 
                    && container.getChildren().size()>0)
                return container.getChildren().toArray(new CDKChemObject[0]);
            else
                return new Object[0];
        }
        else if (parentElement instanceof CDKChemObject) {
            CDKChemObject chemobj=(CDKChemObject)parentElement;
            if (!(chemobj.getChemobj() instanceof IAtomContainer)) {
                return new Object[0];
            }
            IAtomContainer ac = (IAtomContainer) chemobj.getChemobj();
            Container atoms=new Container("Atoms");
            for (int i=0; i<ac.getAtomCount(); i++){
                IAtom atom = ac.getAtom(i);
                String symbol = atom.getSymbol(),
                       name = elementNames.containsKey( symbol )
                                  ? elementNames.get( symbol )
                                  : "unknown";
                CDKChemObject co
                  = new CDKChemObject( name + " (" + symbol + ")", atom );
                atoms.addChild(co);
            }
            Container bonds=new Container("Bonds");
            for (int i=0; i<ac.getBondCount(); i++){
                IBond bond = ac.getBond(i);
                StringBuilder sb = new StringBuilder();
                char separator
                  = bond.getOrder() == CDKConstants.BONDORDER_DOUBLE   ? '='
                  : bond.getOrder() == CDKConstants.BONDORDER_TRIPLE   ? '#'
                  : bond.getFlag(CDKConstants.ISAROMATIC) ? '~' : '-';
                for (Iterator<IAtom> it=bond.atoms().iterator(); it.hasNext();) {
                    sb.append(it.next().getSymbol());
                    if (it.hasNext()) {
                        sb.append(separator);
                    }
                }
                sb.append(   bond.getOrder() == CDKConstants.BONDORDER_DOUBLE
                               ? " (double)"
                           : bond.getOrder() == CDKConstants.BONDORDER_TRIPLE
                               ? " (triple)"
                           : bond.getFlag(CDKConstants.ISAROMATIC)
                               ? " (aromatic)"
                               : "" );
                CDKChemObject co=new CDKChemObject(sb.toString(), bond);
                bonds.addChild(co);
            }
            Object[] retobj=new Object[2];
            retobj[0]=atoms;
            retobj[1]=bonds;
            return retobj;
        }
        return new Object[0];
    }
    public Object getParent(Object element) {
        // TODO Auto-generated method stub
        return null;
    }
    public boolean hasChildren(Object element) {
        return (getChildren(element).length > 0);
    }
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IChemModel) {
            IChemModel model = (IChemModel) inputElement;
            IMoleculeSet ms=model.getMoleculeSet();
            if (ms==null || ms.getAtomContainerCount()<=0)
            {
                logger.debug("No AtomContainers in ChemModel.");
                return new Object[0];
            }
            CDKChemObject[] acs=new CDKChemObject[ms.getAtomContainerCount()];
            for (int i=0; i<ms.getAtomContainerCount(); i++){
                acs[i]=new CDKChemObject("AC_" + i, ms.getAtomContainer(i));
            }
            if (acs.length>1)
                return acs;
            else if (acs.length==1){
                return getChildren(acs[0]);
            }
        }
        return new IChemObject[0];
    }
    public void dispose() {
        // TODO Auto-generated method stub
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
    }
}
