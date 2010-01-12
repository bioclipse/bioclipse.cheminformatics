/*******************************************************************************
* Copyright (c) 2009 Arvid Berg <goglepox@users.sourceforge.net
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Arvid Berg
******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor.nattable;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


/**
 * @author arvid
 *
 */
public class ColorProviderPainter extends CellPainterWrapper {

    
    @Override
    public void paintCell( LayerCell cell, GC gc, Rectangle bounds,
                           IConfigRegistry configRegistry ) {
    
        Object dataValue = cell.getDataValue();
        if(dataValue instanceof IColorProvider) {
            Color backgroundColor = ((IColorProvider)dataValue).getBackground( null );
            if (backgroundColor != null) {
                Color originalBackground = gc.getBackground();

                gc.setBackground(backgroundColor);
                gc.fillRectangle(bounds);

                gc.setBackground(originalBackground);
        }
        }
        super.paintCell(cell, gc, bounds, configRegistry);
    }
}
