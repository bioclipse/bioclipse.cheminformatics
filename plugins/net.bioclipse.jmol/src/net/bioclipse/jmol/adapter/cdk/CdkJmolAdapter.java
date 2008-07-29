/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *	  Egon Willighagen
 *    Ola Spjuth
 *******************************************************************************/
package net.bioclipse.jmol.adapter.cdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jmol.api.JmolAdapter;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.CrystalGeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.ICrystal;
import org.openscience.cdk.interfaces.IPDBAtom;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.protein.data.PDBPolymer;
import org.openscience.cdk.protein.data.PDBStructure;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

/**
 * Provides an interface to CDK IO and CDK data classes. The
 * <code>openBufferedReader</code> uses the ReaderFactory to get an CDK Reader.
 * The <code>getAtomIterator</code> and other methods that act on a
 * <code>clientFile</code> accept <code>AtomContainer</code>s and 
 * <code>ChemFile</code>s.
 */
public class CdkJmolAdapter extends JmolAdapter {

    public final static String ATOM_SET_INDEX
        = "org.jmol.adapter.cdk.ATOM_SET_INDEX";

    private static final Logger logger = Logger.getLogger(CdkJmolAdapter.class);

    public CdkJmolAdapter() {
        super("CdkJmolAdapter");
    }

    /* **************************************************************
     * The file-related methods
     * **************************************************************/

    public Object openBufferedReader(String name,
                                     BufferedReader bufferedReader) {
        IChemFile chemFile = null;
        try {
            ISimpleChemObjectReader chemObjectReader = null;
            try {
                chemObjectReader = new ReaderFactory()
                        .createReader(bufferedReader);
            } catch (IOException ex) {
                return "Jmol: Error determining input format: " + ex;
            }
            if (chemObjectReader == null) {
                return "Jmol: unrecognized input format";
            }
            chemFile = (IChemFile) chemObjectReader
                    .read(new org.openscience.cdk.ChemFile());
        } catch (CDKException ex) {
            return "Error reading input:" + ex;
        }
        if (chemFile == null)
            return "unknown error reading file";
            AtomTypeFactory factory = AtomTypeFactory.getInstance(
                    "pdb_atomtypes.txt", chemFile.getBuilder());
            IAtomContainer atomContainer
                = (IAtomContainer)ChemFileManipulator
                  .getAllAtomContainers(chemFile);
            Iterator<IAtom> it=atomContainer.atoms();
            while(it.hasNext()) {
                IAtom atom = it.next();
                try {
                    if (atom instanceof IPDBAtom) {
                        // the PDBReader has the annoying feature to add the
                        // residue code
                        IPDBAtom pdbAtom = (IPDBAtom) atom;
                        String atName = pdbAtom.getAtomTypeName();
                        if (atName.indexOf(".") != -1) {
                            atName = atName.substring(atName.indexOf(".") + 1);
                        }
                        IAtomType type = factory.getAtomType(atName);
                        AtomTypeManipulator.configure(pdbAtom, type);
                    } else {
                        factory.configure(atom);
                    }
                } catch (CDKException exception) {
                    logger.error("Could not configure atom: " + atom);
                    logger.error("  because: " + exception.getMessage(),
                            exception);
                }
            }
            return chemFile;
    }

    public Object openBufferedReader(String name, String type,
            BufferedReader bufferedReader, Hashtable htParams) {
        
        return openBufferedReader(name, bufferedReader);
    }
    
    public String getFileTypeName(Object clientFile) {
        if (clientFile instanceof PDBPolymer) {
            return "pdb";
        }
        return "other";
    }

    public String getAtomSetCollectionName(Object clientFile) {
        logger.debug("Getting atom set collection name...");
        if (clientFile instanceof IChemObject) {
            Object title = ((IChemObject) clientFile)
                    .getProperty(CDKConstants.TITLE);
            if (title != null) {
                logger.debug("Setting model name to title");
                return title.toString();
            }
        }
        return null;
    }

    /* **************************************************************
     * The frame-related methods
     * **************************************************************/

