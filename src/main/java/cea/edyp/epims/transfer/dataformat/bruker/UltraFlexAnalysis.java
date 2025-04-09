package cea.edyp.epims.transfer.dataformat.bruker;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.util.FileUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An UltraFlex analysis doesn't stand for just one file but for a list of directories corresponding
 * of all the replicates made during the acquisition.
 * There could be 1 to 4 replicates.
 * To respect the eP-Back model, we have to put all the directories in a main directory witch is the
 * analysis file.<br>
 * <br>
 * An UltraFlex analysis got 4 specific attributes:<br>
 * <li> the number of shoots
 * <li> the number of replicates
 * <li> the type of acquisition MS or MSMS
 * <li> the mass of parent ion in MSMS acquisition
 * <br>
 * <p>
 * UltraFlexAnalysis implements Analysis andPropertyChangeListener
 * <br>
 *
 * @author vbouquet
 */
public class UltraFlexAnalysis extends AbstractAnalysis implements PropertyChangeListener {

  //declare the fileFilter
  private static final BrukerFileFilter Filefilter = new BrukerFileFilter();

  //we have to declare the Bruker format
  private UltraFlexFormat dataFormat;

  //declare the specific attributes
  private String autoXMethod;
  private int nbReplicat;
  private String nbShoots;
  private String acqType;
  private String ionParentMasse;
  private int nbAcqPerReplicate;


  //An ultraFlex analysis could have 1 to 4 directories (according to replicates)
  private File[] dataFileList;
  //attribut to indicate if the zip has run
  private String dataFileState;


  ////////////////
  //constructeur//
  ////////////////

  /**
   * Create a UltraFlexAnalysis with a list of directories corresponding of replicates
   * and a UltraFlexFormat
   */
  public UltraFlexAnalysis(File[] fileList, UltraFlexFormat format) {
    status = ANALYSIS_STATUS_UNKNOWN;
    dataFormat = format;
    analyseType = Analysis.AnalysisType.UNKNOWN;
    estimatedSize = 0;
    duration = null;
    description = "";
    nbShoots = "0";
    sample = "";

    //this properties contains the list of replicat paths
    dataFileList = fileList;
  }

  public void setDataFormat(DataFormat format) {
    if (format instanceof UltraFlexFormat) {
      dataFormat = (UltraFlexFormat) format;
      associatedFiles = null;
    }
  }


  public String getIonParentMasse() {
    return ionParentMasse;
  }

  public void setIonParentMasse(String ionParentMasse) {
    this.ionParentMasse = ionParentMasse;
  }

  public int getNbReplicat() {
    return nbReplicat;
  }

  public void setNbReplicat(int nbReplicat) {
    this.nbReplicat = nbReplicat;
    this.addDescription(" " + nbReplicat + " replicat(s),");
  }

  public String getAutoXMethod() {
    return autoXMethod;
  }

  public void setAutoXMethod(String autoXMethod) {
    this.autoXMethod = autoXMethod;
    this.addDescription(" AXMeth: " + autoXMethod);
  }

  public void addDescription(String info) {
    this.description = description + info;
  }


  public long getEstimatedSize() {
    if (estimatedSize == 0) {
      for (int i = 0; i < dataFileList.length; i++) {
        estimatedSize += dataFileList[i].length();
      }
    }
    return estimatedSize;
  }

  // TODO faire un getFile ou les réplicats d'acquisition d'une analyse sont dans un même répertoire
  //le getFile est � la fois pour les analyses MS que MSMS sans r�pliqua

