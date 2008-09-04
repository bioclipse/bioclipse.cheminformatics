/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 * 
 * Contributors:
 *      Arvid Berg 
 *     
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.widgets.SWTRenderer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer2DModel;

public class MoleculesEditorLabelProvider implements ITableLabelProvider{

    public Logger logger = Logger.getLogger(MoleculesEditorLabelProvider.class );
    public List<String>                          propertyHeaders;
    public SWTRenderer                           renderer;    
    public int imageWidth;

    Collection<ILabelProviderListener> listeners =
     new HashSet<ILabelProviderListener>();


    public MoleculesEditorLabelProvider(int width) {
        imageWidth= width;
        setupRenderer();
        
    }
    public void setPropertyHeaders(List<String> headers){
        propertyHeaders = headers;
    }
    private void setupRenderer() {
        
        renderer = new SWTRenderer( new Renderer2DModel() , 7f);
        renderer.getRenderer2DModel().setDrawNumbers( false );
        renderer.getRenderer2DModel().setIsCompact( true );
        renderer.getRenderer2DModel().setBondWidth( 15 );
        renderer.getRenderer2DModel().setUseAntiAliasing(true );
        
    }

    public Image getColumnImage( Object element, int columnIndex ) {

        if ( columnIndex == 1 && element instanceof IAdaptable ) {
            ICDKMolecule mol =
                    (ICDKMolecule) ((IAdaptable) element)
                            .getAdapter( ICDKMolecule.class );
            if ( mol == null )
                return null;
            IAtomContainer drawMolecule = mol.getAtomContainer();
            Dimension screenSize =
                    new Dimension( imageWidth,
                                   imageWidth );

            // If no 2D coordinates
            if ( GeometryTools.has2DCoordinates( drawMolecule ) == false ) {
                // Test if 3D coordinates
                if ( GeometryTools.has3DCoordinates( drawMolecule ) == true ) {
                    // Collapse on XY plane
                    drawMolecule = SWTRenderer.generate2Dfrom3D( 
                                                         drawMolecule );

                }
            }

            GeometryTools.translateAllPositive( drawMolecule );
            GeometryTools.scaleMolecule( drawMolecule, screenSize, 0.8 );
            GeometryTools.center( drawMolecule, screenSize );
            

            // renderer.getRenderer2DModel().setRenderingCoordinates(
            // coordinates);
            renderer.getRenderer2DModel()
                    .setBackgroundDimension( screenSize );
            
            renderer.getRenderer2DModel()
                    .setShowExplicitHydrogens( false );

            Image image =
                    new Image( Display.getDefault(),
                               imageWidth,
                               imageWidth );
            GC gc= new GC( image );
            Color greenScreen = new Color(Display.getCurrent(), 222, 223, 224);
            
            gc.setBackground( greenScreen );
            gc.fillRectangle( image.getBounds() );
            
            renderer.getRenderer2DModel().setBackColor(new java.awt.Color(222,223,224));
            
            renderer.paintMolecule(
                                    drawMolecule,
                                    gc ,
                                    new Rectangle2D.Double(
                                             0,
                                             0,
                                             imageWidth,
                                             imageWidth ) );
            
            ImageData imageData = image.getImageData();
            imageData.transparentPixel = imageData.palette.getPixel(greenScreen
            .getRGB());
            gc.dispose();
            greenScreen.dispose();
            return new Image(Display.getDefault(),imageData);
        }
        return null;
    }

    public String getColumnText( Object element, int columnIndex ) {

        // offset the index to the properties so get(x) works
        int propertyindex = columnIndex - 2;
        String text = null;
        if(columnIndex == 0 && element instanceof PendingUpdateAdapter ){
            return "Pending...";
        }
        if ( element instanceof IAdaptable ) {
            IAdaptable row = (IAdaptable) element;
            ICDKMolecule molecule =
                    (ICDKMolecule) row.getAdapter( ICDKMolecule.class );

            // if(propertyHeaders==null && molecule!=null)
            // createPropertyHeaders( molecule.getAtomContainer());

            switch ( columnIndex ) {
                case 0:
                    text = "NA";//text = Integer.toString( row.getNumber() );
                    break;
                case 1:
                    // text = Long.toString(row.getPosition());
                    break;
                default:
                    if ( molecule == null
                         || propertyindex >= propertyHeaders.size() )
                        return null;
                    IAtomContainer atomContainer =
                            molecule.getAtomContainer();
                    Object o=atomContainer.getProperty(
                                         propertyHeaders.get( propertyindex ) );
                    text = (o!=null?o.toString():null);
            }
        }
        return text;
    }

    public void addListener( ILabelProviderListener listener ) {

        listeners.add( listener );
    }

    public void dispose() {

        listeners.clear();
    }

    public boolean isLabelProperty( Object element, String property ) {

        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener( ILabelProviderListener listener ) {

        listeners.remove( listener );
    }


}