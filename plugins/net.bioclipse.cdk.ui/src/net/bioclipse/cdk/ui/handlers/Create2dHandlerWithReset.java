/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * Stefan Kuhn
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.interfaces.IAtom;

/**
 * A handler class for a Generate 2D Coordinates menu item
 */
public class Create2dHandlerWithReset extends AbstractHandler {

    private static final Logger logger =
                                               Logger
                                                       .getLogger( Create2dHandlerWithReset.class );
    public int                  answer;

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        doCreation(true,false);
        return null;
    }
    
    /**
     * This method creates the coordinates and saves the file on the selection.
     * 
     * @param withReset If true, the other set of coordinates is set to null. This should be used on mol files, since these can only hold one set (3d or 2d).
     * @param make3D true = 3d is generated, false = 2d is generated.
     */
    public static void doCreation(boolean withReset, boolean make3D){
        ISelection sel =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getSelectionService().getSelection();
      if ( !sel.isEmpty() ) {
          if ( sel instanceof IStructuredSelection ) {
              MessageBox mb =
                  new MessageBox( new Shell(), SWT.YES | SWT.NO | SWT.CANCEL
                                               | SWT.ICON_QUESTION );
              IStructuredSelection ssel = (IStructuredSelection) sel;
              mb.setText( "Change "+ssel.size()+" file(s)" );
              mb.setMessage( "Do you want to write the "+ (make3D ? "3D" : "2D")+ " coordinates into the existing file? If no, new file(s) will be created." );
              int makenewfile = mb.open();
              if(makenewfile == SWT.CANCEL)
                  return;
              IFile[] filestosaveto=new IFile[ssel.size()];
              for(int i=0;i<ssel.toArray().length;i++){
                  if ( makenewfile == SWT.NO ){
                      SaveAsDialog dialog = new SaveAsDialog( new Shell() );
                      dialog.setOriginalFile( (IFile) ssel.toArray()[i] );
                      int saveasreturn = dialog.open();
                      if ( saveasreturn != SaveAsDialog.CANCEL ) {
                        IPath result = dialog.getResult();
                        if ( dialog.getResult().getFileExtension() == null )
                            result =
                                    result.addFileExtension( ((IFile) ssel.toArray()[i])
                                            .getFileExtension() );
                        filestosaveto[i]=((IFile) ssel.toArray()[i])
                        .getWorkspace()
                        .getRoot()
                        .getFile(
                                  result );
                      }
                  }
              }
              for(int i=0;i<ssel.toArray().length;i++){
                ICDKMolecule mol;
                try {
                    mol =
                            Activator.getDefault().getJavaCDKManager()
                                    .loadMolecule(
                                                   (IFile) ssel.toArray()[i], new NullProgressMonitor() );
                    if(make3D){
                        mol =
                            (ICDKMolecule) Activator.getDefault()
                                    .getJavaCDKManager()
                                    .generate3dCoordinates( mol ) ;                    
                    }else{
                      mol =
                              (ICDKMolecule) Activator.getDefault()
                                      .getJavaCDKManager()
                                      .generate2dCoordinates( mol );
                    }
                    if(withReset){
                      //we set the other coordinates to null, since when writing out, they might override
                      for(IAtom atom : mol.getAtomContainer().atoms()){
                          if(make3D)
                              atom.setPoint2d( null );
                          else
                              atom.setPoint3d( null );
                      }
                    }
                    if ( makenewfile == SWT.YES ) {
                        try {
                            Activator.getDefault().getJavaCDKManager()
                                    .saveMolecule(
                                                   mol,
                                                   (IFile) ssel.toArray()[i],
                                                   true);
                        } catch ( Exception e ) {
                            throw new RuntimeException( e.getMessage() );
                        }
                    } else if ( makenewfile == SWT.NO ){
                            try {
                                Activator
                                        .getDefault()
                                        .getJavaCDKManager()
                                        .saveMolecule(
                                                       mol,
                                                       filestosaveto[i],
                                                       true );
                            } catch ( Exception e ) {
                                throw new RuntimeException( e.getMessage() );
                            }
                    }
                } catch ( InvocationTargetException e ) {
                    if(e.getCause() instanceof NoSuchAtomTypeException){
                      mb =
                          new MessageBox( new Shell(), SWT.OK
                                                       | SWT.ICON_WARNING );
                      mb.setText( "Problems handling atom types in "+((IFile) ssel.toArray()[i]).getName() );
                      mb.setMessage( "We cannot handle this structure since it contains unknown atom types. We leave this file unchanged!" );
                      mb.open();
                    }
                } catch ( Exception e ) {
                    LogUtils.handleException( e, logger );
                }
              }
          }
      }
    }
}
