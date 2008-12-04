package net.bioclipse.cdk.jchempaint.view;

import java.awt.Color;

import net.bioclipse.cdk.domain.ICDKMolecule;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;

public class JChemPaintView extends ViewPart implements ISelectionListener {

    
    RendererModel r2DModel;

    JChemPaintWidget jcpWidget;
   
    private final static StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    
    public Canvas getCanvas(){return jcpWidget;}
    
    @Override
    public void createPartControl( Composite parent ) {

        jcpWidget = new JChemPaintWidget( parent, SWT.NONE );
        jcpWidget.setSize( 200, 200 );
        
        r2DModel = new RendererModel();
        r2DModel.setBondWidth( 4 );
        r2DModel.setBondDistance( 4 );
        r2DModel.setForeColor( Color.BLACK );
        r2DModel.setAtomColorer( new CDK2DAtomColors() );
        org.eclipse.swt.graphics.Color color = jcpWidget.getBackground();
        
        r2DModel.setBackColor( new Color( color.getRed(), color.getGreen(),
                                          color.getBlue() ) );
        // r2DModel.setIsCompact( true );
        r2DModel.setAtomRadius( 20 );
        r2DModel.setShowAromaticity( true );
        r2DModel.setShowImplicitHydrogens( true );
        
        jcpWidget.setRenderer2DModel( r2DModel );
        
       // getViewSite().getPage().addSelectionListener(this);
    }

    @Override
    public void setFocus() {
        
        jcpWidget.forceFocus();
    }

    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {

        IAtomContainer ac = null;
        if ( part != this && selection instanceof IStructuredSelection ) {
            Object selected = ((IStructuredSelection) selection)
            .getFirstElement();
            if ( selected instanceof IAdaptable ) {
                ac = (IAtomContainer) ((IAdaptable) selected)
                .getAdapter( IAtomContainer.class );

                if ( ac == null ) {
                    ICDKMolecule mol = (ICDKMolecule) ((IAdaptable) selected)
                    .getAdapter( ICDKMolecule.class );

                    if ( mol != null ) {
                        ac = mol.getAtomContainer();
                        // //Create 2D-coordinates if not available
                        if ( !GeometryTools.has2DCoordinates( ac ) ) {
                            IAtomContainer container = null;
                            try {
                                sdg.setMolecule( (IMolecule) ac.clone() );
                                sdg.generateCoordinates();
                                // sdg.getMolecule();
                                container = sdg.getMolecule();
                            } catch ( CloneNotSupportedException e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch ( Exception e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            ac = container;
                        }
                    }

                }

            }

        }
        setAtomContainer( ac );

    }

    public void setAtomContainer( IAtomContainer atomContainer ) {

        if ( jcpWidget != null ) {
            jcpWidget.setAtomContainer( atomContainer );
        }
    }
}
