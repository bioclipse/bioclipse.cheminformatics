/*******************************************************************************
 * Copyright (c) 2013  Klas Jšnsson <klas.joensson@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.smilesURL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.editor.JCPCellPainter;
import net.bioclipse.core.business.BioclipseException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class SmilesURL extends URLConnection {

    private Logger logger = Logger.getLogger( SmilesURL.class );
    
    protected SmilesURL(URL url) {
        super( url );
        
    }

    @Override
    public void connect() throws IOException {  }
    
    public InputStream getInputStream() throws IOException {
        String smiles = getURL().getPath();
        ICDKMolecule mol;
        try {
            mol = SmilesProtocol.smilesToMolecule( smiles );
        } catch ( BioclipseException e ) {
            logger.error( "Could not create a molecule for the tool-tip: " + 
                    e.getMessage() );
            return null;
        }
        
        return drawMolecue( mol );
    }
    
    public String getContentType() {
        return "image";
    }
    
    public static InputStream drawMolecue(ICDKMolecule molecule) {
        JCPCellPainter painter = new JCPCellPainter();        
        Rectangle rect = new Rectangle( 0, 0, 25, 25 );
        Display display = Display.getDefault();
        Image image = new Image( display, rect );
        GC gc = new GC( image );
        painter.getColumnImage( gc, rect, molecule );
        ImageData imageData = image.getImageData();
        ImageLoader loader = new ImageLoader();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        loader.data = new ImageData[] {imageData};
        loader.save( bao, SWT.IMAGE_PNG );
        
        return new ByteArrayInputStream( bao.toByteArray() );
    }
}
