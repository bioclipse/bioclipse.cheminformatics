/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg goglepox@users.sf.net
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.editor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.view.JChemPaintWidget;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor.Row;
import net.bioclipse.cdk.ui.views.IMoleculesEditorModel;
import net.bioclipse.core.util.LogUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.IRowContentProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.random.RandomAccessSDFReader;
import org.openscience.cdk.renderer.color.CPKAtomColors;
import org.openscience.cdk.renderer.color.IAtomColorer;
/**
 * @author arvid
 *
 */
public class MoleculeTableContentProvider implements IRowContentProvider{
    Logger logger = Logger.getLogger( MoleculeTableContentProvider.class );
    TableViewer viewer;
    IFile file = null;
    IMoleculesEditorModel model = null;
    int childCount;
    boolean readerReady = false;
    RandomAccessSDFReader reader;
//    IAtomColorer atomColorer;
    IRenderer2DConfigurator renderer2DConfigurator;
        public IRenderer2DConfigurator getRenderer2DConfigurator() {
                return renderer2DConfigurator;
        }
        public void setRenderer2DConfigurator(
                        IRenderer2DConfigurator renderer2DConfigurator) {
                this.renderer2DConfigurator = renderer2DConfigurator;
        }
        public void ready() {
        readerReady = true;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
     */
    public ICDKMolecule getMoleculeAt(int index) {
        Iterator<ICDKMolecule> iter;
        try {
            iter = Activator.getDefault().getCDKManager()
                   .createMoleculeIterator( file );
            ICDKMolecule molecule;
            int count = 0;
            while ( iter.hasNext() ) {
                molecule = iter.next();
                if ( count++ == index ) {
                    return molecule;
                }
            }
        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if(viewer != this.viewer)
            this.viewer = (TableViewer)viewer;
        if ( newInput != oldInput ) {
            if ( newInput instanceof IEditorInput ) {
                IEditorInput input = (IEditorInput) newInput;
                file = (IFile)input.getAdapter( IFile.class );
                if(file !=null) {
                    model = null;
                    return;
                }
                 model = (IMoleculesEditorModel)
                                input.getAdapter( IMoleculesEditorModel.class );
            }
        }
    }
    public int numberOfEntries(int max) {
        if(model != null) {
            return model.getNumberOfMolecules();
        }
        try {
            int count = 0;
            BufferedReader reader = new BufferedReader(
                         new InputStreamReader( file.getContents() ) );
            String line;
            while ( (line = reader.readLine()) != null ) {
                if ( line.contains( "$$$$" ) )
                    count++;
                if(count >= max)
                    break;
            }
            reader.close();
            return count;
        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
        return -1;
    }
    public IFile getFile() {
        return file;
    }
    public int init() {
        if(file != null) {
            IPath location = file.getLocation();
            if (location != null) {
               java.io.File file = location.toFile();
               IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
               try {
                reader = new RandomAccessSDFReader(file,builder);
                return reader.size();
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                LogUtils.debugTrace( logger, e );
            }
            }
        }
        return -1;
    }
    public void refresh( CompositeTable sender,
                         int currentObjectOffset,
                         Control rowControl ) {
        Row row = (Row) rowControl;
        Control[] columns = row.getChildren();
        Text index = (Text)columns[0];
        JChemPaintWidget structure = (JChemPaintWidget) columns[1];
        index.setText( Integer.toString( currentObjectOffset+1));
        IAtomContainer ac=null;
        try {
            if(model != null) {
                Object o =model.getMoleculeAt( currentObjectOffset );
                if(o instanceof IAdaptable) {
                    ac = ((ICDKMolecule) ((IAdaptable)o).getAdapter(
                                       ICDKMolecule.class  )).getAtomContainer();
                }
            } else if(readerReady) {
                IChemObject chemObject =reader.readRecord( currentObjectOffset );
//                List<IAtomContainer> containers =                     ChemFileManipulator.getAllAtomContainers((IChemFile)chemObject);
                IMolecule ret = (IMolecule) chemObject;
                ac = ret;
//                if (containers.size() > 1) {
//                    IAtomContainer newContainer = chemObject.getBuilder()
//                   .newAtomContainer();
//                for (IAtomContainer container : containers) {
//                    newContainer.add( container );
//                }
//                    mol = newContainer;
//                }else {
//                    if(containers.size() == 1) {
//                        mol = containers.get( 0 );
//                    }
//                }
                // TODO get a ICDKMolecules and assing it to mol
            } else {
                ac = this.getMoleculeAt( currentObjectOffset ).getAtomContainer();
            }
            structure.setAtomContainer( ac );
            setProperties( row.properties, ac );
            ICDKMolecule cdkmol=new CDKMolecule(ac);
            //Allows for external actions to register a renderer2dconfigurator
            //to customize rendering
            if (renderer2DConfigurator!=null) renderer2DConfigurator.configure(
            		structure.getRenderer2DModel(), cdkmol);
        } catch ( CoreException e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace( logger, e );
        }
    }
    private void setProperties(Label properties,IAtomContainer ac) {
        StringBuilder b = new StringBuilder();
        int count =0;
        Map<Object,Object> proper= ac.getProperties();
        for(Object o:proper.keySet()) {
//            b = new StringBuilder();
           String key = o.toString();
           String value = proper.get( o ).toString();
           b.append( key ).append( ": " ).append( value ).append( ", \n" );
//           properties.add( b.toString() );
           // FIXME dirty hack to make it look good
           if(count++>=5) break;
        }
        properties.setText( b.toString());
    }
}