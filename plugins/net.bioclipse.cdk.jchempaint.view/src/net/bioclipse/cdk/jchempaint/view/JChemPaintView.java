package net.bioclipse.cdk.jchempaint.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.RenderingModel;
import org.openscience.cdk.renderer.modules.AbstractModule;
import org.openscience.cdk.renderer.modules.AtomModule;
import org.openscience.cdk.renderer.modules.AtomSymbolModule;
import org.openscience.cdk.renderer.modules.BondModule;
import org.openscience.cdk.renderer.modules.IRenderingModule;

public class JChemPaintView extends ViewPart implements ISelectionListener {

    IAtomContainer  atomContainer;
    Renderer2DModel r2DModel;

    Canvas          canvas;
    private final static StructureDiagramGenerator sdg = new StructureDiagramGenerator();

    @Override
    public void createPartControl( Composite parent ) {

        canvas = new Canvas( parent, SWT.NONE );
        canvas.setSize( 200, 200 );
        r2DModel = new Renderer2DModel();
        r2DModel.setBondWidth( 4 );
        r2DModel.setBondDistance( 4 );
        r2DModel.setForeColor( Color.BLACK );
        r2DModel.setAtomColorer( new CDK2DAtomColors() );
        org.eclipse.swt.graphics.Color color = canvas.getBackground();
        r2DModel.setBackColor( new Color( color.getRed(), color.getGreen(),
                                          color.getBlue() ) );
        // r2DModel.setIsCompact( true );
        r2DModel.setAtomRadius( 20 );

        canvas.addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent e ) {

                JChemPaintView.this.paintControl( e );
            }
        } );
        getViewSite().getPage().addSelectionListener(this);
    }

    @Override
    public void setFocus() {
        
        canvas.forceFocus();
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
        IAtomContainer ac = null;
        if(part != this && selection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection)selection)
                                            .getFirstElement();
            if(selected instanceof IAdaptable) {
                ac = (IAtomContainer) ((IAdaptable)selected)
                                    .getAdapter( IAtomContainer.class );
                
                if(ac == null) {
                    ICDKMolecule mol = (ICDKMolecule)
                                        ((IAdaptable)selected).
                                        getAdapter( ICDKMolecule.class );
                    
                    if(mol != null){
                        ac = mol.getAtomContainer();
//                  //Create 2D-coordinates if not available
                    if (!GeometryTools.has2DCoordinates( ac )){
                       ac = null; 
//                        ac     
//                        sdg.setMolecule((IMolecule)molecule.clone());
//                        sdg.generateCoordinates();
//                        sdg.get
//                        molecule = sdg.getMolecule();
                    }
                    }
                    
                }
                
            }
            
        }
        setAtomContainer( ac );

    }

    private void paintControl( PaintEvent event ) {

        if ( atomContainer == null )
            return;
        RenderingModel model = new RenderingModel();
        Point size = canvas.getSize();
        Dimension dimension = new Dimension( size.x, size.y );
        double[] scalse = model.getDimensions( atomContainer, dimension );

        RenderingModel renderingModel = generateRenderingModel( model );
        Transform transform= new Transform(event.gc.getDevice());
        Point2D center = model.center( atomContainer, dimension );
        transform.translate((float) center.getX(), (float)center.getY()  );
        event.gc.setTransform( transform );
        SWTRenderer renderer = new SWTRenderer( event.gc, r2DModel, scalse );
        renderingModel.accept( renderer );
    }

    private RenderingModel generateRenderingModel( RenderingModel model ) {

        AbstractModule superModule = new AbstractModule() {

            @Override
            public IRenderingElement accept( IAtomContainer ac, IAtom atom,
                                             IRenderingElement element ) {

                return element;
            }

            @Override
            public IRenderingElement accept( IAtomContainer ac, IBond bond,
                                             IRenderingElement element ) {

                return element;
            }

            @Override
            public Renderer2DModel getModel() {

                return r2DModel;
            }
        };

        IRenderingModule modules =
                new AtomSymbolModule(
                    new BondModule(
                    new AtomModule(
                    superModule ) ) );
        if ( atomContainer == null )
            return model;

        /*
         * accept( ..., element) only known in AbstractModule and all that needs
         * to be known here is accept( IAtomcontainer, atom/bond) superModule
         * created in AbstractModule in default constructor maybe?
         */
        for ( IBond bond : atomContainer.bonds() ) {
            model.add( modules.accept( atomContainer, bond, null ) );
        }
        for ( IAtom atom : atomContainer.atoms() ) {
            model.add( modules.accept( atomContainer, atom, null ) );
        }

        return model;
    }

    public void setAtomContainer( IAtomContainer ac ) {

        atomContainer = ac;
        if(ac == null) {
            canvas.setVisible( false );
        } else {
            canvas.setVisible( true );
            canvas.redraw();
        }
    }

}
