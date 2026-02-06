package fr.edyp.epims.transfer.dataformat.bruker;

import fr.edyp.epims.transfer.model.AbstractAnalysis;
import fr.edyp.epims.transfer.model.DataFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class TimsTOFAnalysis extends AbstractAnalysis  {

  private TimsTOFFormat dataFormat;

  private final File dirFile;
  private List<File> allAcqFiles = new ArrayList<>();
  private File zipFile = null;
  private final String zipFileName;
  private boolean isInitialised = false;

  public TimsTOFAnalysis(File f, TimsTOFFormat format){
    dirFile = f;
    analysisFile = f;
    dataFormat = format;
    status = ANALYSIS_STATUS_UNKNOWN;
    setName(FilenameUtils.getBaseName(dirFile.getName()));
    zipFileName = dirFile.getName()+".zip";
    determineType();
    try {
      initFile();
    } catch (Exception e) {
      throw new RuntimeException("Error Creating timsTOF Analysis "+f.getName());
    }
  }

  public File getParentDirFile(){
    return dirFile;
  }

  @Override
  public File getFileToTransfer() {
    if(zipFile == null ) {
      try {
        if(!isInitialised)
          initFile();
        if (!allAcqFiles.isEmpty()) {
          zipFile = new File(dirFile.getParentFile(),dirFile.getName()+".zip");
          boolean zipCreateSucess = fr.edyp.epims.transfer.util.FileUtils.copyFilesToOneZip(zipFile, allAcqFiles, dirFile, true);
          if(zipCreateSucess) {
            estimatedSize = zipFile.length();
          } else {
            zipFile = null;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return zipFile;
  }

  @Override
  public boolean isTransferFileTempo() {
    return true;
  }


  private void initFile()  {

    allAcqFiles =new ArrayList<>( FileUtils.listFiles(dirFile, null, true));//Arrays.asList(dirFile.listFiles());
    if(!allAcqFiles.isEmpty()){
      estimatedSize = 0;
      for (File nextF : allAcqFiles){
        estimatedSize += nextF.length();
      }
    }
    isInitialised = true;
  }

  @Override
  public String getFileName() {
   return zipFileName;
  }

  // Use to accept File from getFile() : zipFile
  @Override
  public FileFilter getContentFilter() {
    return new TimsTOFFormat.TimsTOFFileFilter();
  }

  @Override
  public File[] getAssociatedFiles() {
    return new File[0];
  }

  @Override
  public String getAssociatedFileType(File associatedFile) {
    return null;
  }

  @Override
  public void setDataFormat(DataFormat dataFormat) {
    if(dataFormat instanceof TimsTOFFormat)
        this.dataFormat = (TimsTOFFormat) dataFormat;
  }

}
