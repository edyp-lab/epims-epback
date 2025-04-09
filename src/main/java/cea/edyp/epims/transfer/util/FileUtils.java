/*
 * Created on Nov 26, 2004
 *
 * $Id: FileUtils.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */ 
public class FileUtils {

  private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

  public static boolean copyFilesToOneZip(File destFile, List<File> srcFiles){
    return FileUtils.copyFilesToOneZip(destFile, srcFiles, null, false);
  }

	public static boolean copyFilesToOneZip(File destFile, List<File> srcFiles, File srcParent, boolean keepRelativePath){
		File currentFile;
		ZipOutputStream zipOutput;
		String fileName = destFile.getName();
		try {
	    
		    Long start = System.currentTimeMillis();
		    logger.debug("******** Create ZIP "+fileName);
		    zipOutput = new ZipOutputStream(new FileOutputStream(destFile));
		
		    // Create a buffer for reading the files
		    byte[] buf = new byte[2048];    
		      
		    // Compress the files
		    for(int j=0; j<srcFiles.size(); j++) {
		    	currentFile = srcFiles.get(j);    
		    	FileInputStream inputStream = new FileInputStream(currentFile);
		    	logger.info("File to add to zip : "+currentFile.getName());
          String zipEntry =currentFile.getName();
          if(keepRelativePath)
            zipEntry = srcParent!=null ? srcParent.toURI().relativize(currentFile.toURI()).getPath() : destFile.toURI().relativize(currentFile.toURI()).getPath();
		    	// Add ZIP entry to output stream.
		    	zipOutput.putNextEntry(new ZipEntry(zipEntry));
		    
		    	// Transfer bytes from the file to the ZIP file
		    	int len;
		    	while ((len = inputStream.read(buf)) > 0) {
		    		zipOutput.write(buf, 0, len);
		    	}
		    
		    	// Complete the entry
		    	zipOutput.closeEntry();
		    	inputStream.close();
		    }//END of for all files
		      
		    // Complete the ZIP file
		    zipOutput.close();
		    Long stop = System.currentTimeMillis();
		    logger.debug("******** STOP Create ZIP "+fileName+" duration (ms) " +(stop-start));
		    		    
		    ZipInputStream zis = new ZipInputStream(Files.newInputStream(destFile.toPath()));
		    int nbrEntries  =0;
		    while(zis.getNextEntry() != null){
		    	nbrEntries++;
		    }
		    zis.close();
        if (nbrEntries!= srcFiles.size()) {
          
          logger.debug(" Erreur de création du zip! ");
          destFile.delete();
          throw new IOException("Erreur de création du zip "+fileName);
       }
        Long stop2 = System.currentTimeMillis();
        logger.debug("******** STOP VERIFY ZIP "+fileName+" duration (ms) "+(stop2-stop));

	    } catch (IOException e) {
	    	if(destFile.exists())
	    		destFile.delete();
	    	logger.error("Error while compacting file into a zip. Trace : "+e);
	    	return false;
	    }
			destFile.setLastModified(srcFiles.get(0).lastModified());   
	    return true;

	}

   public static void secureCopy(File src, File dest) throws FileNotFoundException, IOException {

      if (src.isDirectory()) {
         dest.mkdir();
         File[] content = src.listFiles();
         for (int i = 0; i < content.length; i++) {
            File f = content[i];
            File d = new File(dest, f.getName());
            logger.debug(" copy file "+f.getName());
            secureCopy(f, d);
         }

      } else {
        doSecureCopy(src,dest);
      }
   }

  public static void secureCopy(File src, File dest, FileFilter filter) throws FileNotFoundException, IOException {
      if (src.isDirectory()&& filter.accept(src) ) {
         dest.mkdir();
         File[] content = src.listFiles(filter);
         for (int i = 0; i < content.length; i++) {
            File f = content[i];
            File d = new File(dest, f.getName());
            logger.debug(" copy file "+f.getName());
            secureCopy(f, d, filter);
         }

      } else if(filter.accept(src)) {
        doSecureCopy(src, dest);
      }
   }

   private static void doSecureCopy(File src, File dest) throws IOException {
     FileInputStream in = new FileInputStream(src );
     CheckedInputStream cin = new CheckedInputStream(in, new Adler32());
     FileOutputStream out = new FileOutputStream(dest);
     CheckedOutputStream cout = new CheckedOutputStream(out, new Adler32());
     byte[] buf = new byte[64*1024];
     int c;
     while ((c = cin.read(buf)) != -1)
       cout.write(buf, 0, c);

     if (cin.getChecksum().getValue() != cout.getChecksum().getValue()) {
       cin.close();
       cout.close();
       logger.debug(" Erreur de checksum sur la copie du fichier");
       throw new IOException("Erreur de checksum sur la copie du fichier "+src.getName());
     }
     cin.close();
     cout.close();

     if (src.length() != dest.length()) {
       logger.debug(" Erreur de taille du fichier");
       throw new IOException("Erreur de taille du fichier copie :"+src.length()+" attendu, "+dest.length()+" constaté");
     }
   }

  public static String getExtension(File f) {
     String ext = FilenameUtils.getExtension(f.getName());
     return ext.toLowerCase();
  }
  
  /**
   * Function recursive that retrieve all the files contained in a directories and
   * subdirectories
   * @param files : the results of the scan
   * @param dir: the dir we want to scan
   * @author vbouquet
   *
   * @deprecated use org.apache.commons.io.FileUtils.listFiles
   */
  public static void getFilesFromDirectory(List<File> files, File dir){
	  //Stop condition
	  if(dir.isFile()){
		  files.add(dir);
	  }else{
		  //for each file.dir in the dir apply the same function
		  for(int i=0; i<dir.listFiles().length; i++){
			  getFilesFromDirectory(files, dir.listFiles()[i]);
		  }
	  }
  }
  
}
