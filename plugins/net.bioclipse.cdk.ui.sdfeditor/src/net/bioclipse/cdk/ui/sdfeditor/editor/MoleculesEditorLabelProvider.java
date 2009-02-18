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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.SDFElement;
import net.bioclipse.cdk.jchempaint.view.SWTFontManager;
import net.bioclipse.cdk.jchempaint.view.SWTRenderer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class MoleculesEditorLabelProvider implements ITableLabelProvider{

    public Logger logger = Logger.getLogger(MoleculesEditorLabelProvider.class );
    public List<String>                          propertyHeaders;
    public Renderer                           renderer;
    private IDrawVisitor drawVisitor;
    public int imageWidth;
    private  IRenderer2DConfigurator renderer2DConfigurator;

    Collection<ILabelProviderListener> listeners =
     new HashSet<ILabelProviderListener>();

    public MoleculesEditorLabelProvider(int width) {
        imageWidth= width;
        setupRenderer();

    }
    
    public IRenderer2DConfigurator getRenderer2DConfigurator() {
        return renderer2DConfigurator;
    }
    public void setRenderer2DConfigurator(
                             IRenderer2DConfigurator renderer2DConfigurator ) {
        this.renderer2DConfigurator = renderer2DConfigurator;
    }
    
    
    public void setPropertyHeaders(List<String> headers){
        propertyHeaders = headers;
    }
    private void setupRenderer() {


        IFontManager fontManager = new SWTFontManager(Display.getCurrent());
        
        List<IGenerator> generators = new ArrayList<IGenerator>();
        generators.add(new BasicBondGenerator());
        generators.add(new BasicAtomGenerator());
        generators.add(new RingGenerator());
        generators.add(new HighlightAtomGenerator());
        generators.add(new HighlightBondGenerator());
        
        renderer = new Renderer(generators, fontManager);


        renderer.getRenderer2DModel().setDrawNumbers( false );
        renderer.getRenderer2DModel().setIsCompact( true );
        //renderer.getRenderer2DModel().setBondWidth( 15 );
        renderer.getRenderer2DModel().setDrawNumbers( false );
        //renderer.getRenderer2DModel().setBondDistance( 1 );
        renderer.getRenderer2DModel().setUseAntiAliasing(true );
    }

    public Image getColumnImage( Object element, int columnIndex ) {
        boolean generated = false;

        if ( columnIndex == 1 && element instanceof IAdaptable ) {
            ICDKMolecule mol =
                    (ICDKMolecule) ((IAdaptable) element)
                            .getAdapter( ICDKMolecule.class );
            if ( mol == null )
                return null;
            IAtomContainer drawMolecule = mol.getAtomContainer();

            // If no 2D coordinates
            if ( GeometryTools.has2DCoordinates( drawMolecule ) == false ) {
                // Test if 3D coordinates
                drawMolecule = null;
                if(drawMolecule == null) {
                    try {

                        drawMolecule = ((ICDKMolecule)Activator.getDefault()
                                .getCDKManager()
                                .generate2dCoordinates( mol ))
                                .getAtomContainer();
                    } catch ( Exception e ) {
                        logger.info( "Failed to generate 2D-coordinates" );

                        return null;
                    }
                }
                generated = true;
            }



            // renderer.getRenderer2DModel().setRenderingCoordinates(
            // coordinates);

            renderer.getRenderer2DModel()
                    .setShowExplicitHydrogens( false );

            Image image;

            renderer.getRenderer2DModel().setBackColor(new java.awt.Color(252,253,254));
//            renderer.getRenderer2DModel().setBackColor( java.awt.Color.CYAN );
            renderer.getRenderer2DModel().setUseAntiAliasing( true );
            renderer.getRenderer2DModel().setHighlightDistance( 10 );
            renderer.getRenderer2DModel().setFitToScreen( true );

            if (renderer2DConfigurator!=null){
                renderer2DConfigurator.configure( renderer.getRenderer2DModel(), mol );
            }
            Color greenScreen = new Color(Display.getCurrent(), 252, 253, 254);

                image =
                    new Image( Display.getDefault(),
                               imageWidth,
                               imageWidth );
                GC gc= new GC( image );


                gc.setBackground( greenScreen );
                gc.fillRectangle( image.getBounds() );


                Color generatedColor = new Color(gc.getDevice(),200,100,100);
                Font generatedFont =new Font(gc.getDevice(),"Arial",16,SWT.BOLD);
                if(generated) {
                    gc.setFont( generatedFont );
                    int h = image.getBounds().height-gc.getFontMetrics().getHeight();
                    gc.setForeground( generatedColor);
                    gc.drawText( "Generated", 0, h );

                }

                SWTRenderer drawVisitor= new SWTRenderer(gc);
                renderer.paintMolecule( drawMolecule,
                                        drawVisitor,
                                        new Rectangle2D.Double( 0, 0,
                                                                imageWidth,
                                                                imageWidth ),
                                        true );

                gc.dispose();
                generatedColor.dispose();

            ImageData imageData = image.getImageData();
            imageData.transparentPixel = imageData.palette.getPixel(greenScreen
            .getRGB());

            greenScreen.dispose();
            image.dispose();
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
                    if(row instanceof SDFElement)
                        text = Integer.toString( ((SDFElement)row).getNumber());
                    else
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

    /*
     * Utility method for copying 3D x,y to 2D coordinates
     */
    public static IAtomContainer generate2Dfrom3D( IAtomContainer atomContainer ) {

        try {
            atomContainer = (IAtomContainer) atomContainer.clone();

            // For each molecule,
            for ( int i = 0; i < atomContainer.getAtomCount(); i++ ) {
                IAtom atom = atomContainer.getAtom( i );
                Point3d p3 = atom.getPoint3d();
                Point2d p2 = new Point2d();
                p2.x = p3.x;
                p2.y = p3.y;
                atom.setPoint3d( null );
                atom.setPoint2d( p2 );
            }
        } catch ( CloneNotSupportedException e ) {
            return null;
        }
        return atomContainer;
    }

}