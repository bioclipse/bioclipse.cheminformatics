/*******************************************************************************
 * Copyright (c) 2007 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.jmol.views.properties;

import net.bioclipse.jmol.views.outline.IJmolObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An abstract section for TabbedProperties
 * @author ola
 *
 */
public abstract class AbstractSection
    extends AbstractPropertySection implements PropertyChangeListener {

    private IJmolObject jmolObject;


    /**
     * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection)selection).getFirstElement();
        Assert.isTrue(input instanceof IJmolObject);
        this.jmolObject = (IJmolObject) input;
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#aboutToBeShown()
     */
    public void aboutToBeShown() {
//        getJmolObject().addPropertyChangeListener(this);
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#aboutToBeHidden()
     */
    public void aboutToBeHidden() {
//        getElement().removePropertyChangeListener(this);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }

    public IJmolObject getJmolObject() {
        return jmolObject;
    }

    public void setJmolObject(IJmolObject jmolObject) {
        this.jmolObject = jmolObject;
    }
}