    public int getAtomSetCount(Object clientFile) {
        logger.debug("Getting atom set count...");
        if (clientFile instanceof IAtomContainer) {
            logger.debug("Found a IAtomContainer... NOT good");
            return 1;
        } else if (clientFile instanceof IChemFile) {
            logger.debug("Found a IChemFile... good");
            logger.debug("Found #sets: "
                         + ChemFileManipulator.getAllChemModels(
                               (IChemFile)clientFile
                           ).size() );
            
            return ChemFileManipulator.getAllChemModels(
                (IChemFile)clientFile
            ).size();
        } else {
            logger.error("Don't know what type this clientFile is... ");
            return 0;
        }
    }

    public int getEstimatedAtomCount(Object clientFile) {
        logger.debug("Estimating atom count...");
        if (clientFile instanceof IAtomContainer)
            return ((IAtomContainer)clientFile).getAtomCount();
        if (clientFile instanceof IChemFile)
            return ChemFileManipulator.getAtomCount((IChemFile)clientFile);
        logger.error("Don't know what kind of clientFile this is: "
                     + clientFile.getClass().getName());
        return 0;
    }

    /*
     this needs to be handled through the StructureIterator

     String[] getPdbStructureRecords(Object clientFile) {
     ChemFile chemFile = (ChemFile)clientFile;
     ChemSequence chemSequence = chemFile.getChemSequence(0);
     ChemModel chemModel = chemSequence.getChemModel(0);
     Vector structureVector =
     (Vector)chemModel.getProperty("pdb.structure.records");
     if (structureVector == null)
     return null;
     String[] t = new String[structureVector.size()];
     structureVector.copyInto(t);
     return t;
     }
     */

    public float[] getNotionalUnitcell(Object clientFile) {
        if (clientFile instanceof ICrystal) {
            ICrystal crystal = (ICrystal) clientFile;
            double[] notional = CrystalGeometryTools.cartesianToNotional(
                    crystal.getA(), crystal.getB(), crystal.getC());
            float[] fNotional = new float[6];
            for (int i = 0; i < 6; i++) {
                fNotional[i] = (float) notional[i];
            }
            return fNotional;
        } // else: no crystal thus no unit cell info
        return null;
    }

    public String getClientAtomStringProperty(Object clientAtom,
            String propertyName) {
        Object value = ((IAtom) clientAtom).getProperty(propertyName);
        return value == null ? null : "" + value;
    }

    public JmolAdapter.AtomIterator getAtomIterator(Object clientFile) {
        logger.debug("Jmol requested an AtomIterator...");
        return new AtomIterator((IChemFile) clientFile);
    }

    public JmolAdapter.BondIterator getBondIterator(Object clientFile) {
        logger.debug("Jmol requested an BondIterator...");
        return new BondIterator((IChemFile) clientFile);
    }

    /* ***************************************************************
     * the frame iterators
     * **************************************************************/
    class AtomIterator extends JmolAdapter.AtomIterator {
        
        IChemFile chemFile;
        int atomCount, iatom;
        int modelCount, imodel;
        int icontainer;
        List<IChemModel> models;
        List<IAtomContainer> containers;

        IAtom atom;

        AtomIterator(IChemFile chemFile) {
            this.chemFile = chemFile;
            models = ChemFileManipulator.getAllChemModels(chemFile);
            modelCount = models.size();
            if (modelCount > 0) {
                containers = ChemModelManipulator.getAllAtomContainers(
                    (IChemModel)models.iterator().next()
                );
                if (containers.iterator().hasNext()) 
                    atomCount
                        = ((IAtomContainer)containers.get(0)).getAtomCount();
            }
            iatom = 0;
            imodel = 0;
            icontainer = 0;
        }

