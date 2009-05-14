/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching.views;

import java.util.List;

import net.bioclipse.cdk.smartsmatching.model.SmartsHit;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class SmartsMatchingContentProvider implements ITreeContentProvider {

    public Object[] getChildren( Object parentElement ) {
        if ( parentElement instanceof SmartsWrapper ) {
            SmartsWrapper sw = (SmartsWrapper) parentElement;
            if (sw.getHits()!=null && sw.getHits().size()>0){
                return sw.getHits().toArray();
            }
        }
        return null;
    }

    public Object getParent( Object element ) {
        if ( element instanceof SmartsHit ) {
            SmartsHit hit = (SmartsHit) element;
            return hit.getParent();
        }
        return null;
    }

    public boolean hasChildren( Object element ) {
        return getChildren( element )!=null ? getChildren( element ).length>0 : false;
    }

    public Object[] getElements( Object inputElement ) {
        
        if ( inputElement instanceof List<?> ) {
            List<?> lst = (List<?>) inputElement;
            return lst.toArray();
        }
        return null;
    }

    
    public void dispose() {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }

}
