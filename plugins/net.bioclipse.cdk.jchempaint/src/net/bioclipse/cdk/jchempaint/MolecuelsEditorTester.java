/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.part.MultiPageEditorPart;


/**
 * @author arvid
 *
 */
public class MolecuelsEditorTester extends PropertyTester {

    /**
     *
     */
    public MolecuelsEditorTester() {

        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test( Object receiver, String property, Object[] args,
                         Object expectedValue ) {

        MultiPageEditorPart part = (MultiPageEditorPart) receiver;
        if("isPartEditorActive".equals( property )) {
            String activeEditor= part.getPartProperty( "activePage" );
            if(activeEditor != null && activeEditor.equals( (String)expectedValue ))
                return true;
        }
        return false;
    }


}
