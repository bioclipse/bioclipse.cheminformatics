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
package net.bioclipse.cdk.ui.views;

import java.util.List;

/**
 * Interface to enable sorting of an <tt>IMoleculesEditorModel</tt>
 * @author Arvid
 *
 */
public interface ISortable {
    enum SortDirection {
        Ascending, Descending 
    }
    /**
     * This class represent a property to be sorted and the direction of
     * the sorting.
     * @author Arvid
     *
     */
    public static class SortProperty<T>{
        private final T property;
        private final SortDirection direction;
        public SortProperty(T property, SortDirection direction) {
            this.property = property;
            this.direction = direction;
        }
        /**
         * Returns the key of the property to sort
         * @return property key to sort on
         */
        public T getKey() { return property;}
        
        /**
         * Returns the sorting direction 
         * @return direction to sort in.
         */
        public SortDirection getDirection() {return direction;}
    }
    
    /**
     * Sets what properties to sort on indicated by <code>SortProperty</code>.
     * Sorting should be done first on the first element of the list and then
     * on the second.
     * @param properties list of <code>SortProperty</code> indicating what to
     * sort on.
     */
    public void setSortingProperties(List<SortProperty<?>> properties);
}