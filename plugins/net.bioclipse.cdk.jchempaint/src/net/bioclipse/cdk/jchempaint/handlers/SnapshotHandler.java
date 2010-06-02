/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import java.net.URI;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class SnapshotHandler extends AbstractHandler {
    
    Logger logger = Logger.getLogger( SnapshotHandler.class );

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        IEditorPart editor = HandlerUtil.getActiveEditor( event );
        if(editor==null) return null;
        final JChemPaintEditor jcp = 
                (JChemPaintEditor) editor.getAdapter( JChemPaintEditor.class );
        if(jcp==null) return null;
        
        Display.getDefault().asyncExec( new Runnable() {
            
            public void run() {
                showDialog( jcp );
            }
        });
        
        return null;
    }
    
    private void showDialog(JChemPaintEditor editor) {
        
        IFile file = (IFile) editor.getEditorInput().getAdapter( IFile.class );
        String destFile = "image.png";
        if(file!=null) {
            URI uri = file.getLocationURI();
            String path = uri.getPath();
            path = path.replaceFirst( ".*/", "" );
            int dot = path.lastIndexOf( "." );
            if(dot==-1)
                path = path+".";
            else
                path = path.substring( 0, dot+1 );
            path = path +"png";
            destFile = path;
        }
        
        FileDialog dialog = new FileDialog( 
                                           editor.getWidget().getShell(),
                                           SWT.SAVE | SWT.SHEET);
        dialog.setFileName( destFile );
        dialog.setText("Save Image");
        dialog.setFilterPath(System.getProperty( "user.home" ));
        dialog.setFilterIndex( 1 );
        String selectedDirectoryName = dialog.open();
        
        if ( selectedDirectoryName == null ) {
            return;
        }

        int dot = selectedDirectoryName.lastIndexOf( "." );
        if(dot==-1)
            selectedDirectoryName = selectedDirectoryName+".png";
        
        Image image = editor.getWidget().snapshot();
        final ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { image.getImageData() };
        loader.save(selectedDirectoryName, SWT.IMAGE_PNG);
    }
}
