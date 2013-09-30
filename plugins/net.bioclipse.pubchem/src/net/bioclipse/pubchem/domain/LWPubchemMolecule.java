/* *****************************************************************************
 * Copyright (c) 2010  Ola Spjuth <ospjuth@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.pubchem.domain;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.pubchem.Activator;

/**
 * 
 * @author ola
 *
 */
public class LWPubchemMolecule extends BioObject implements IMolecule{

    private Logger logger = Logger.getLogger(LWPubchemMolecule.class);

    private Integer CID;
    private String cml;
    private String smiles;
    private ImageDescriptor imageDescriptor;
    
    
    public LWPubchemMolecule(Integer CID) {
        this.CID=CID;
    }

    public Integer getCID() {
        return CID;
    }

    public void setCID( Integer cID ) {
        CID = cID;
    }

    public List<IMolecule> getConformers() {
        return null;
    }

    /**
     * Use cache if exists, otherwise download CML
     */
    public String toCML() throws BioclipseException {
        if (CID==null)
            throw new BioclipseException( "No CID available." );
        if (cml==null)
            try {
                cml=downloadCML();
            } catch ( Exception e ) {
                throw new BioclipseException( "Could not download CML: " + e.getMessage()  );
            }
        if (cml==null)
            throw new BioclipseException( "Could not download CML." );
        return cml;
    }

    /**
     * Use cache if exists, otherwise download SMILES
     */
    public String toSMILES() throws BioclipseException {
        if (CID==null)
            throw new BioclipseException( "No CID available." );
        if (smiles==null)
            try {
                cml=downloadSMILES();
            } catch ( Exception e ) {
                throw new BioclipseException( "Could not download CML: " + e.getMessage()  );
            }
        if (smiles==null)
            throw new BioclipseException( "Could not download CML." );
        return cml;
    }
    

    /**
     * Download SMILES from PubChem using Web service
     * @return SMILES string
     * @throws CoreException 
     * @throws BioclipseException 
     * @throws IOException 
     */
    private String downloadSMILES() throws IOException, BioclipseException, CoreException {
        IMolecule mol=Activator.getDefault().getJavaManager().download( CID );
        return mol.toSMILES();
    }

    /**
     * Download CML from PubChem using Web service
     * @return CMLstring
     * @throws CoreException 
     * @throws BioclipseException 
     * @throws IOException 
     */
    private String downloadCML() throws IOException, BioclipseException, CoreException {
        IMolecule mol=Activator.getDefault().getJavaManager().download( CID );
        return mol.toCML();
    }

    
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((CID == null) ? 0 : CID.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {

        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        LWPubchemMolecule other = (LWPubchemMolecule) obj;
        if ( CID == null ) {
            if ( other.CID != null )
                return false;
        } else if ( !CID.equals( other.CID ) )
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "PubChem CID: " + CID;
    }

    /**
     * We wish to provide an icon by the adapter pattern
     */
    @Override
    public Object getAdapter( Class adapter ) {

        if ( adapter.isAssignableFrom( ImageDescriptor.class ) || adapter
                             .equals( Image.class ) ) {
            if ( imageDescriptor == null )
                imageDescriptor = Activator
                                .getImageDescriptor( "icons/benzene.gif" );
            if ( adapter.equals( Image.class ) ) {
                return imageDescriptor.createImage();
            } else {
                return imageDescriptor;
            }
        }

        if (adapter == ICDKMolecule.class){
            try {
                return net.bioclipse.cdk.business.Activator
                    .getDefault().getJavaCDKManager().asCDKMolecule( this );
            } catch ( BioclipseException e ) {
                LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
            }
        }

        return super.getAdapter( adapter );
    }

}
