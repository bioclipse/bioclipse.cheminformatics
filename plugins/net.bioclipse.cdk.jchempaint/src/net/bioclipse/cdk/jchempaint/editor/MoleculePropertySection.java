package net.bioclipse.cdk.jchempaint.editor;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.business.IJavaCDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculePropertySource;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.inchi.InChI;
import net.bioclipse.inchi.business.Activator;
import net.bioclipse.inchi.business.IInChIManager;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MoleculePropertySection extends AbstractPropertySection {

    private static final Logger logger = LoggerFactory.getLogger( MoleculePropertySection.class );

    private Text         inchi;
    private Text         inchi_key;

    private ICDKMolecule molecule;

    @Override
    public void createControls( Composite parent,
                                TabbedPropertySheetPage aTabbedPropertySheetPage ) {

        super.createControls( parent, aTabbedPropertySheetPage );

        Composite composite = getWidgetFactory()
                        .createFlatFormComposite( parent );
        FormData data;
        
        inchi_key = getWidgetFactory().createText( composite, "" );
        inchi_key.setEditable( false );
        data = new FormData();
        data.left = new FormAttachment( 0, STANDARD_LABEL_WIDTH );
        data.right = new FormAttachment( 100, 0 );
        data.top = new FormAttachment( 0, ITabbedPropertyConstants.VSPACE );
        inchi_key.setLayoutData( data );

        CLabel inchiKeyLabel = getWidgetFactory().createCLabel( composite, "InChI key:" );
        inchiKeyLabel.setAlignment( SWT.RIGHT );
        data = new FormData();
        data.left = new FormAttachment(0,0);
        data.right = new FormAttachment(inchi_key,-ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(inchi_key,0,SWT.CENTER);
        inchiKeyLabel.setLayoutData( data );
        
        inchi = getWidgetFactory().createText( composite, "" ); //$NON-NLS-1$
        inchi.setEditable( false );
        data = new FormData();
        data.left = new FormAttachment( 0, STANDARD_LABEL_WIDTH );
        data.right = new FormAttachment( 100, 0 );
        data.top = new FormAttachment( inchi_key, ITabbedPropertyConstants.VSPACE );
        inchi.setLayoutData( data );
        // labelText.addModifyListener( listener );

        CLabel labelLabel = getWidgetFactory().createCLabel( composite,
                                                             "InChI:" ); //$NON-NLS-1$
        labelLabel.setAlignment( SWT.RIGHT );
        data = new FormData();
        data.left = new FormAttachment( 0, 0 );
        data.right = new FormAttachment( inchi,
            -ITabbedPropertyConstants.HSPACE );
        data.top = new FormAttachment( inchi, 0, SWT.CENTER );
        labelLabel.setLayoutData( data );

        Button button = getWidgetFactory().createButton( composite, "Generate",
                                                         SWT.PUSH );
        data = new FormData();
        data.left = new FormAttachment( inchi, 0, SWT.LEFT );
        data.right = new FormAttachment( 100, 0 );
        data.top = new FormAttachment( inchi, 0, SWT.BOTTOM );
        button.setLayoutData( data );
        button.addSelectionListener( new SelectionListener() {

            @Override
            public void widgetSelected( SelectionEvent e ) {

                final ICDKMolecule item = molecule;

                boolean allOK = true;
                try {
                    IAtomContainer container = item.getAtomContainer().clone();
                    String result = CDKMoleculePropertySource.ensureFullAtomTyping( container );
                    if ( result == null || result.length() > 0 )
                        allOK = false;
                } catch ( CloneNotSupportedException e1 ) {
                    allOK = false;
                }
                if ( !allOK ) {
                    String msg = "Incorrect Structure.";
                    item.setProperty( CDKMolecule.INCHI_OBJECT, msg );
                    refresh();
                }

                ICDKManager cdk = getService( IJavaCDKManager.class );
                final ICDKMolecule inchiClone;
                try {
                    inchiClone  = cdk.clone( item );
                }
                catch ( BioclipseException ex ) {
                    throw new RuntimeException( ex );
                }

                if ( item.getProperty( CDKMolecule.INCHI_OBJECT,
                                       Property.USE_CACHED ) == null ) {
                    Job j = new Job( "Calculating inchi for properties view" ) {

                        @Override
                        protected IStatus run( IProgressMonitor monitor ) {

                            IInChIManager inchi = Activator.getDefault()
                                            .getJavaInChIManager();
                            try {
                                item.setProperty( CDKMolecule.INCHI_OBJECT,
                                                  inchi.generate( inchiClone ) );
                            } catch ( Exception e ) {
                                logger.debug( "Could not calculate InChi", e );
                                item.setProperty( CDKMolecule.INCHI_OBJECT,
                                                  InChI.FAILED_TO_CALCULATE );
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    // j.addJobChangeListener( new PropertyViewNotifier() );
                    j.addJobChangeListener( new JobChangeAdapter() {
                        
                        @Override
                        public void done( IJobChangeEvent event ) {
                            Display.getDefault().asyncExec( new Runnable() {
                                @Override
                                public void run() {
                                    MoleculePropertySection.this.refresh();
                                }
                            });
                        }
                    } );
                    j.schedule();
                }
            }

            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {
            }
        } );
    }

    public void setInput( IWorkbenchPart part, ISelection selection ) {

        super.setInput( part, selection );
        Assert.isTrue( selection instanceof IStructuredSelection );
        Object input = ((IStructuredSelection) selection).getFirstElement();
        Assert.isTrue( input instanceof ICDKMolecule );
        this.molecule = (ICDKMolecule) input;
    }
    
    public void refresh() {
//        labelText.removeModifyListener(listener);
        CDKMoleculePropertySource properties = (CDKMoleculePropertySource) molecule
            .getAdapter(IPropertySource.class);
        Object value = properties
                        .getPropertyValue( "net.bioclipse.cdk.domain.property.InChI" );
        inchi.setText( value != null ? value.toString() : "N/A" );

        value = properties
                        .getPropertyValue( "net.bioclipse.cdk.domain.property.InChIKey" );
        inchi_key.setText( value != null ? value.toString() : "N/A" );
        // labelText.addModifyListener(listener);
    }
    
    private <T> T getService(Class<T> manager) {
        BundleContext bc = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
        ServiceReference<T> sRef = bc.getServiceReference( manager );
        return bc.getService( sRef );
    }
}
