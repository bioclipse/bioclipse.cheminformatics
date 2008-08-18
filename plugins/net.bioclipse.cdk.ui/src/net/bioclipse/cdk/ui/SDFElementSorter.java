/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui;

import net.bioclipse.cdk.domain.SDFElement;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;


public class SDFElementSorter extends ViewerSorter {

    @Override
    public int compare( Viewer viewer, Object e1, Object e2 ) {

        if ( e1 instanceof SDFElement && 
             e2 instanceof SDFElement ) {
            
            return ( (SDFElement) e1).getNumber() - 
                   ( (SDFElement) e2).getNumber();
        }

        return super.compare( viewer, e1, e2 );
    }
}
