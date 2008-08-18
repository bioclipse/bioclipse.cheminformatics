 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.views;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.ui.Activator;
import net.bioclipse.cdk.ui.model.MoleculesFromSDF;
import net.bioclipse.core.domain.IMolecule;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.openscience.cdk.geometry.GeometryTools;

/**
 * This Provider provides text, image, and description for Molecules
 * @author ola
 *
 */
public class MoleculeLabelProvider implements ILabelProvider, 
                                              IDescriptionProvider {

    public Image getImage(Object element) {
        if (element instanceof SDFElement) {

            String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            
            return PlatformUI.getWorkbench().getSharedImages()
                                            .getImage(imageKey);
        }
        return null;
    }

    public String getText(Object element) {
        if (element instanceof SDFElement) {
            SDFElement mol = (SDFElement) element;
            return mol.getName();
        }
        if ( element instanceof MoleculesFromSDF ) {
            return ( (MoleculesFromSDF)element).getLabel( element );
        }
        if ( element instanceof PendingUpdateAdapter ) {
            return "Pending...";
        }
        return null;
    }

    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return true;
    }

    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    public String getDescription(Object anElement) {
        // TODO Auto-generated method stub
        return null;
    }

}
