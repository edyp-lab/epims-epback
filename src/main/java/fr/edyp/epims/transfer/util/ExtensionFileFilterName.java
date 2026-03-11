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
 * $Id: ExtensionFileFilterName.java,v 1.1 2008-02-20 07:01:10 dupierris Exp $
 */
package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * 
 * @author CB205360
 */
public class ExtensionFileFilterName implements FilenameFilter {

  private final String[] extensions;

  public ExtensionFileFilterName(String[] ext) {
    extensions = ext;
  }

  public boolean accept(File file, String name) {
    int dotIndex = name.lastIndexOf('.');
    if(dotIndex == -1)
      return false;
    String extension = name.substring(dotIndex+1);
    return isAcceptable(extension);
  }

  public boolean isAcceptable(String extension) {
    for (String s : extensions) {
      if (extension.equals(s))
        return true;
    }
    return false;
  }

}
