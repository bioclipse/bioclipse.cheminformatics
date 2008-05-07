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

import net.bioclipse.jmol.views.outline.JmolChain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The sequence section on the Chain tab in TabbedProperties
 * @author ola
 */
public class SequenceSection
    extends AbstractSection {

    Text sequenceText;

    public void createControls(Composite parent,
            TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        Composite composite = getWidgetFactory()
            .createFlatFormComposite(parent);
        FormData data;

        sequenceText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        sequenceText.setEditable(false);
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        sequenceText.setLayoutData(data);

        CLabel nameLabel = getWidgetFactory().createCLabel(composite, "Sequence:"); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(sequenceText,
            -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(sequenceText, 0, SWT.CENTER);
        nameLabel.setLayoutData(data);
    }

    /*
     * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#refresh()
     */
    public void refresh() {
        if (getJmolObject() instanceof JmolChain) {
            JmolChain ch=(JmolChain)getJmolObject();
            sequenceText.setText(ch.getSequence());
        }else {
            sequenceText.setText("??");
        }
    }
}