/* $RCSfile$
 * $Author: egonw $    
 * $Date: 2007-01-04 18:26:00 +0100 (Thu, 04 Jan 2007) $    
 * $Revision: 7634 $
 *
 * Copyright (C) 2003-2007  The JChemPaint project
 *
 * Contact: jchempaint-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.applications.jchempaint.action;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import net.bioclipse.cdk.ui.editors.JCPMultiPageEditor;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
import org.openscience.cdk.applications.jchempaint.io.JCPExportFileFilter;
import org.openscience.cdk.applications.jchempaint.io.JCPFileView;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.manipulator.MoleculeSetManipulator;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.sun.media.jai.codec.JPEGEncodeParam;


/**
 * Exporting the current model various formats
 *
 * @author      Stefan Kuhn
 */
public class ExportAction extends JCPAction {

	private static final long serialVersionUID = -3287152749914283054L;
	
	private FileFilter currentFilter = null;
	
	protected IChemObjectWriter cow;
    
	public void run() {
		run(null);
	}
	
    /**
    * Saves the structure as an image
    */
    public void run(ActionEvent e){
    	try{
	        JFileChooser chooser = new JFileChooser();

	        //FIXME in Bioclipse2
//	        chooser.setCurrentDirectory(new File(BioResourceView.getRootFolder().getPersistedResource().getNameFromObject()));

	        JCPExportFileFilter.addChoosableFileFilters(chooser);
	        if (currentFilter != null) {
	            chooser.setFileFilter(currentFilter);
	        }
	        chooser.setFileView(new JCPFileView());
	        String filename="";
	        while(true){
		        int returnVal = chooser.showSaveDialog(null);
		        String type = null;
		        currentFilter = chooser.getFileFilter();
		        if(returnVal == JFileChooser.APPROVE_OPTION) {
		        	type = ((JCPExportFileFilter)currentFilter).getType();
		        	File outFile = chooser.getSelectedFile();
		        	filename = outFile.toString()+"."+type;
		        	boolean dowrite=true;
		        	if(new File(filename).exists()){
		        		MessageBox messageBox=new MessageBox(new Shell(), SWT.YES | SWT.NO);
		    			messageBox.setMessage("File already exists. Do you want to overwrite it?");
		    			messageBox.setText("File already exists");
		    			int value=messageBox.open();
		    			if(value==SWT.NO){
		    				dowrite=false;
		    			}
		        	}
		        	if(dowrite){
			            JChemPaintModel jcpm = ((JCPMultiPageEditor)this.getContributor().getActiveEditorPart()).getJcpModel();
			            ChemModel model = (ChemModel)jcpm.getChemModel();
			    		IAtomContainer ac = model.getBuilder().newAtomContainer();
			    		Iterator containers = MoleculeSetManipulator.getAllAtomContainers(model.getMoleculeSet()).iterator();
			    		while (containers.hasNext())
			    			ac.add((IAtomContainer)containers.next());
			        	FileOutputStream fos=new FileOutputStream(new File(filename));
			            String svg=this.getMolSvg(-1,-1,false,ac);
			            if(type.equals(JCPExportFileFilter.svg)){
			            	fos.write(svg.getBytes());
			            }else{
			            	JChemPaintModel jchemPaintModel=jcpm;
			                // A binary image
		                    Renderer2D r2d = new Renderer2D(jchemPaintModel.getRendererModel());
				            r2d.setRenderer2DModel(jchemPaintModel.getRendererModel());
				            HashMap coords=new HashMap();
				            Iterator it=jchemPaintModel.getRendererModel().getRenderingCoordinates().keySet().iterator();
				            while(it.hasNext()){
				            	Object key=it.next();
				            	coords.put(key,new Point2d(((Point2d)jchemPaintModel.getRendererModel().getRenderingCoordinates().get(key)).x,((Point2d)jchemPaintModel.getRendererModel().getRenderingCoordinates().get(key)).y));
				            }
				            Dimension dim = GeometryTools.get2DDimension(ac,coords);
				            GeometryTools.translateAllPositive(ac,coords);
				            GeometryTools.translate2D(ac, new Vector2d(40,40),coords);
				            Image awtImage = ((JCPMultiPageEditor)this.getContributor().getActiveEditorPart()).getDrawingPanel().createImage((int)dim.getWidth()+80, (int)dim.getHeight()+80);
				            Graphics2D snapGraphics = (Graphics2D) awtImage.getGraphics();
				            snapGraphics.setBackground(Color.WHITE);
				            snapGraphics.clearRect(0,0,(int)dim.getWidth()+80, (int)dim.getHeight()+80);
				            r2d.useScreenSize=false;
				            HashMap oldcoords=r2d.getRenderer2DModel().getRenderingCoordinates();
				            r2d.getRenderer2DModel().setRenderingCoordinates(coords);
				            r2d.paintMolecule(ac, (Graphics2D) snapGraphics,false,true);
				            r2d.getRenderer2DModel().setRenderingCoordinates(oldcoords);
				            r2d.useScreenSize=true;
					        RenderedOp image = JAI.create("AWTImage", awtImage);
					        if (type.equals(JCPExportFileFilter.bmp)) {
					            JAI.create("filestore", image, filename, "BMP", null);
					        } else if (type.equals(JCPExportFileFilter.tiff)) {
					            JAI.create("filestore", image, filename, "TIFF", null);
					        } else if (type.equals(JCPExportFileFilter.jpg)) {
					            JAI.create("filestore", image, filename, "JPEG", new JPEGEncodeParam());
					        } else if (type.equals(JCPExportFileFilter.png)) {
					            JAI.create("filestore", image, filename, "PNG", null);
					        } else { // default to a PNG binary image
					            JAI.create("filestore", image, filename, "PNG", null);
					        }
			            }
			            fos.flush();
			            fos.close();
		        		break;
		        	}		        	
		        }else{
		        	break;
		        }
	        }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    
    
    public String getMolSvg(int width, int height, boolean drawNumbers, IAtomContainer cdkmol) throws Exception {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    Renderer2DModel r2dm = new Renderer2DModel();
	    r2dm.setDrawNumbers(drawNumbers);
	    r2dm.setBackColor(Color.WHITE);
	    r2dm.setShowImplicitHydrogens(false);
	    r2dm.setShowEndCarbons(false);
	    Renderer2D renderer = new Renderer2D(r2dm);
	    int number=((int)Math.sqrt(cdkmol.getAtomCount()))+1;
	    int moleculewidth = number*100;
	    int moleculeheight = number*100;
	    if(width>-1){
		    moleculewidth=width;
		    moleculeheight=height;
	    }
	    if(moleculeheight<200 || moleculewidth<200){
	      r2dm.setIsCompact(true);
	      r2dm.setBondDistance(3);
	    }
	    r2dm.setBackgroundDimension(new Dimension(moleculewidth, moleculeheight));
	    GeometryTools.translateAllPositive(cdkmol,r2dm.getRenderingCoordinates());
	    GeometryTools.scaleMolecule(cdkmol, new Dimension(moleculewidth, moleculeheight), 0.8,r2dm.getRenderingCoordinates());
	    GeometryTools.center(cdkmol, new Dimension(moleculewidth, moleculeheight),r2dm.getRenderingCoordinates());
	    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
	    Document document = domImpl.createDocument(null, "svg", null);
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
	    svgGenerator.setBackground(Color.WHITE);
	    svgGenerator.setColor(Color.WHITE);
	    svgGenerator.fill(new Rectangle(0, 0, moleculewidth, moleculeheight));
	    renderer.paintMolecule(cdkmol, svgGenerator, false,true);
	    boolean useCSS = false;
	    baos = new ByteArrayOutputStream();
	    Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
	    StringBuffer sb = new StringBuffer();
	    svgGenerator.stream(outwriter, useCSS);
	    StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
	    while (tokenizer.hasMoreTokens()) {
	      String name = tokenizer.nextToken();
	      if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
	        sb.append(name.substring(0, name.length() - 1)).append(" width=\"" + moleculewidth + "\" height=\"" + moleculeheight + "\">" + "\n\r");
	      } else {
	        sb.append(name + "\n\r");
	      }
	    }
	    return (sb.toString());
    }
}
    
