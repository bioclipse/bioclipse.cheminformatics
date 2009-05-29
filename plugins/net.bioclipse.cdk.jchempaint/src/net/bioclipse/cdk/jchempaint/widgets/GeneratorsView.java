/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.widgets;

import java.util.List;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.openscience.cdk.renderer.generators.IGenerator;


/**
 * @author arvid
 *
 */
public class GeneratorsView extends PageBookView {

    @Override
    protected IPage createDefaultPage( PageBook book ) {
        MessagePage messagePage = new MessagePage();
        initPage( messagePage );
        messagePage.setMessage( "No JCP editor open" );
        messagePage.createControl( book );
        return messagePage;
    }

    @Override
    protected PageRec doCreatePage( final IWorkbenchPart part ) {
        JChemPaintEditor editor = (JChemPaintEditor) part;
        final List<IGenerator> generators = editor.getWidget().getRenderer()
                                            .getGenerators();
        
        Page page = new Page() {
            
            private CheckboxTreeViewer treeViewer;
            @Override
            public void createControl( Composite parent ) {

                final Tree tree = new Tree(parent,SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.CHECK);
             // Attach a listener directly after the creation
             tree.addListener(SWT.Selection,new Listener() {
                public void handleEvent(Event event) {
                    if( event.detail == SWT.CHECK ) {
                        if( !(event.item.getData() instanceof ChoiceGenerator) &&
                                !(((TreeItem)event.item).getParentItem().getData() instanceof ChoiceGenerator)) {
                           event.detail = SWT.NONE;
                           event.type   = SWT.None;
                           event.doit   = false;
                           try {
                              tree.setRedraw(false);
                              TreeItem item = (TreeItem)event.item;
                              
                              item.setChecked(! item.getChecked() );
                           } finally {
                              tree.setRedraw(true);
                           }
                        }
                    }
                }
             });

                treeViewer = new CheckboxTreeViewer(tree);
                treeViewer.setLabelProvider( new LabelProvider() {
                    @Override
                    public String getText( Object element ) {
                        return element.getClass().getSimpleName();
                    }
                });
                
                treeViewer.setContentProvider( new ITreeContentProvider() {

                    @SuppressWarnings("unchecked")
                    public Object[] getElements( Object inputElement ) {
                        if(inputElement instanceof List) {
                            List<IGenerator> generators = (List<IGenerator>) inputElement;
                            return generators.toArray();
                        }else if(inputElement instanceof ChoiceGenerator) {
                            ChoiceGenerator cg = (ChoiceGenerator) inputElement;
                            return cg.toArray();
                        }
                        return new Object[0];
                    }

                    public void dispose() {

                    }

                    public void inputChanged( Viewer viewer, Object oldInput,
                                              Object newInput ) {

                    }

                    @SuppressWarnings("unchecked")
                    public Object[] getChildren( Object parentElement ) {

                        if(parentElement instanceof List) {
                            List<IGenerator> generators = (List<IGenerator>) parentElement;
                            return generators.toArray();
                        }else if(parentElement instanceof ChoiceGenerator) {
                            ChoiceGenerator cg = (ChoiceGenerator) parentElement;
                            return cg.toArray();
                        }
                        return new Object[0];
                    }

                    public Object getParent( Object element ) {

                        // TODO Auto-generated method stub
                        return null;
                    }

                    public boolean hasChildren( Object element ) {

                        if(element instanceof List || element instanceof ChoiceGenerator)
                            return true;
                        return false;
                    }
                    
                });
                
                treeViewer.setInput( generators );
                for(IGenerator g:generators) {
                    if(!(g instanceof ChoiceGenerator)) {
                        treeViewer.setChecked( g, true );
                        treeViewer.setGrayed( g, true );
                    }
                }
            }

            @Override
            public Control getControl() {
                return treeViewer.getTree();
            }

            @Override
            public void setFocus() {
                treeViewer.getTree().setFocus();
            }
            
        };
        
        
//        MessagePage messagePage = new MessagePage();
        initPage( page );
//        messagePage.setMessage( "Page for "+part.getTitle() );
//        messagePage.createControl( getPageBook());
        page.createControl( getPageBook() );
        return new PageRec(part,page);
    }

    @Override
    protected void doDestroyPage( IWorkbenchPart part, PageRec pageRecord ) {
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {

        
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {    
            IEditorPart part = page.getActiveEditor();
            if(part != null && part.getSite().getId()
                    .startsWith( "net.bioclipse.cdk.ui.editors.jchempaint" ))
            return page.getActiveEditor();
        }
        return null;
    }

    @Override
    protected boolean isImportant( IWorkbenchPart part ) {
        return part.getSite().getId().startsWith( "net.bioclipse.cdk.ui.editors.jchempaint" );
    }

}