        public boolean hasNext() {
            if (modelCount == 0) return false;
            if (iatom == atomCount) {
                icontainer++;
                if (icontainer == containers.size()) {
                    imodel++;
                    if (imodel == modelCount) return false;
                    containers 
                        = ChemModelManipulator.getAllAtomContainers(
                              (IChemModel)models.get(imodel)
                          );
                    if (containers.size() == 0) return false;
                    icontainer = 0;
                }
                //FIXME: catch ArrayIndexOutOfBoundsException for next line
                try{
                    atomCount
                        = ((IAtomContainer)containers.get(icontainer))
                          .getAtomCount();
                } catch (IndexOutOfBoundsException e){
                    return false;
                }
                iatom = 0;
                return hasNext();
            }
            atom
                = ((IAtomContainer)containers.get(icontainer)).getAtom(iatom++);
//                  logger.debug("unique ID: " + getUniqueID());
//                  logger.debug("element symbol: " + getElementSymbol());
//                  logger.debug("element number: " + getElementNumber());
//                  logger.debug("atom name: " + getAtomName());
//                  logger.debug("formal charge: " + getFormalCharge());
//                  logger.debug("partial charge: " + getPartialCharge());
//                  logger.debug("x: " + getX());
//                  logger.debug("y: " + getY());
//                  logger.debug("z: " + getZ());
//                  logger.debug("vx: " + getVectorX());
//                  logger.debug("vy: " + getVectorY());
//                  logger.debug("vz: " + getVectorZ());
//                  logger.debug("b factor: " + getBfactor());
//                  logger.debug("occupacy: " + getOccupancy());
//                  logger.debug("is hetero: " + getIsHetero());
//                  logger.debug("atom serial: " + getAtomSerial());
//                  logger.debug("chain id: " + getChainID());
//                  logger.debug("alternate location id: "
//                               + getAlternateLocationID());
//                  logger.debug("group3: " + getGroup3());
//                  logger.debug("sequence number: " + getSequenceNumber());
//                  logger.debug("insertion code: " + getInsertionCode());
            return true;
        }

        public int getAtomSerial() {
            return iatom;
        }

        public Object getUniqueID() {
            return atom;
        }

        public int getElementNumber() {
            int atomicNum = atom.getAtomicNumber();
            if (atomicNum == 0)
                atomicNum = -1;
            return atomicNum;
        }

        public String getElementSymbol() {
            return atom.getSymbol();
        }

        public float getX() {
            return atom.getPoint3d() == null ? 0.0f
                                             : (float)atom.getPoint3d().x;
        }

        public float getY() {
            return atom.getPoint3d() == null ? 0.0f
                                             : (float)atom.getPoint3d().y;
        }

        public float getZ() {
            return atom.getPoint3d() == null ? 0.0f
                                             : (float)atom.getPoint3d().z;
        }

        public String getPdbAtomRecord() {
            return (String) atom.getProperty("pdb.record");
        }

        public String getAtomName() {
            if (atom instanceof IPDBAtom) {
                IPDBAtom pdbAtom = (IPDBAtom) atom;
                return pdbAtom.getName();
            }
            return super.getAtomName();
        }

        public int getAtomSetIndex() {
            return imodel;
        }

        public char getChainID() {
            if (atom instanceof IPDBAtom) {
                IPDBAtom pdbAtom = (IPDBAtom) atom;
                if (pdbAtom.getChainID().length() > 0)
                    return pdbAtom.getChainID().charAt(0);
            }
            return super.getChainID();
        }

        public String getGroup3() {
            if (atom instanceof IPDBAtom) {
                IPDBAtom pdbAtom = (IPDBAtom) atom;
                if ( pdbAtom.getResName() != null
                     && pdbAtom.getResName().length() > 0 )
                    return pdbAtom.getResName();
            }
            return super.getGroup3();
        }

        public int getSequenceNumber() {
            if (atom instanceof IPDBAtom) {
                IPDBAtom pdbAtom = (IPDBAtom) atom;
                if (pdbAtom.getResSeq().length() > 0)
                    return Integer.parseInt(pdbAtom.getResSeq());
            }
            return super.getSequenceNumber();
        }

        public char getInsertionCode() {
            if (atom instanceof IPDBAtom) {
                IPDBAtom pdbAtom = (IPDBAtom) atom;
                if (pdbAtom.getICode().length() > 0)
                    return pdbAtom.getICode().charAt(0);
            }
            return super.getInsertionCode();
        }

        public Object getClientAtomReference() {
            return atom;
        }
    }

    class BondIterator extends JmolAdapter.BondIterator {

        IChemFile chemFile;
        int modelCount, imodel;
        int bondCount, ibond;
        int containerCount, icontainer;
        IBond bond;
        List<IChemModel> models;
        List<IAtomContainer> containers;

