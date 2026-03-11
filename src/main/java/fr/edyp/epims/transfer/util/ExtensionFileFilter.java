/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
/*
 * Created on Nov 26, 2004
 *
 * $Id: ExtensionFileFilter.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * 
 * @author CB205360
 */
public class ExtensionFileFilter implements FileFilter {

   private final String[] extensions;

   public ExtensionFileFilter(String[] ext) {
      extensions = ext;
   }

   public boolean accept(File file) {
      String extension = FileUtils.getExtension(file);
      if (extension != null) {
         return isAcceptable(extension);
      }
      return false;
   }

   public boolean isAcceptable(String extension) {
     for (String s : extensions) {
       if (extension.equals(s))
         return true;
     }
      return false;
   }

}
