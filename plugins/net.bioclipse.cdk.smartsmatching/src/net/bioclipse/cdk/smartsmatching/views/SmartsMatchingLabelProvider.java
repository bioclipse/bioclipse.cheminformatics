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

import net.bioclipse.cdk.smartsmatching.Activator;
import net.bioclipse.cdk.smartsmatching.model.SmartsHit;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class SmartsMatchingLabelProvider implements ILabelProvider {

    Image wrapperWithHits=Activator.getImageDecriptor( "icons/testerr.gif" ).createImage();
    Image wrapperWithoutHits=Activator.getImageDecriptor( "icons/test.gif" ).createImage();
    Image chemObjectImage=Activator.getImageDecriptor( "icons/hit.gif" ).createImage();
    
    public Image getImage( Object element ) {

        if ( element instanceof SmartsWrapper ) {
            SmartsWrapper sw = (SmartsWrapper) element;
            if (sw.getHits()!=null && sw.getHits().size()>0){
                return wrapperWithHits;
            }
            return wrapperWithoutHits;
        }
        
        else if ( element instanceof SmartsHit ) {
            return chemObjectImage;
        }

        
        return null;
    }

    public String getText( Object element ) {
        if ( element instanceof SmartsWrapper ) {
            SmartsWrapper sw = (SmartsWrapper) element;
            if (sw.getHits()!=null){
                return sw.getName() + " ["+ sw.getHits().size()+ " matches]";
            }
            return sw.getName();
        }
        else if ( element instanceof SmartsHit ) {
            
            SmartsHit hit = (SmartsHit)element;
            if (hit.getAtomContainer()!=null)
                return hit.getName() + " ["+ hit.getAtomContainer().getAtomCount() + " atoms]";
            else
                return hit.getName();
        }

        return element.toString();
    }

    public void addListener( ILabelProviderListener listener ) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty( Object element, String property ) {
        return false;
    }

    public void removeListener( ILabelProviderListener listener ) {
    }
}
