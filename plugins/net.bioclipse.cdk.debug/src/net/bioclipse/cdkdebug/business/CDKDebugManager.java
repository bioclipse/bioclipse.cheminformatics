/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 *     Jonathan Alvarsson <jonalv@user.sf.net> 2009-01-15 Corrected Whitespaces 
 *                                             tabs and scripts seemed to have
 *                                             wrecked havoc...
 ******************************************************************************/
package net.bioclipse.cdkdebug.business;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.scripting.ui.views.JsConsoleView;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.atomtype.mapper.AtomTypeMapper;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.WriterFactory;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.setting.IOSetting;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class CDKDebugManager implements IBioclipseManager {

    private static final Logger logger = Logger.getLogger(CDKManager.class);
    private static final CDKManager cdk = new CDKManager();
    private static final WriterFactory writerFactory = new WriterFactory();
    private static final ReaderFactory readerFactory = new ReaderFactory();

    private static AtomTypeFactory factory;
    
    static {
    	try {
    	URL owlURL = Platform.getBundle("org.openscience.cdk.io").getResource("/org/openscience/cdk/dict/data/sybyl-atom-types.owl");
        InputStream iStream = owlURL.openStream();
//            = org.openscience.cdk.atomtype.Activator.class.getResourceAsStream(
//                "/org/openscience/cdk/dict/data/sybyl-atom-types.owl");
        factory = AtomTypeFactory.getInstance( iStream, "owl",
                                               SilentChemObjectBuilder.getInstance()
        );
    	} catch (IOException e) {
    		logger.error("Could not get sybyl-atom-types.owl file",e);
    	} 
    }

    
    public String diff(ICDKMolecule mol, ICDKMolecule mol2) {
        return AtomContainerDiff.diff(
            mol.getAtomContainer(), mol2.getAtomContainer()
        ); 
    }
    
    public String debug(ICDKMolecule mol) {
        return mol.getAtomContainer().toString();
    }
    
    public String getManagerName() {
        return "cdx";
    }
   
    public String perceiveSybylAtomTypes(IMolecule mol)
                        throws InvocationTargetException {
        
        ICDKMolecule cdkmol;
        
        try {
            cdkmol = cdk.asCDKMolecule(mol);
        } 
        catch (BioclipseException e) {
            System.out.println("Error converting cdk10 to cdk");
            e.printStackTrace();
            throw new InvocationTargetException(e);
        }
        
        IAtomContainer ac = cdkmol.getAtomContainer();
        CDKAtomTypeMatcher cdkMatcher 
            = CDKAtomTypeMatcher.getInstance(ac.getBuilder());
        AtomTypeMapper mapper 
            = AtomTypeMapper.getInstance(
                 "org/openscience/cdk/dict/data/cdk-sybyl-mappings.owl" );

        IAtomType[] sybylTypes = new IAtomType[ac.getAtomCount()];
        
        int atomCounter = 0;
        int a=0;
        for (IAtom atom : ac.atoms()) {
            IAtomType type;
            try {
                type = cdkMatcher.findMatchingAtomType(ac, atom);
            } 
            catch (CDKException e) {
                type = null;
            }
            if (type==null) {
//                logger.debug("AT null for atom: " + atom);
                type = atom.getBuilder().newInstance(
                	IAtomType.class, atom.getSymbol()
                );
                type.setAtomTypeName("X");
            }
            AtomTypeManipulator.configure(atom, type);
            a++;
        }
        try {
            CDKHueckelAromaticityDetector.detectAromaticity(ac);
//            System.out.println("Arom: " 
//                + CDKHueckelAromaticityDetector.detectAromaticity(ac) );
		    } 
        catch (CDKException e) {
			    logger.debug("Failed to perceive aromaticity: " + e.getMessage());
		    }
        for (IAtom atom : ac.atoms()) {
            String mappedType = mapper.mapAtomType(atom.getAtomTypeName());
            if ("C.2".equals(mappedType)
                    && atom.getFlag(CDKConstants.ISAROMATIC)) {
                mappedType = "C.ar";
            } 
            else if ("N.pl3".equals(mappedType)
                    && atom.getFlag(CDKConstants.ISAROMATIC)) {
                mappedType = "N.ar";
            }
            try {
                sybylTypes[atomCounter] = factory.getAtomType(mappedType);
		        } 
            catch (NoSuchAtomTypeException e) {
                // yes, setting null's here is important
                sybylTypes[atomCounter] = null; 
			      }
            atomCounter++;
        }
        StringBuffer result = new StringBuffer();
        // now that full perception is finished, we can set atom type names:
        for (int i = 0; i < sybylTypes.length; i++) {
            if (sybylTypes[i] != null) {
                ac.getAtom(i).setAtomTypeName(sybylTypes[i].getAtomTypeName());
            } 
            else {
                ac.getAtom(i).setAtomTypeName("X");
            }
            
            result.append(i).append(':').append(ac.getAtom(i).getAtomTypeName())
                  /*.append("\n")*/;

        }
        return result.toString();
    }
    
    public String perceiveCDKAtomTypes(IMolecule mol)
                throws InvocationTargetException {
        
        ICDKMolecule cdkmol;
        
        try {
            cdkmol = cdk.asCDKMolecule(mol);
        } 
        catch ( BioclipseException e ) {
            e.printStackTrace();
            throw new InvocationTargetException(
                          e, "Error while creating a ICDKMolecule" );
        }
        
        IAtomContainer ac = cdkmol.getAtomContainer();
        CDKAtomTypeMatcher cdkMatcher 
            = CDKAtomTypeMatcher.getInstance(ac.getBuilder());
        
        StringBuffer result = new StringBuffer();
        int i = 1;
        for (IAtom atom : ac.atoms()) {
            IAtomType type = null;
            try {
                type = cdkMatcher.findMatchingAtomType(ac, atom);
            } 
            catch ( CDKException e ) {}
            result.append(i).append(':').append(
                type != null ? type.getAtomTypeName() : "null"
            ).append('\n'); // FIXME: should use NEWLINE here
            i++;
        }
        return result.toString();
    }

    public String listWriterOptions(IChemFormat format) {
        if (format.getWriterClassName() == null)
            return "No writer avaiable for this format";

        IChemObjectWriter writer = writerFactory.createWriter(format);
        if (writer == null)
            return "Cannot instantiate writer: " + format.getWriterClassName();

        IOSetting[] settings = writer.getIOSettings();
        if (settings == null || settings.length == 0)
            return "The writer does not have options";
        
        StringBuffer overview = new StringBuffer();
        for (String _ : new String[] {
            format.getFormatName(), JsConsoleView.NEWLINE })
            overview.append(_);
        for (IOSetting setting : settings) {
            for (String _ : new String[] {
                "[", setting.getName(), "]",             JsConsoleView.NEWLINE,
                setting.getQuestion(),                   JsConsoleView.NEWLINE,
                "Default value: ", 
                setting.getDefaultSetting(),             JsConsoleView.NEWLINE,
                "Value        : ", setting.getSetting(), JsConsoleView.NEWLINE})
                overview.append(_);
        }
        if (overview.length() == 0) // there are no options
            return "The reader does not have options";

        return overview.toString();
    }

    public String listReaderOptions(IChemFormat format) {
        if (format.getReaderClassName() == null)
            return "No reader avaiable for this format";

        IChemObjectReader reader = readerFactory.createReader(format);
        if (reader == null)
            return "Cannot instantiate writer: " + format.getReaderClassName();

        IOSetting[] settings = reader.getIOSettings();
        if (settings == null || settings.length == 0)
            return "The reader does not have options";

        StringBuffer overview = new StringBuffer();
        for (String _ : new String[] {
            format.getFormatName(), JsConsoleView.NEWLINE })
            overview.append(_);
        for (IOSetting setting : settings) {
            for (String _ : new String[] {
                "[", setting.getName(), "]",             JsConsoleView.NEWLINE,
                setting.getQuestion(),                   JsConsoleView.NEWLINE,
                "Default value: ", 
                setting.getDefaultSetting(),             JsConsoleView.NEWLINE,
                "Value        : ", setting.getSetting(), JsConsoleView.NEWLINE})
                overview.append(_);
        }

        return overview.toString();
    }
}