        BondIterator(IChemFile chemFile) {
            this.chemFile = chemFile;
            bondCount = 0;
            models = ChemFileManipulator.getAllChemModels(chemFile);
            modelCount = models.size();
            if (modelCount > 0) {
                containers
                    = ChemModelManipulator.getAllAtomContainers(
                          (IChemModel)models.get(0)
                      );
                if (containerCount > 0) 
                    bondCount
                        = ((IAtomContainer)containers.get(0)).getBondCount();
            }
            ibond = 0;
            imodel = 0;
            icontainer = 0;
        }

        public boolean hasNext() {
            if (ibond == bondCount) {
                icontainer++;
                if (icontainer >= containerCount) {
                    imodel++;
                    if (imodel >= models.size()) return false;
                    containers
                        = ChemModelManipulator.getAllAtomContainers(
                              (IChemModel)models.get(imodel)
                          );
                    containerCount = containers.size();
                    if (containerCount == 0) return false;
                    icontainer = 0;
                }
                bondCount
                    = ((IAtomContainer)containers.get(icontainer))
                      .getBondCount();
                ibond = 0;
                return hasNext();
            }
            // FIXME: skip all bonds which are messy, but this should really
            // be examined more closely
            bond
                = ((IAtomContainer)containers.get(icontainer)).getBond(ibond++);
            boolean isOK = bond.getAtomCount() == 2
                && (bond.getAtom(0) != null && bond.getAtom(1) != null);
            if (!isOK) {
                logger.error("Something wrong with the bonds CDK created!");
                return hasNext();
            }
            // end of work-around
            return true;
        }

        public Object getAtomUniqueID1() {
            return (bond.getAtomCount() == 2) ? bond.getAtom(0) : null;
        }

        public Object getAtomUniqueID2() {
            return (bond.getAtomCount() == 2) ? bond.getAtom(1) : null;
        }

        public int getEncodedOrder() {
            if (bond.getOrder().equals(Order.SINGLE))
                return 1;
            else if (bond.getOrder().equals(Order.DOUBLE))
                return 2;
            else if (bond.getOrder().equals(Order.QUADRUPLE))
                return 4;
            else
                logger.error("Unknown bond order: " + bond.getOrder());
            return -1;
        }
    }

    public JmolAdapter.StructureIterator getStructureIterator(
        Object clientFile) {
        
        logger.debug("Jmol requested a stucture iterator for a "
                + clientFile.getClass().getName());
        if (clientFile instanceof IChemFile)
            return new StructureIterator((IChemFile)clientFile);
        return null;
    }

    public class StructureIterator extends JmolAdapter.StructureIterator {
        int structureCount;
        Iterator structures;
        PDBStructure structure;

        int istructure;

        StructureIterator(IChemFile chemFile) {
            // OK, the structures are only defined in the first PDBPolymer
            this.structures = new ArrayList(0).iterator();
            if (chemFile == null) return;
            List<IChemModel> models
                = ChemFileManipulator.getAllChemModels(chemFile);
            if (models.size() == 0) return;
            List<IAtomContainer> containers
              = ChemModelManipulator.getAllAtomContainers(
                    (IChemModel)models.get(0)
                );
            if (containers.size() == 0) return;
            if (!(containers.get(0) instanceof PDBPolymer)) return;
            PDBPolymer polymer = (PDBPolymer)containers.get(0);
            Collection pdbStructures = polymer.getStructures();
            if (pdbStructures == null) return;
            this.structures = pdbStructures.iterator();
        }

        @Override
        public boolean hasNext() {
            if (!structures.hasNext())
                return false;
            structure = (PDBStructure) structures.next();
            return true;
        }

        @Override
        public String getStructureType() {
            return structure.getStructureType();
        }

        @Override
        public char getStartChainID() {
            return structure.getStartChainID();
        }

        @Override
        public int getStartSequenceNumber() {
            return structure.getStartSequenceNumber();
        }

        @Override
        public char getStartInsertionCode() {
            return structure.getStartInsertionCode();
        }

        @Override
        public char getEndChainID() {
            return structure.getEndChainID();
        }

        @Override
        public int getEndSequenceNumber() {
            return structure.getEndSequenceNumber();
        }

        @Override
        public char getEndInsertionCode() {
            return structure.getEndInsertionCode();
        }

        @Override
        public int getModelIndex() {
            // FIXME: do something good here!!!
            return 1;
        }
    }
}
