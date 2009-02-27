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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;
import net.bioclipse.cdk.jchempaint.view.SWTFontManager;
import net.bioclipse.cdk.jchempaint.view.SWTRenderer;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.model.INatTableModel;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.renderer.ICellRenderer;
import net.sourceforge.nattable.typeconfig.style.DisplayModeEnum;
import net.sourceforge.nattable.typeconfig.style.IStyleConfig;
import net.sourceforge.nattable.util.GUIHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;


/**
 * @author arvid
 *
 */
public class JCPCellPainter implements ICellPainter {
    public Logger logger = Logger.getLogger(JCPCellPainter.class );

    private Color generatedColor;
    private Font generatedFont;
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




    void setRenderer2DConfigurator(
                                           IRenderer2DConfigurator renderer2DConfigurator ) {

        this.renderer2DConfigurator = renderer2DConfigurator;
    }



    private void setupRenderer() {

        IFontManager fontManager = new SWTFontManager(Display.getCurrent());

        List<IGenerator> generators = new ArrayList<IGenerator>();

        generators.add(extensionGenerator = ChoiceGenerator.getGeneratorsFromExtensionPoint());
        generators.add(new BasicBondGenerator());
        generators.add(new BasicAtomGenerator());
        generators.add(new RingGenerator());
        generators.add(new HighlightAtomGenerator());
        generators.add(new HighlightBondGenerator());

        renderer = new Renderer(generators, fontManager);

        RendererModel rModel = renderer.getRenderer2DModel();
        rModel.setMargin( 30 );
        rModel.setDrawNumbers( false );
        rModel.setIsCompact( true );
//        rModel.setUseAntiAliasing(true );

        rModel.setShowExplicitHydrogens( false );
        rModel.setBackColor( new java.awt.Color(252,253,254));
        rModel.setFitToScreen( true );

//        greenScreen = new Color(Display.getCurrent(), 252, 253, 254);
        generatedColor = new Color(Display.getCurrent(),200,100,100);
        generatedFont =new Font(Display.getCurrent(),"Arial",16,SWT.BOLD);
//        imageBounds = new Rectangle2D.Double( 0, 0, imageWidth, imageWidth );
//        tempImage = new Image( Display.getDefault(),
//                               imageWidth,
//                               imageWidth );
//        imageBoundsSWT = tempImage.getBounds();

    }

    private boolean retriveAtomContainer(IAdaptable element,IAtomContainer[] result) {

        Assert.isTrue( result!=null && result.length >0);
        boolean generated = false;
        ICDKMolecule mol = (ICDKMolecule)element.getAdapter( ICDKMolecule.class);
        if(mol == null) return false;

        // If no 2D coordinates
        if ( !GeometryTools.has2DCoordinates( mol.getAtomContainer() ) ) {
            // Test if 3D coordinates
            IAtomContainer generatedAC = null;
            try {

                generatedAC = ((ICDKMolecule)Activator.getDefault()
                        .getCDKManager()
                        .generate2dCoordinates( mol ))
                        .getAtomContainer();
                //FIXME work-around for bug 613
                generatedAC.setProperties( new HashMap<Object, Object>(
                                    mol.getAtomContainer().getProperties()) );
                result[0] = generatedAC;
            } catch ( Exception e ) {
                logger.info( "Failed to generate 2D-coordinates" );
            }
            generated =  true;
        }else
            result[0]= mol.getAtomContainer();
        generated =  false;

        if (renderer2DConfigurator!=null){
            renderer2DConfigurator.configure( renderer.getRenderer2DModel(),
                                              result[0] );
        }
        return generated;
    }

    public void getColumnImage( GC gc, Rectangle rect,
                                 Object element ) {
        boolean generated = false;

        if ( element instanceof IAdaptable ) {

            IAtomContainer[] acArray= new IAtomContainer[1];
            generated = retriveAtomContainer( (IAdaptable ) element,
                                              acArray);
            if(acArray[0] == null) return;




            if(generated) {
                gc.setFont( generatedFont );
                int h = rect.height-gc.getFontMetrics().getHeight();
                gc.setForeground( generatedColor);
                gc.drawText( "Generated", 0, h );
            }

            gc.setClipping( rect );

            Rectangle2D rectangle = new Rectangle2D.Double( rect.x, rect.y,
                                                            rect.width,
                                                            rect.height);
            SWTRenderer drawVisitor= new SWTRenderer(gc);
            renderer.paintMolecule( acArray[0],
                                    drawVisitor,
                                    rectangle,
                                    true );
        }
    }


    public void drawCell( GC gc, Rectangle rectangle, NatTable natTable,
                          ICellRenderer cellRenderer, int row, int col,
                          boolean selected ) {

     // Selection Color
        IStyleConfig normalStyleConfig = cellRenderer.getStyleConfig(DisplayModeEnum.NORMAL.toString(), row, col);
        IStyleConfig selectionStyleConfig = cellRenderer.getStyleConfig(DisplayModeEnum.SELECT.toString(), row, col);

        Color fg = selected ? selectionStyleConfig.getForegroundColor(row, col)
            : normalStyleConfig.getForegroundColor(row, col);
        Color bg = selected ? selectionStyleConfig.getBackgroundColor(row, col)
            : normalStyleConfig.getBackgroundColor(row, col);

        gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
        gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);

        INatTableModel tableModel = natTable.getNatTableModel();
        // Allow display grid
        if (tableModel.isGridLineEnabled()) {
          rectangle.x = rectangle.x + 1;
          rectangle.width = rectangle.width - 1;
          rectangle.y = rectangle.y + 1;
          rectangle.height = rectangle.height - 1;
        }

        gc.fillRectangle( rectangle );

        getColumnImage( gc, rectangle, cellRenderer.getValue( row, col ) );

    }

    public boolean isUseExtensionGenerators() {

        return useExtensionGenerators;
    }


    public void setUseExtensionGenerators( boolean useExtensionGenerators ) {

        extensionGenerator.setUse( useExtensionGenerators );
    }

}
