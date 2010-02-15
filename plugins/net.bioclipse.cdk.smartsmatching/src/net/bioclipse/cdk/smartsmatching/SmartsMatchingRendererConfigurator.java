/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.smartsmatching.views.SmartsMatchingView;
import net.bioclipse.cdk.ui.sdfeditor.editor.IRenderer2DConfigurator;
import net.bioclipse.core.business.BioclipseException;

public class SmartsMatchingRendererConfigurator 
  implements IRenderer2DConfigurator{

    private static final Logger logger = Logger.getLogger(
                                      SmartsMatchingRendererConfigurator.class);

    //The bondcolor in M2d results
    Color bondcolor=new Color(33,33,33);

    /**
     * Add tooltip read from M2D property
     */
    public void configure(final RendererModel model, final IAtomContainer ac) {

//        Job job=new Job("") {
//            
//            @Override
//            protected IStatus run( IProgressMonitor monitor ) {

                ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
                
                //Get the SMARTS from the SmartsView
                List<String> allSmarts=SmartsMatchingView.getActiveSmarts();
                if (allSmarts==null || allSmarts.size()<=0)
                    return;
//                    return Status.OK_STATUS;
                
                //Read from prefs for AC to see if we need to calculate again
                String smartsProp=(String) ac.getProperty( 
                                SmartsMatchingConstants.SMARTS_MATCH_PROPERTY );

                //The below does not work since properties are lost.
                //Filed as Bug 1676
                if (smartsProp!=null){
                    //Check if we have calculated, if so, do not repeat it
                    return;
                }

                //Calculate smarts matches and store as property
                ICDKMolecule cdkmol=new CDKMolecule(ac);

                //Store atoms for highlighting in a set, no duplicates
                Set<IAtom> hitAtoms=new HashSet<IAtom>();

                for (String activeSmarts : allSmarts ){
                    List<IAtomContainer> hitACs;
                    try {
                        hitACs = cdk.getSmartsMatches( cdkmol, activeSmarts );
                        if (hitACs!=null){

                            for (IAtomContainer hitAC : hitACs){
                                for (IAtom atom : hitAC.atoms()){
                                    hitAtoms.add( atom );
                                }
                            }
                        }
                    } catch ( BioclipseException e ) {
                        logger.error( "SMARST matching failed for smarts: " 
                                      + activeSmarts + " due to: " + e.getMessage());
                    }
                }
                
                if (hitAtoms.size()>0)
                    //Serialize to property
                    SmartsMatchingHelper.serializeToProperty(cdkmol, hitAtoms);

                model.setIsCompact( true );
                model.setShowAtomTypeNames( false );
                model.setShowImplicitHydrogens( false );
                model.setShowExplicitHydrogens(  false );

                
                //Update drawing
                //TODO: is this really needed here?
                model.fireChange();

                // TODO Auto-generated method stub
                return;
//                return Status.OK_STATUS;
//            }
//        };
//        
//        job.setUser( false );
//        job.schedule();
        

    }

}