  /**
   * This method has the role to create a unique zip file, witch will be transfered, <br>
   * with the list of files in the property dataFileList<br>
   * <br>
   * The returned file is a zip, to create it:
   * <li>check that all the files in dataFileList exist
   * <li>getting all files, but not directories because ZipOutputStream accepts just file
   * <li>get all the relative paths for zipentry
   * <li>add all in the ZipOutputStream and create zip file
   *
   * @author vbouquet
   */
  @Override
  public File getFile() {

    //declare tools
    ZipOutputStream zipOutput = null;
    File currentDir;
    boolean errorExists = false;
    //use this two list to store file and its zip entry
    List<File> listFileToZip = new ArrayList<File>();
    List<String> listEntryZipName = new ArrayList<String>();

    //use this list to retrieve the file from a directory
    List<File> files = new ArrayList<File>();

    //if analysisFile, this means that the zip oupput is not creadted yet
    if (analysisFile == null) {
      try {
        //TODO: check all the directories and files to be sure that the zip file will be created correctly
        logger.debug("Checking files before, and preparing the MAP");

        for (int j = 0; j < dataFileList.length; j++) {
          //in bruker analysis this currentFile is a directory
          //corresponding to replicates directory
          //TODO: changer la dataFileList en DataDirList
          currentDir = dataFileList[j];
          //if this dir doesn't exist
          if (!currentDir.exists()) {
            dataFileState = RSCS.getString("datafile.state.error");
            String msg = RSCS.getString("datafile.not.found");
            Object[] args = {currentDir};
            fileLogger.error(MessageFormat.format(msg, args));
            errorExists = true;
            throw new IOException(msg);
          } else if (!currentDir.isDirectory()) {
            dataFileState = RSCS.getString("datafile.state.error");
            String msg = "A element of the dataFileList is not a directory";
            Object[] args = {currentDir};
            fileLogger.error(MessageFormat.format(msg, args));
            errorExists = true;
            throw new IOException(msg);
          } else {//currentDir is a directory
            //get its files
            //warning empty the files list first, because used in the loop for
            files.clear();
            FileUtils.getFilesFromDirectory(files, currentDir);

            //get the absolute path, here becaus it's used below
            String dirPath = currentDir.getAbsolutePath();

            //add all the files add associated name in the Map
            for (int i = 0; i < files.size(); i++) {
              //get the file to add to the zip
              File curFile = files.get(i);
              String filePath = curFile.getAbsolutePath();
              //Warning, we must add to the zip entry a relatif path
              String zipEntryName = currentDir.getName() + filePath.substring(dirPath.length(), filePath.length());
              logger.debug("New zip entry: " + zipEntryName);
              //add zip entry in list
              listEntryZipName.add(zipEntryName);
              listFileToZip.add(files.get(i));
            }//end for
          }//end if
        }//end for dataFileList

        //now use the two list created upper to create the zipFile
        analysisFile = new File("temp/" + getFileName());
        zipOutput = new ZipOutputStream(new FileOutputStream(analysisFile));
        logger.debug("Creating File: " + analysisFile.getAbsolutePath());

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        //Add the file to the compress output
        for (int i = 0; i < listFileToZip.size(); i++) {
          //create the stream with the file in the file list
          FileInputStream inputStream = new FileInputStream(listFileToZip.get(i));
          //creat the zip entry
          zipOutput.putNextEntry(new ZipEntry(listEntryZipName.get(i)));
          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = inputStream.read(buf)) > 0) {
            zipOutput.write(buf, 0, len);
          }
          // Complete the entry
          zipOutput.closeEntry();
          inputStream.close();
        }
        zipOutput.close();

      } catch (FileNotFoundException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
        logger.error("Error while compacting file into a zip. Trace : " + e1);
        dataFileState = RSCS.getString("datafile.state.error");
        errorExists = true;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        logger.error("Error while compacting file into a zip. Trace : " + ioe);
        dataFileState = RSCS.getString("datafile.state.error");
        errorExists = true;
      }
    }//end if analysisFile==null

    if (errorExists) {
      try {
        zipOutput.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      logger.debug("Errors exist in zip protocol");
      logger.debug("deleting analysis file: " + analysisFile.delete());
      return null;
    } else {
      dataFileState = RSCS.getString("datafile.state.ok");
      return analysisFile;
    }
  }


  public String getAssociatedFileType(File associatedFile) {
    return SPECTRA_FILETYPE;
  }

  //There is no associated files for bruker analysis so return an empty list
  public File[] getAssociatedFiles() {
    return new File[0];
  }

  public void setAssociatedFiles() {
    associatedFiles = new ArrayList<File>();
  }

  public FileFilter getContentFilter() {
    return UltraFlexAnalysis.Filefilter;
  }

  //Warning it's a zip file
  public String getFileName() {
    return name + ".zip";
  }

  public void propertyChange(PropertyChangeEvent evt) {
    this.associatedFiles = null;
    if (evt.getNewValue() != null)
      setAssociatedFiles();
  }

  public String getAcqType() {
    return acqType;
  }

  public void setAcqType(String acqType) {
    this.acqType = acqType;
  }

  public String getNbShoots() {
    return nbShoots;
  }

  public void setNbShoots(String nbShoots) {
    this.nbShoots = nbShoots;
    this.addDescription(" " + nbShoots + " Shots,");
  }

  public int getNbAcqPerReplicate() {
    return nbAcqPerReplicate;
  }

  public void setNbAcqPerReplicate(int nbAcqPerReplicate) {
    this.nbAcqPerReplicate = nbAcqPerReplicate;
    this.addDescription(" " + nbAcqPerReplicate + " Acq/Rep,");
  }
}//end UltraFlexAnalysis class

class BrukerFileFilter implements FileFilter {

  public boolean accept(File file) {
    return (file.isDirectory() & !file.getName().equalsIgnoreCase("temp")) | (file.isFile() & !file.getName().equalsIgnoreCase("temp"));
  }
}
