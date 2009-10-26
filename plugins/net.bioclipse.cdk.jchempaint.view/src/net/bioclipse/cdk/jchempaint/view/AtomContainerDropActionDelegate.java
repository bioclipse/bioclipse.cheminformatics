/*******************************************************************************
 * Copyright (c) 2009 Arvid Berg <goglepox@users.sourceforge.net>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Arvid Berg
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.view;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.part.IDropActionDelegate;


public class AtomContainerDropActionDelegate implements IDropActionDelegate {

    private static Pattern ending = Pattern.compile( ".*\\(([0-9]+)\\)$" );

    public boolean run( Object source, Object target ) {

        if (target instanceof IContainer) {
            AtomContainerTransfer transfer = AtomContainerTransfer.getInstance();
            //IAtomContainer atomContainer = transfer.fromByteArray((byte[])source);
            IContainer parent = (IContainer)target;
            writeAsCML( parent, (byte[]) source );
            return true;
         }
         //drop was not successful so return false
         return false;
      }

    //TODO move this to utility method in core
    private IPath retriveCandidate(IPath path) {
        IPath candidate = path.removeFileExtension();
        String lastSegment = candidate.lastSegment();
        Matcher matcher =  ending.matcher( lastSegment );
        if(matcher.matches()) {
            int val = Integer.valueOf( matcher.group(1) );
            lastSegment = lastSegment.replaceAll( "\\(([0-9]+)\\)$", String
                                                  .format( "(%d)",val+1 ));

        }else {
            lastSegment = lastSegment+"(1)";
        }
         return candidate.removeLastSegments( 1 )
                                 .append( lastSegment )
                                 .addFileExtension( path.getFileExtension() );

    }

    private void writeAsCML(IContainer parent, byte[] ac) {
        try {

            IFile file = parent.getFile(new Path("Moleucle.cml"));
            for(int i=0;i<100 && file.exists();i++){
                IPath path = retriveCandidate( file.getLocation().makeRelativeTo( parent.getLocation() ));
                file = parent.getFile( path );
            }

            ByteArrayInputStream in = createFileContents(ac);
            if (file.exists()) {
                file.setContents(in, IResource.NONE, null);
            } else {
                file.create(in, IResource.NONE, null);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private ByteArrayInputStream createFileContents(byte[] ac) {
        return new ByteArrayInputStream(ac);
     }
}
