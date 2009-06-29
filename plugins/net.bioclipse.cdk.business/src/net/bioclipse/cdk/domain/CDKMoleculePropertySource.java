/*******************************************************************************
 * Copyright (c) 2009 Egon Willighagen <egonw@users.sf.net>
 *               2009 Arvid Berg <goglepox@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMoleculeUtils.MolProperty;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.domain.props.BioObjectPropertySource;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.IInChIManager;
import net.bioclipse.inchi.business.InChIManager;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IChemObject;


public class CDKMoleculePropertySource extends BioObjectPropertySource {

    protected static Map<ICDKMolecule, BioclipseJob> inchiJobs 
        = new HashMap<ICDKMolecule, BioclipseJob>();
    protected static Map<ICDKMolecule, BioclipseJob> smilesJobs
        = new HashMap<ICDKMolecule, BioclipseJob>();
    
    protected static final String PROPERTY_HAS2D = "Has 2D Coords";
    protected static final String PROPERTY_HAS3D = "Has 3D Coords";
    protected static final String PROPERTY_FORMAT = "Molecular Format";
    protected static final String PROPERTY_FORMULA = "Molecular Formula";
    protected static final String PROPERTY_MASS = "Molecular Mass";
    protected static final String PROPERTY_SMILES = "SMILES";
    protected static final String PROPERTY_INCHI = "InChI";
    protected static final String PROPERTY_INCHIKEY = "InChIKey";
    
    private static final Logger logger 
        = Logger.getLogger( CDKMoleculePropertySource.class );

    private final Object cdkPropertiesTable[][] =
    {
        { PROPERTY_HAS2D,
            new TextPropertyDescriptor(PROPERTY_HAS2D,PROPERTY_HAS2D)},
        { PROPERTY_HAS3D,
            new TextPropertyDescriptor(PROPERTY_HAS3D,PROPERTY_HAS3D)},
        { PROPERTY_FORMAT,
            new TextPropertyDescriptor(PROPERTY_FORMAT,PROPERTY_FORMAT)},
        { PROPERTY_FORMULA,
            new TextPropertyDescriptor(PROPERTY_FORMULA,PROPERTY_FORMULA)},
        { PROPERTY_MASS,
            new TextPropertyDescriptor(PROPERTY_MASS,PROPERTY_MASS)},
        { PROPERTY_SMILES,
            new TextPropertyDescriptor(PROPERTY_SMILES,PROPERTY_SMILES)},
        { PROPERTY_INCHI,
            new TextPropertyDescriptor(PROPERTY_INCHI,PROPERTY_INCHI)},
        { PROPERTY_INCHIKEY,
            new TextPropertyDescriptor(PROPERTY_INCHIKEY,PROPERTY_INCHIKEY)}
    };

    private CDKMolecule cdkMol;
    private ArrayList<IPropertyDescriptor> cdkProperties;

    private HashMap<String, Object> cdkValueMap;

    public CDKMoleculePropertySource(CDKMolecule item) {
        super(item);
        cdkMol = item;
        
        cdkProperties = setupProperties(item.getAtomContainer());
        cdkValueMap   = getPropertyValues(item);
        
        if ( item.getAtomContainer().getAtomCount() > 0 ) {
            createPropertiesJobs(item);
        }
        else {
           cdkValueMap.put( PROPERTY_INCHI,    "Failed to calculate" );
           cdkValueMap.put( PROPERTY_INCHIKEY, "Failed to calculate" );
           cdkValueMap.put( PROPERTY_SMILES,   "Failed to calculate" );
        }
    }

    /**
     * 
     */
    private void createPropertiesJobs(final CDKMolecule item) {

        final ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        final IInChIManager inchi = net.bioclipse.inchi.business.Activator
                                       .getDefault().getJavaInChIManager();
        final ICDKMolecule inchiClone;
        final ICDKMolecule smilesClone;
        try {
            inchiClone  = cdk.clone( item );
            smilesClone = cdk.clone( item );
        }
        catch ( BioclipseException e ) {
            throw new RuntimeException(e);
        }
        BioclipseJob inchiJobToBeCancelled  = inchiJobs.remove(  item );
        BioclipseJob smilesJobToBeCancelled = smilesJobs.remove( item );
        
        if ( inchiJobToBeCancelled != null ) {
            inchiJobToBeCancelled.cancel();
        }
        if ( smilesJobToBeCancelled != null ) {
            smilesJobToBeCancelled.cancel();
        }
        if (item.getProperty( PROPERTY_INCHI, Property.USE_CACHED ) == null) {
            
            Job j = new Job("Calculating inchi for properties view") {
                @Override
                protected IStatus run( IProgressMonitor monitor ) {
                    try {
                        item.setProperty( MolProperty.InChI.name(), 
                                          inchi.generate( inchiClone ) );
                    }
                    catch ( Exception e ) {
                        LogUtils.debugTrace( logger, e );
                        item.setProperty( MolProperty.InChI.name(), 
                                          InChI.FAILED_TO_CALCULATE );
                    }
                    return Status.OK_STATUS;
                }
            };   
            j.addJobChangeListener( new JobChangeAdapter() {
                @Override
                public void done( IJobChangeEvent event ) {
                    Display.getDefault().asyncExec( new Runnable() {
    
                        public void run() {
                            PropertySheet p 
                                = (PropertySheet) 
                                  PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getActivePage()
                                            .findView(
                                      "org.eclipse.ui.views.PropertySheet" );
                            if(p != null) {
                                PropertySheetPage pp
                                = (PropertySheetPage) p.getCurrentPage();

                                pp.refresh();
                            }
                        }
                    });
                }
            });
            j.schedule();
        }
        
        if (item.getProperty( PROPERTY_SMILES, Property.USE_CACHED ) == null ) {
            
            Job j = new Job("Calculating smiles for properties view") {
                @Override
                protected IStatus run( IProgressMonitor monitor ) {
                    try {
                        String s = cdk.calculateSMILES( smilesClone );
                        item.setProperty( MolProperty.SMILES.name(), 
                                          s );
                    }
                    catch ( Exception e ) {
                        LogUtils.debugTrace( logger, e );
                        item.setProperty( MolProperty.SMILES.name(), 
                                          "Failed to calculate" );
                    }
                    return Status.OK_STATUS;
                }
            };   
            j.addJobChangeListener( new JobChangeAdapter() {
                @Override
                public void done( IJobChangeEvent event ) {
                    Display.getDefault().asyncExec( new Runnable() {
    
                        public void run() {
                            PropertySheet p 
                                = (PropertySheet) 
                                  PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getActivePage()
                                            .findView(
                                      "org.eclipse.ui.views.PropertySheet" );
                            if(p != null) {
                                //The page might be a TabbedPropertySheetPage
                                //in the future but we ignore it for now
                                if ( p.getCurrentPage() 
                                        instanceof 
                                     PropertySheetPage ) {
                                    
                                    PropertySheetPage pp
                                    = (PropertySheetPage) p.getCurrentPage();

                                    pp.refresh();
                                }
                            }
                        }
                    });
                }
            });
            j.schedule();
        }
    }

    /**
     * @param item
     */
    private HashMap<String, Object> getPropertyValues(CDKMolecule item) {
        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(
            PROPERTY_HAS2D,
            GeometryTools.has2DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        valueMap.put(
            PROPERTY_HAS3D,
            GeometryTools.has3DCoordinates(item.getAtomContainer()) ?
                "yes" : "no"
        );
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        valueMap.put(
            PROPERTY_FORMULA, cdk.molecularFormula(item)
        );
        try {
            valueMap.put(
                PROPERTY_MASS, cdk.calculateMass(item)
            );
        } catch (BioclipseException e) {
            valueMap.put(PROPERTY_MASS, "N/A");
        }
        IResource resource = item.getResource();
        if (resource instanceof IFile) {
            IFile fileRes = (IFile)resource;
            try {
                valueMap.put(
                    PROPERTY_FORMAT, fileRes.getContentDescription().getContentType().getName()
                );
            } catch (CoreException e) {
                valueMap.put(PROPERTY_FORMAT, "error");
            }
        } else {
            valueMap.put(PROPERTY_FORMAT, "N/A");
        }

        String smiles = null;
        try {
            smiles = cdkMol.toSMILES();
        } catch ( BioclipseException e ) {
            e.printStackTrace();
        }
        valueMap.put(
            PROPERTY_SMILES,
            (smiles == null) ? "Calculating..." : smiles
        );

        String inchi = null;
        try {
            inchi = cdkMol.getInChI(Property.USE_CACHED);
        } catch ( BioclipseException e ) {
            e.printStackTrace();
        }
        valueMap.put(
            PROPERTY_INCHI,
            (inchi == null || inchi.length() == 0) ? "Calculating..." : inchi
        );
        String inchikey = null;
        try {
            inchikey = cdkMol.getInChIKey(Property.USE_CACHED);
        } catch ( BioclipseException e ) {
            e.printStackTrace();
        }
        valueMap.put(
            PROPERTY_INCHIKEY,
            (inchikey == null || inchikey.length() == 0) ? "Calculating..." 
                                                         : inchikey
        );

        // IChemObject.getProperties()
        Map<Object,Object> objectProps = item.getAtomContainer().getProperties();
        for (Object propKey : objectProps.keySet()) {
            String label = ""+propKey;
            valueMap.put(label, ""+objectProps.get(propKey));
        }
        return valueMap;
    }

    private ArrayList<IPropertyDescriptor> setupProperties(IChemObject object) {
        ArrayList<IPropertyDescriptor> cdkProperties =
            new ArrayList<IPropertyDescriptor>();
        // default properties
        for (int i=0;i<cdkPropertiesTable.length;i++) {
            PropertyDescriptor descriptor;
            descriptor = (PropertyDescriptor)cdkPropertiesTable[i][1];
            descriptor.setCategory("General");
            cdkProperties.add(descriptor);
        }
        // IChemObject.getProperties()
        Map<Object,Object> objectProps = object.getProperties();
        for (Object propKey : objectProps.keySet()) {
            PropertyDescriptor descriptor;
            String label = ""+propKey;
            descriptor = new TextPropertyDescriptor(label,label);
            descriptor.setCategory("Molecular Properties");
            cdkProperties.add(descriptor);
        }
        return cdkProperties;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        // Create the property vector.

        IPropertyDescriptor[] propertyDescriptors =
            new IPropertyDescriptor[cdkProperties.size()];
        for (int i=0; i< cdkProperties.size();i++){
            propertyDescriptors[i]=(IPropertyDescriptor) cdkProperties.get(i);
        }

        // Return it.
        return propertyDescriptors;
    }

    public Object getPropertyValue(Object id) {
        if (cdkValueMap.containsKey(id))
            return cdkValueMap.get(id);

        return super.getPropertyValue(id);
    }

    public ArrayList<IPropertyDescriptor> getProperties() {
        return cdkProperties;
    }

    public void setProperties(ArrayList<IPropertyDescriptor> properties) {
        this.cdkProperties = properties;
    }

    public HashMap<String, Object> getValueMap() {
        return cdkValueMap;
    }

    public void setValueMap(HashMap<String, Object> valueMap) {
        this.cdkValueMap = valueMap;
    }

}
