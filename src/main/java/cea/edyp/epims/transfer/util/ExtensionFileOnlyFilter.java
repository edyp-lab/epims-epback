/*
 * Created on Nov 26, 2004
 *
 * $Id: ExtensionFileOnlyFilter.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * 
 * @author CB205360
 */
public class ExtensionFileOnlyFilter implements FileFilter {

  private String[] extensions;

  public ExtensionFileOnlyFilter(String[] ext) {
    extensions = ext;
  }

  public boolean accept(File file) {
    if (file.isDirectory())
      return false;
    
    String extension = FileUtils.getExtension(file);
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
