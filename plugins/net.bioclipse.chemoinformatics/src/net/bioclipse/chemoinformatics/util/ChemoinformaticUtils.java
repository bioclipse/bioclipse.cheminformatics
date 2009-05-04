/*****************************************************************************
 * Copyright (c) 2009  Stefan Kuhn
 *               2009  Egon Willighagen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.chemoinformatics.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.TestClasses;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;


/**
 * A class with some miscellaneous helper methods in the cheminf field. 
 * 
 * @author shk3
 * @author egonw
 */
@SuppressWarnings("serial")
@TestClasses("net.bioclipse.chemoinformatics.util.ChemoinformaticUtilsTest")
public class ChemoinformaticUtils {

    public final static List<String> SUPPORTED_CONTENT_TYPES =
        new ArrayList<String>() {{
           add("net.bioclipse.contenttypes.mdlMolFile2D");
           add("net.bioclipse.contenttypes.mdlMolFile3D");
           add("net.bioclipse.contenttypes.cml.singleMolecule2d");
           add("net.bioclipse.contenttypes.cml.singleMolecule3d");
           add("net.bioclipse.contenttypes.cml.singleMolecule5d");
        }};
    
    /**
     * Tells if a file is a molecule according to Bioclipse content types.
     * 
     * @param file The file to check
     * @return true=is any of the molecule content types, false= is not.
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is not local.</li>
     * <li> The workspace is not in sync with the corresponding location
     *       in the local file system.</li>
     * </ul>
     * @throws IOException if an error occurs while reading the contents 
     */
    public static boolean isMolecule(IFile file) throws CoreException, IOException{
        if(!file.exists())
            return false;
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        InputStream stream = file.getContents();
        IContentType contentType = contentTypeManager.findContentTypeFor(stream, file.getName());
        stream.close();
        if (contentType != null &&
            SUPPORTED_CONTENT_TYPES.contains(contentType.getId()))
            return true;
        else
            return false;
    }

}
