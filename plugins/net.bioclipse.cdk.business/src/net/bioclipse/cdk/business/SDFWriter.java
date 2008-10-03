package net.bioclipse.cdk.business;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.DefaultChemObjectWriter;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.formats.SDFFormat;

/**
 * MDL SD file writer, which outputs all IMolecule.getProperties() as SD properties.
 *
 * @author egonw
 * @author ola
 */
public class SDFWriter extends DefaultChemObjectWriter {

    private Writer writer;
    
    public SDFWriter(Writer writer) {
    	this.writer=writer;
	}

    public void setWriter( Writer writer ) throws CDKException {
        this.writer = writer;
    }

    public void setWriter( OutputStream writer ) throws CDKException {
        this.writer = new OutputStreamWriter(writer);
    }

    public void write( IChemObject object ) throws CDKException {
        if (object instanceof IMoleculeSet) {
            writeMoleculeSet((IMoleculeSet)object);
        } else {
            throw new CDKException("Cannot writer anything other than IMoleculeSet.");
        }
    }

    private void writeMoleculeSet(IMoleculeSet set) throws CDKException {
        try {
        	
        	
            Iterator<IAtomContainer> molecules = set.molecules().iterator();
            while (molecules.hasNext()) {
            	IAtomContainer ac=molecules.next();
                IMolecule mol = null;
            	if (ac instanceof IMolecule) {
					mol = (IMolecule) ac;
				}else{
					mol=new Molecule(ac);
				}
                StringWriter sWriter = new StringWriter();
                MDLWriter mdlWriter = new MDLWriter(sWriter);
                mdlWriter.setSdFields(mol.getProperties());
                mdlWriter.write(mol);
                mdlWriter.close();
                this.writer.write(sWriter.toString());
                if (molecules.hasNext()) this.writer.write("$$$$\n");
                writer.flush();
            }
        } catch (IOException exception) {
            throw new CDKException(
                "Error while writing SD file: " + exception.getMessage(),
                exception
            );
        }
    }
    
    public boolean accepts( Class classObject ) {
        Class[] interfaces = classObject.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            if (IMoleculeSet.class.equals(interfaces[i])) return true;
        }
        return false;
    }

    public void close() throws IOException {
        this.writer.close();
    }

    public IResourceFormat getFormat() {
        return SDFFormat.getInstance();
    }
    
}
