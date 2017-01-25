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

import java.awt.Point;
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
    private final String STANDARD_SIDE_SIZE = "50";
    
    protected SmilesURL(URL url) {
        super( url );
        
    }

    @Override
    public void connect() throws IOException {  }
    
    public InputStream getInputStream() throws IOException {
        String smiles = getURL().getPath();
        String query = getURL().getQuery();
        Point imageSize = queryToPoint(query);
        
        ICDKMolecule mol;
        try {
            mol = SmilesProtocol.smilesToMolecule( smiles );
        } catch ( BioclipseException e ) {
            logger.error( "Could not create a molecule for the tool-tip: " + 
                    e.getMessage() );
            return null;
        }
        
        return drawMolecue( mol, imageSize );
    }
    
    private Point queryToPoint( String query ) {
        int height = -1, width = -1;
        
        // The expected format of the query-string is: "height=[height]&width=[width]"
        String[] strValues = query.split( "&" );
        for (String data:strValues){
            try {
            if (data.startsWith( "height" )) {
                height = Integer.parseInt( data.substring( 7 ) );
            } else if (data.startsWith( "width" )) {
                width = Integer.parseInt( data.substring( 6 ) );
            }
            } catch (NumberFormatException e) {
                logger.error( "Could not decide " + 
                        data.substring( 0, data.indexOf( "=")-1 ) +
                        " the tool-tip image: " + 
                        e.getMessage() );
            }
            
        }

        if (height == -1 && width == -1) {
            height = Integer.parseInt( STANDARD_SIDE_SIZE );
            width = Integer.parseInt( STANDARD_SIDE_SIZE );
        } else if (height == -1)
            height = width;
        else if (width == -1)
            width = height;
        
        return new Point( width, height);
    }

    public String getContentType() {
        return "image";
    }

    public static InputStream drawMolecue(ICDKMolecule molecule, Point size) {
        JCPCellPainter painter = new JCPCellPainter();        
        Rectangle rect = new Rectangle( 0, 0, size.x, size.y );
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
