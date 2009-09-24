/*******************************************************************************
 * Copyright (c) 2009 Arvid Berg <goglepox@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *      Arvid Berg
 *
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.ISortable;
import net.bioclipse.core.domain.IMolecule.Property;


public class ListMoleculesEditorModel implements IMoleculesEditorModel,
                ISortable {

    List<ICDKMolecule> molecules;
    List<ICDKMolecule> source;
    boolean dirty = false;

    public ListMoleculesEditorModel(List<ICDKMolecule> molecuels) {
        this.source = molecuels;
        this.molecules = source;
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        if(index < molecules.size())
            return molecules.get( index );
        else
            return null;
    }

    public int getNumberOfMolecules() { return molecules.size(); }

    public void markDirty( int index, ICDKMolecule moleculeToSave ) {
        molecules.set( index, moleculeToSave );
        dirty= true;
    }

    public void save() {
        throw new UnsupportedOperationException(
                               "Saving lists of molecules not supported");
    }

    public void setSortingProperties(final List<SortProperty<?>> properties ) {
        if(properties.isEmpty()) {
            molecules = source;
            return;
        }
        molecules = new ArrayList<ICDKMolecule>( source );
        Collections.sort( molecules, new Comparator<ICDKMolecule>() {
            private <T> Object getProperty(ICDKMolecule mol,SortProperty<T> prop) {
                T key = prop.getKey();
                Object value = null;
                if(key instanceof String)
                    value = mol.getProperty( (String)key, Property.USE_CACHED );
                if( value == null)
                    value = mol.getAtomContainer().getProperty( key );
                return value;
            }

            @SuppressWarnings("unchecked")
            public int compare( ICDKMolecule o1, ICDKMolecule o2 ) {
                SortProperty<?> property = properties.get( 0 );
                int dir = property.getDirection()==SortDirection.Descending?1:-1;

                Object prop = getProperty( o1, property );
                Object prop2 = getProperty( o2, property );
                if ( prop.getClass().equals( prop2.getClass() ) ) {
                    if ( prop instanceof Comparable<?> )
                        return ((Comparable) prop).compareTo( prop2 ) * dir;
                }
                return prop.toString().compareTo( prop2.toString() ) * dir;
            }
        });
    }

    public Collection<Object> getAvailableProperties() {
        if(!molecules.isEmpty())
            return molecules.get(0).getAtomContainer().getProperties().keySet();
        return Collections.emptyList();
    }

    public <T> void setPropertyFor( int moleculeIndex, String property, T value ) {

        molecules.get( moleculeIndex ).setProperty( property, value );

    }

}
