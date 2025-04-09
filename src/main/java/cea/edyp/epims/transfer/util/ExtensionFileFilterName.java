/*
 * Created on Nov 26, 2004
 *
 * $Id: ExtensionFileFilterName.java,v 1.1 2008-02-20 07:01:10 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * 
 * @author CB205360
 */
public class ExtensionFileFilterName implements FilenameFilter {

  private String[] extensions;

  public ExtensionFileFilterName(String[] ext) {
    extensions = ext;
  }

  public boolean accept(File file, String name) {
    int dotIndex = name.lastIndexOf('.');
    if(dotIndex == -1)
      return false;
    String extension = name.substring(dotIndex+1);
    if (extension != null) {
      return isAcceptable(extension);
    }
    return false;
  }

  public boolean isAcceptable(String extension) {
    for (int i = 0; i < extensions.length; i++) {
      if (extension.equals(extensions[i]))
        return true;
    }
    return false;
  }

}
