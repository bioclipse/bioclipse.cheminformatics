/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.cml.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.xmlcml.cml.attribute.DictRefAttribute;
import org.xmlcml.cml.attribute.MetadataNameAttribute;
import org.xmlcml.cml.attribute.UnitsAttribute;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLRuntimeException;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLDictionary;
import org.xmlcml.cml.map.DictionaryMap;
import org.xmlcml.cml.map.UnitListMap;


public class ValidateCMLManager implements IValidateCMLManager {

	private boolean succeeded = true;
	private List<String> errorList = new ArrayList<String>();
	private static File simpleDir = null;
	private static File unitsDir = null;
	private static File dictDir = null;
	private static DictionaryMap simpleMap = null;
	private static UnitListMap unitListMap = null;
	private static DictionaryMap dictListMap = null;
	CMLElement cmlElement = null;
	static String result;
	
	
    public String validate(String filename) throws IOException, BioclipseException {
		initDicts();
		IFile file=ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filename));
		if(!file.exists())
			throw new BioclipseException("File "+filename+" does not exist");
		return validateCMLFile(file);
	}

    public void validate(IFile input) throws IOException {
		initDicts();
		result=validateCMLFile(input);
	    Display.getDefault().syncExec(
			      new Runnable() {
			        public void run(){
				        MessageBox mb = new MessageBox(new Shell(), SWT.OK);
				        mb.setText("CML checked");
				        mb.setMessage(ValidateCMLManager.result);
				        mb.open();
			        }
			      });
	}

    public String getNamespace() {
        return "cml";
    }
    
	public void namespaceThemAll(Elements elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element elem = elements.get(i);
			elem.setNamespaceURI(CMLUtil.CML_NS);
			if (elem.getChildCount() != 0) {
				namespaceThemAll(elem.getChildElements());
			}
		}
	}
	
	private void initDicts() throws IOException {
		if (simpleDir == null || unitsDir == null) {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

			URL simpleURL = FileLocator.resolve(FileLocator.find(bundle, new Path("/dict10/simple/catalog.cml"),null));
			URL unitsURL = FileLocator.resolve(FileLocator.find(bundle, new Path("/dict10/units/catalog.cml"),null));
			URL dictURL = FileLocator.resolve(FileLocator.find(bundle, new Path("/dict10/dict/catalog.cml"),null));
			
			try {
				unitsDir = new File(unitsURL.toURI().getPath());				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
	    if (simpleMap  == null) {
	        try {
	          simpleMap = new DictionaryMap(simpleURL, new CMLDictionary());
	        } catch (Exception e) {
	        	e.printStackTrace();
	          throw new CMLRuntimeException("dictionaryMap could not be created "+e);
	        }
	      }
	    if (dictListMap == null) {
	        try {
	          dictListMap  = new DictionaryMap(dictURL, new CMLDictionary());
	        } catch (Exception e) {
	          throw new CMLRuntimeException("dictListMap could not be created "+e);
	        }
	      }
		}
		if (unitListMap == null) {
			try {
				unitListMap  = new UnitListMap(unitsDir, true);
			} catch (Exception e) {
				throw new CMLRuntimeException("unitListMap could not be created "+e);
			}
		}
	}

    
	private String validateCMLFile(IFile input) {
		succeeded = true;
		StringBuffer returnString=new StringBuffer();
		InputStream is=null;
		try {
			is=input.getContents();
			cmlElement = (CMLElement) new CMLBuilder().build(is).getRootElement() ;
		} catch (ParsingException e) {
			returnString.append(e);
			this.succeeded = false;
		} catch (ClassCastException ccee) {
			InputStream is2=null;
			try{
				is2=input.getContents();
				Element element=new CMLBuilder().build(is2).getRootElement();
				namespaceThemAll(element.getChildElements());
				element.setNamespaceURI(CMLUtil.CML_NS);
				cmlElement = (CMLElement) new CMLBuilder().parseString(element.toXML());
			}catch(Exception ex){
				returnString.append(ex);
				this.succeeded = false;
			}
			finally{
				
				try {
					is2.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			returnString.append(e);
			this.succeeded = false;
		}
		finally{
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (succeeded) {
			errorList.clear();
			errorList = new DictRefAttribute().checkAttribute(cmlElement, simpleMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
			errorList.clear();
			errorList = new MetadataNameAttribute().checkAttribute(cmlElement, simpleMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
			errorList.clear();
			errorList = new UnitsAttribute().checkAttribute(cmlElement, unitListMap);
			if (errorList.size() > 0) {
				for (String error : errorList) {
					returnString.append("warning: " + error);
				}
			}
	        return("Input is valid CML. "+returnString.toString());
		}else{
			return("Input is not valid CML: "+returnString.toString());
		}
	}

	public boolean getSuceeded() {
		return succeeded;
	}

	public CMLElement getCMLElement() {
		return cmlElement;
	}
}
