/* Copyright (C) 2009  Arvid Berg <goglepox@users.sf.net>
 *
 * Contact: cdk-devel@list.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.selection;

import static org.openscience.cdk.graph.ConnectivityChecker.partitionIntoMolecules;
import static org.openscience.cdk.tools.manipulator.ChemModelManipulator.getRelevantAtomContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMoleculeSet;

/**
 * 
 * @author Arvid
 * @cdk.module rendercontrol
 */
public class LinkedSelection implements IChemObjectSelection {
    
    IChemObject start;
    IAtomContainer selected;
    

    public LinkedSelection(IChemObject selected) {
        this.start = selected;
    }
    public boolean contains( IChemObject obj ) {
        if(selected == null) return false;
        if(obj instanceof IAtom)
            return selected.contains( (IAtom)obj );
        else if( obj instanceof IBond)
            return selected.contains( (IBond)obj );
        return false;
    }

    @SuppressWarnings("unchecked")
    public <E extends IChemObject> Collection<E> elements( Class<E> clazz ) {
        Set<E> set = new HashSet<E>();
        if(clazz.isAssignableFrom( IAtom.class )){
            for(IAtom atom:selected.atoms())
                set.add((E) atom);
        }
        if(clazz.isAssignableFrom( IBond.class )){
            for(IBond bond:selected.bonds())
                set.add((E) bond);
        }
        return set;
    }

    public IAtomContainer getConnectedAtomContainer() {
        return selected;
    }

    public boolean isFilled() {
        return selected!=null && selected.getAtomCount()!=0;
    }

    public void select( IChemModel chemModel ) {
            IAtomContainer container;
            if(start instanceof IAtom) {
                IAtom atom = (IAtom)start;
                container = getRelevantAtomContainer( chemModel, atom );
                IChemModel parts = getParts( container );
                selected = getRelevantAtomContainer( parts, atom );
            }
            else if(start instanceof IBond) {
                IBond bond = (IBond)start;
                container = getRelevantAtomContainer( chemModel, bond );
                IChemModel parts = getParts( container );
                selected = getRelevantAtomContainer( parts, bond );
            }
            else if (start instanceof IAtomContainer) {
                IAtomContainer result = chemModel.getBuilder().newAtomContainer();
                container = chemModel.getMoleculeSet().getAtomContainer( 0 );
                IChemModel parts = getParts( container );
                for(IAtom atom:((IAtomContainer)start).atoms()) {
                    result.add(getRelevantAtomContainer( parts, atom ));
                }
                selected = result;
            }
    }
    
    private IChemModel getParts(IAtomContainer container) {
        IMoleculeSet molecules = partitionIntoMolecules(container);
        IChemModel parts = container.getBuilder().newChemModel();
        parts.setMoleculeSet( molecules );
        return parts;
    }
}
