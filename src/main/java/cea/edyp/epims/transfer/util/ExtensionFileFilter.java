/*
 * Created on Nov 26, 2004
 *
 * $Id: ExtensionFileFilter.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * 
 * @author CB205360
 */
public class ExtensionFileFilter implements FileFilter {

   private String[] extensions;

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
      for (int i = 0; i < extensions.length; i++) {
         if (extension.equals(extensions[i]))
            return true;
      }
      return false;
   }

}
