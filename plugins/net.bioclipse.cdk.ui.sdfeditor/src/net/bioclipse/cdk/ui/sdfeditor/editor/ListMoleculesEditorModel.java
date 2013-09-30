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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.cdk.ui.views.ISortable;
import net.bioclipse.core.domain.IMolecule.Property;


public class ListMoleculesEditorModel implements IMoleculesEditorModel,
                ISortable {
    static class ListElement implements Comparable<ListElement>{
        static ListElement newElement(int index,ICDKMolecule mol) {
            return new ListElement(index,mol,false);
        }
        static ListElement changeElement(ListElement element,ICDKMolecule mol) {
            return new ListElement(element.index,mol,true);
        }
        
        int index;
        ICDKMolecule mol;
        boolean dirty;
        
        private ListElement(int index,ICDKMolecule mol,boolean dirty) {
            this.index = index;
            this.mol = mol;
            this.dirty = dirty;
        }
        
        public boolean isDirty() { return dirty;}
        public ICDKMolecule getMolecule() {
            return mol;
        }
        
        @Override
        public int compareTo( ListElement arg0 ) {
            return arg0.index - index;
        }
    }
    
    final List<ListElement> molecules;
    
    Comparator<ICDKMolecule> comparator= null;
    boolean dirty = false;

    public ListMoleculesEditorModel(List<ICDKMolecule> molecuels) {
        this.molecules = new ArrayList<ListElement>(molecuels.size());
        int index = 0;
        for(ICDKMolecule mol: molecuels) {
            this.molecules.add( ListElement.newElement(index,mol) );
            
        }
    }

    public ICDKMolecule getMoleculeAt( int index ) {
        if(index < molecules.size() || index >= 0)
            return molecules.get( index ).getMolecule();
        else
            return null;// index out of bounds
    }

    public int getNumberOfMolecules() { return molecules.size(); }

    public void markDirty( int index, ICDKMolecule moleculeToSave ) {
        molecules.set( index, ListElement.changeElement( molecules.get( index ), moleculeToSave ) );
        dirty=true;
    }
    
    public boolean isDirty(int index) {
        return molecules.get( index ).isDirty();
    }

    public void save() {
        throw new UnsupportedOperationException(
                               "Saving lists of molecules not supported");
    }

    public void setSortingProperties(final List<SortProperty<?>> properties ) {
        if(properties.isEmpty()) {
            Collections.sort( molecules);
            comparator = null;
            return;
        }
        comparator = new Comparator<ICDKMolecule>() {
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
        };
        sort();
    }

    private void sort() {
        if(comparator!=null) {
            Collections.sort( molecules, wrapComparator( comparator ) );
        }
    }
    
    static private <T extends Comparator<? super ICDKMolecule>> ListElementComparator<T> wrapComparator(T comparator) {
        return new ListElementComparator<T>( comparator );
    }
    static class ListElementComparator<T extends Comparator<? super ICDKMolecule>> implements Comparator<ListElement> {
        
        final T comparator;
        
        public ListElementComparator(T comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare( ListElement o1, ListElement o2 ) {
            return comparator.compare( o1.getMolecule(), o2.getMolecule() );
        }
        
    }

    public Collection<Object> getAvailableProperties() {
    	Set<Object> props = new HashSet<Object>();
        if(!molecules.isEmpty()) {
            List<ListElement> mols = molecules.subList( 0, Math.min(molecules.size(),100) );
            for(ListElement m:mols) {
                ICDKMolecule mol = m.getMolecule();
                props.addAll(mol.getAtomContainer().getProperties().keySet());
            }
        	return props;
        }
        return Collections.emptyList();
    }

    public <T> void setPropertyFor( int moleculeIndex, String property, T value ) {

        molecules.get( moleculeIndex ).getMolecule().setProperty( property, value );

    }

    public void instert( ICDKMolecule... molecules ) {
        List<ListElement> elements = new ArrayList<ListElement>(molecules.length);
        int molCount = this.molecules.size();
        for(int i = 0;i < molecules.length;i++) {
            elements.add( ListElement.newElement( molCount + i, molecules[i] ) );
        }
        this.molecules.addAll( elements);
        sort();
    }

    public void delete( int index ) {
        ListElement mol = molecules.remove( index );
    }
}
