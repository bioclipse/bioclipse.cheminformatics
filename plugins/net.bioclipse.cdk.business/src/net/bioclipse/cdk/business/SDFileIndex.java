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
package net.bioclipse.cdk.business;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

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

      SDFileIndex(final IFile file,final List<Long> filePos) {
          this.file = file;
          this.filePos=new ArrayList<Long>(filePos);
          this.filePos.add( 0, 0l );
      }

      public IFile file() {
          return file;
      }
      public int size() {
          return filePos.size()-1;
      }

      public long start(int index) {
          return filePos.get(index);
      }
      
      public static SDFileIndex emptyIndex() {
          return EMPTY_INDEX;
      }
  }