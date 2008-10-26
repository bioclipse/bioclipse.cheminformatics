/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     Egon Willighagen
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.util.Properties;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.PDBReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.cml.MDMoleculeConvention;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormatMatcher;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.MDLV2000Format;
import org.openscience.cdk.io.formats.MDLV3000Format;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.nonotify.NNChemFile;

public class CDKManagerHelper {

    /**
     * Register all formats that we support for reading in Bioclipse.
     * 
     * @param fac
     */
    public static void registerFormats(ReaderFactory fac) {
        IResourceFormat[] supportedFormats = {
            SDFFormat.getInstance(),
            CMLFormat.getInstance(),
            MDLV2000Format.getInstance(),
            MDLV3000Format.getInstance()
        };
        for (IResourceFormat format : supportedFormats) {
            if (!fac.getFormats().contains(format)) fac.registerFormat((IChemFormatMatcher)format);
        }
    }
    
    public static void customizeReading(ISimpleChemObjectReader reader, IChemFile chemFile) {
        System.out.println("customingIO, reader found: " + reader.getClass().getName());
        System.out.println("Found # IO settings: " + reader.getIOSettings().length);
        if (reader instanceof PDBReader) {
            chemFile = new NNChemFile();

            Properties customSettings = new Properties();
            customSettings.setProperty("DeduceBonding", "false");

            PropertiesListener listener = new PropertiesListener(customSettings);
            reader.addChemObjectIOListener(listener);
        }

        if (reader instanceof CMLReader) {
            ((CMLReader)reader).registerConvention("md:mdMolecule", new MDMoleculeConvention(new ChemFile()));
            System.out.println("****** CmlReader, registered MDMoleculeConvention");

        }

    }


}
