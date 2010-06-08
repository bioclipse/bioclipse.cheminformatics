/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg<br> <goglepox@users.sf.net><br>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;

import static net.bioclipse.cdk.ui.sdfeditor.Activator.STRUCTURE_COLUMN_WIDTH;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintGlobalPropertiesManager;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.jchempaint.view.SWTRenderer;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget.Message;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.font.SWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactShape;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.Shape;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.ShowExplicitHydrogens;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.BackGroundColor;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;


/**
 * @author arvid
 *
 */
public class JCPCellPainter extends BackgroundPainter {
    public Logger logger = Logger.getLogger(JCPCellPainter.class );

    private Renderer renderer;
    private ChoiceGenerator extensionGenerator;

    private  IRenderer2DConfigurator renderer2DConfigurator;
    private boolean useExtensionGenerators = false;


    public JCPCellPainter() {
        setupRenderer();
    }

    IRenderer2DConfigurator getRenderer2DConfigurator() {

        return renderer2DConfigurator;
    }

    void setRenderer2DConfigurator( IRenderer2DConfigurator r2DConfigurator ) {

        this.renderer2DConfigurator = r2DConfigurator;
    }

    private void applyGlobalProperties(RendererModel rendererModel) {
        // apply the global JCP properties
        IJChemPaintGlobalPropertiesManager jcpprop =
        net.bioclipse.cdk.jchempaint.Activator.getDefault().getJCPPropManager();
        try {
            jcpprop.applyProperties(rendererModel);
        } catch (BioclipseException e) {
            LogUtils.debugTrace( logger, e );
        }
    }

    private void setupRenderer() {

        IFontManager fontManager = new SWTFontManager(Display.getCurrent());

        List<IGenerator<IAtomContainer>> generators =
        	new ArrayList<IGenerator<IAtomContainer>>();

        generators.add(extensionGenerator = ChoiceGenerator.getGeneratorsFromExtensionPoint());
       // generators.add(new BasicBondGenerator());
        generators.add(new BasicSceneGenerator());
        generators.add(new BasicAtomGenerator());
        generators.add(new RingGenerator());
        generators.add(new HighlightAtomGenerator());
        generators.add(new HighlightBondGenerator());

        renderer = new Renderer(generators, fontManager);

        RendererModel rModel = renderer.getRenderer2DModel();
        rModel.getRenderingParameter(CompactShape.class).setValue(Shape.OVAL);

        applyGlobalProperties( rModel );

        rModel.getRenderingParameter(Margin.class).setValue(30.0);
        rModel.setDrawNumbers( false );
        rModel.getRenderingParameter(CompactAtom.class).setValue(true );
//        rModel.setUseAntiAliasing(true );

        rModel.getRenderingParameter(ShowExplicitHydrogens.class).setValue( false );
        ((BackGroundColor)rModel.getRenderingParameter(BackGroundColor.class))
        	.setValue( new java.awt.Color(252,253,254));
        rModel.setFitToScreen( true );

    }

    private boolean retriveAtomContainer(IAdaptable element,IAtomContainer[] result) {

        Assert.isTrue( result!=null && result.length >0);
        boolean generated = false;
        ICDKMolecule mol = (ICDKMolecule)element.getAdapter( ICDKMolecule.class);
        if(mol == null) return false;

        // If no 2D coordinates
        if ( GeometryTools.has2DCoordinatesNew( mol.getAtomContainer() )<2 ) {
            // Test if 3D coordinates
            IAtomContainer generatedAC = null;
            try {

                generatedAC = ((ICDKMolecule)Activator.getDefault()
                        .getJavaCDKManager()
                        .generate2dCoordinates( mol ))
                        .getAtomContainer();
                result[0] = generatedAC;
            } catch ( Exception e ) {
                logger.info( "Failed to generate 2D-coordinates" );
            }
            generated =  true;
        }else {
            result[0]= mol.getAtomContainer();
            generated =  false;
        }
        if (renderer2DConfigurator!=null){
            renderer2DConfigurator.configure( renderer.getRenderer2DModel(),
                                              result[0] );
        }
        return generated;
    }

    public void getColumnImage( GC gc, Rectangle rect,
                                 Object element ) {
        boolean generated = false;

        IAtomContainer atomContainer = null;

        if ( element instanceof IAtomContainer ) {
            atomContainer = (IAtomContainer) element;
        } else  if ( element instanceof IAdaptable ) {
            IAtomContainer[] acArray= new IAtomContainer[1];
            generated = retriveAtomContainer( (IAdaptable ) element, acArray);
            if(acArray[0] == null) return;
            atomContainer = acArray[0];
        } else return;

            Color oldBackground = gc.getBackground();
            Rectangle2D rectangle = new Rectangle2D.Double( rect.x, rect.y,
                                                            rect.width,
                                                            rect.height);
            SWTRenderer drawVisitor= new SWTRenderer(gc);
            renderer.paintMolecule( atomContainer,
                                    drawVisitor,
                                    rectangle,
                                    true );
            if(generated && showGeneratedLabel()) {
                Message message = Message.GENERATED;
                gc.setBackground( oldBackground );
                JChemPaintWidget.paintMessage( gc, message, rect );
            }
    }

    public static boolean showGeneratedLabel() {
        return net.bioclipse.cdk.jchempaint.Activator.getDefault().getPreferenceStore()
                        .getBoolean( "showGeneratedLabel" );
    }

    public void paintCell( LayerCell cell, GC gc, Rectangle bounds,
                           IConfigRegistry configRegistry ) {
        super.paintCell( cell, gc, bounds, configRegistry );
        Rectangle originalClipping = gc.getClipping();
        gc.setClipping( bounds.intersection( originalClipping ) );
        IStyle cellStyle = CellStyleUtil.getCellStyle( cell, configRegistry );
        setupGCFromConfig( gc, cellStyle );
        getColumnImage( gc, bounds, cell.getDataValue() );
        gc.setClipping( originalClipping );
    }

    public boolean isUseExtensionGenerators() {

        return useExtensionGenerators;
    }

    public void setUseExtensionGenerators( boolean useExtensionGenerators ) {

        extensionGenerator.setUse( useExtensionGenerators );
    }

    public int getPreferredHeight( LayerCell cell, GC gc,
                                   IConfigRegistry configRegistry ) {
        return net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
        .getPreferenceStore().getInt( STRUCTURE_COLUMN_WIDTH );
    }

    public int getPreferredWidth( LayerCell cell, GC gc,
                                  IConfigRegistry configRegistry ) {
        return net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
        .getPreferenceStore().getInt( STRUCTURE_COLUMN_WIDTH );
    }

    private void setupGCFromConfig(GC gc, IStyle cellStyle) {
        Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
        Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
        gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
        gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
    }
}
