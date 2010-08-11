/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * Stefan Kuhn
 ******************************************************************************/
package net.bioclipse.cdk.ui.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.interfaces.IAtom;

/**
 * A handler class for a Generate 2D Coordinates menu item
 */
public class Create2dHandlerWithReset extends AbstractHandler {

    private static final Logger logger
                = Logger.getLogger( Create2dHandlerWithReset.class );
    public int answer;

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        ISelection sel = HandlerUtil.getCurrentSelection(event);
        if(sel instanceof IStructuredSelection && !((IStructuredSelection)sel).isEmpty()) {

            doCreation( HandlerUtil.getActiveShell(event),
                        (IStructuredSelection)sel,
                        true,
                        Coordiantes.Coord_2D);
        }
        return null;
    }

    private static int openMessageBox(Shell shell,String text,String message) {
        MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.CANCEL
                                                       | SWT.ICON_QUESTION );

        mb.setText(text);
        mb.setMessage(message);
        return mb.open();
    }
    /**
     * This method creates the coordinates and saves the file on the selection.
     *
     * @param withReset If true, the other set of coordinates is set to null. This should be used on mol files, since these can only hold one set (3d or 2d).
     * @param make true = 3d is generated, false = 2d is generated.
     */
    static void doCreation( final Shell shell,
                            final IStructuredSelection ssel,
                            final boolean withReset,
                            final Coordiantes make){
        boolean single = ssel.size()==1;
        String text = "Change "+ssel.size()+" file"+(single?"":"s");
        String message = "Do you want to write the "+
            (make==Coordiantes.Coord_3D ? "3D" : "2D") +
            " coordinates into the existing file? If no, new file" +
            (single?"":"s") + " will be created.";
        final int makenewfile = openMessageBox(shell, text, message);
        if(makenewfile == SWT.CANCEL)
            return;
        final IFile[] filestosaveto=new IFile[ssel.size()];
        for(int i=0;i<ssel.toArray().length;i++){
            if ( makenewfile == SWT.NO ){
                SaveAsDialog dialog = new SaveAsDialog( shell );
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
                }else{
                    return;
                }
            }
        }
        try {
            List<IMolecule> mols = new ArrayList<IMolecule>();
            for(int i=0;i<ssel.toArray().length;i++){
                mols.add(Activator.getDefault().getJavaCDKManager()
                        .loadMolecule((IFile) ssel.toArray()[i]));
            }
            if(make==Coordiantes.Coord_3D){
                //This try-catch is not working
                try{
                    Activator.getDefault()
                    .getJavaCDKManager()
                    .generate3dCoordinates( mols, new BioclipseUIJob<List<IMolecule>>() {

                        @Override
                        public void runInUI() {
                            List<IMolecule> newMols = getReturnValue();
                            if(newMols!=null)
                                handlePostProduction( withReset, make, makenewfile, ssel, filestosaveto, newMols );
                        }
                    });
                } catch ( Exception e ) {
                    if(e.getCause().getCause() instanceof NoSuchAtomTypeException){
                        MessageBox mb =
                            new MessageBox( shell, SWT.OK
                                    | SWT.ICON_WARNING );
                        mb.setText( "Problems handling atom types in "+((IFile) ssel.toArray()[Integer.parseInt( e.getMessage().split( " " )[e.getMessage().split( " " ).length-1])]).getName() );
                        mb.setMessage( "We cannot handle this structure since it contains unknown atom types. We recommend you leave this out from generation!" );
                        mb.open();
                    }else{
                        throw e;
                    }
                }
            }else{
                Activator.getDefault()
                .getJavaCDKManager()
                .generate2dCoordinates( mols, new BioclipseUIJob<List<IMolecule>>() {

                    @Override
                    public void runInUI() {
                        List<IMolecule> newMols = getReturnValue();
                        handlePostProduction( withReset, make, makenewfile, ssel, filestosaveto, newMols );
                    }
                });
            }
        } catch ( Exception e ) {
            LogUtils.handleException( e, logger, net.bioclipse.cdk.ui.Activator.PLUGIN_ID );
        }
    }

    private static void handlePostProduction(boolean withReset,
                                             Coordiantes make,
                                             int makenewfile,
                                             IStructuredSelection ssel,
                                             IFile[] filestosaveto,
                                             List<IMolecule> mols){
        List<IFile> files;
        switch (makenewfile) {
            case SWT.YES:
                files = new ArrayList<IFile>();
                for(Object o:ssel.toArray()) {
                    if(o instanceof IFile)
                        files.add((IFile)o);
                }
            break;
        case SWT.NO: files = Arrays.asList(filestosaveto); break;
        default:
            logger.error( "Creating coordinates dialog awnser was neither "+
                          " yes or no.");
            return;
        }

        for(int i=0;i<mols.size();i++){
            IMolecule mol = mols.get(i);
            IFile file = files.get(i);
            if( withReset ) {
                switch (make) {
                    case Coord_2D: resetCoordinates(mol, Coordiantes.Coord_3D);
                        break;
                    case Coord_3D: resetCoordinates(mol, Coordiantes.Coord_2D);
                        break;
                }
                saveMolecule(mol, file);
            } else
                saveMolecule(mol, file);
        }
    }

    /** Resets the coordinates of the molecule indicated by type.
     * If Coordinate2D is passed as type the 2D-coordinates will be reset.
     *
     * @param mol Molecule which coordinates will be reset
     * @param type indicates if the 2D or 3D coordinates should be reset
     */
    private static void resetCoordinates(IMolecule mol, Coordiantes type) {
        if(!(mol instanceof ICDKMolecule))
            logger.error("Can not resset coordinates of non cdk molecules");
        for(IAtom atom: ((ICDKMolecule)mol).getAtomContainer().atoms()) {
            switch(type) {
                case Coord_2D: atom.setPoint2d(null);break;
                case Coord_3D: atom.setPoint3d(null);break;
            }
          }
    }

    private static void saveMolecule(IMolecule mol,IFile file) {
        try {
            ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
            cdk.saveMolecule( mol, file, true);
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage() );
        }
    }
}

enum Coordiantes { Coord_2D,Coord_3D}
