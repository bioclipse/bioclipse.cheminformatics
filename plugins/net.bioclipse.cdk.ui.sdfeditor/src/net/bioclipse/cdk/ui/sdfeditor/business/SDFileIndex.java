/*******************************************************************************
 * Copyright (c) 2009 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg <goglepox@users.sf.net>
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class SDFileIndex {
    private static SDFileIndex EMPTY_INDEX = new SDFileIndex( null,
                                                        new ArrayList<Long>()) {
        @Override
        public int size() {
            return 0;
        }

    };
      final IFile file;

      List<Long> filePos;
      Map<Integer,List<Long>> propertiesPos;
      boolean isBondOrder4 = false;

      SDFileIndex(final IFile file,final List<Long> filePos) {
          this(file,filePos,null,false);
      }

      SDFileIndex( final IFile file,
                   final List<Long> filePos,
                   Map<Integer,List<Long>> propPos) {
          this(file,filePos,propPos,false);
      }
      SDFileIndex( final IFile file,
                   final List<Long> filePos,
                   Map<Integer,List<Long>> propPos,
                   boolean bondOrder4) {
          this.file = file;
          this.filePos=new ArrayList<Long>(filePos);
          this.filePos.add( 0, 0l );
          if(propPos != null)
              this.propertiesPos = propPos;
          else
              propertiesPos = Collections.emptyMap();
          this.isBondOrder4 = bondOrder4;
      }
      public IFile file() {
          return file;
      }
      public int size() {
          return filePos.size()-1;
      }

      private long start(int index) {
          return filePos.get(index);
      }

      public static SDFileIndex emptyIndex() {
          return EMPTY_INDEX;
      }

      public long getPropertyStart(int index) {
          List<Long> propPos = propertiesPos.get(index);
          if(propPos.size()==0) return -1;

          return propertiesPos.get( index ).get( 0 );
      }

      public int getPropertyCount(int index) {
          List<Long> propPos = propertiesPos.get(index);
          return propPos!=null?propPos.size():0;
      }

      public String getRecord(int index) throws CoreException, IOException {
          InputStream in = file.getContents();
          long start = start( index );
          int length = (int) (start( index +1 )-start);
          in.skip( start );
          byte[] bytes= new byte[length];
          in.read( bytes , 0  , length );
          in.close();
          String result = new String( bytes );
          int i= -1;
          if((i=result.indexOf( "$$$$" ))!= -1)
              return result.substring( 0,i);
          return result;
      }
  }