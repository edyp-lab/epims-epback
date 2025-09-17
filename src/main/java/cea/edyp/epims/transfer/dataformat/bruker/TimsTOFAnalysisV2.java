package cea.edyp.epims.transfer.dataformat.bruker;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import cea.edyp.epims.transfer.model.DataFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class TimsTOFAnalysisV2 extends AbstractAnalysis  {

  private TimsTOFFormatV2 dataFormat;

  private final File dirFile;
  private List<File> allAcqFiles = new ArrayList<>();
  private File zipFile = null;
  private String zipFileName;
  private boolean isInitialised = false;

  public TimsTOFAnalysisV2(File f, TimsTOFFormatV2 format){
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
  public File getFile() {
    if(zipFile == null ) {
      try {
        if(!isInitialised)
          initFile();
        if (!allAcqFiles.isEmpty()) {
          zipFile = new File(dirFile.getParentFile(),dirFile.getName()+".zip");
          boolean zipCreateSucess = cea.edyp.epims.transfer.util.FileUtils.copyFilesToOneZip(zipFile, allAcqFiles, dirFile, true);
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

  public File getSourceFile() {
    return dirFile;
  }

  @Override //Zip file should be removed
  public boolean removeTemporaryZipFile() {
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
    return new TimsTOFFormatV2.TimsTOFFileFilter();
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
    if(dataFormat instanceof TimsTOFFormatV2 )
        this.dataFormat = (TimsTOFFormatV2) dataFormat;
  }

}
