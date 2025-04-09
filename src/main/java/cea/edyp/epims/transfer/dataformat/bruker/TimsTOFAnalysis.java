package cea.edyp.epims.transfer.dataformat.bruker;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.MultiFilesAnalysis;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimsTOFAnalysis extends AbstractAnalysis  implements MultiFilesAnalysis {

  private TimsTOFFormat dataFormat;

  private final File dirFile;
  private List<File> allAcqFiles;
  private File zipFile = null;


  public TimsTOFAnalysis(File f, TimsTOFFormat format){
    dirFile = f;
    analysisFile = f;
    dataFormat = format;
    status = ANALYSIS_STATUS_UNKNOWN;
    setName(FilenameUtils.getBaseName(dirFile.getName()));
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
      return analysisFile;
  }

  private void initFile()  {

    allAcqFiles =new ArrayList<>( FileUtils.listFiles(dirFile, null, true));//Arrays.asList(dirFile.listFiles());
    if(!allAcqFiles.isEmpty()){
      estimatedSize = 0;
      for (File nextF : allAcqFiles){
        estimatedSize += nextF.length();
      }
      zipFile = new File(dirFile.getParentFile(),dirFile.getName()+".zip");
    }
  }

  public List<File> getAllAcquisitionFile(){
    if(allAcqFiles ==null) {
      initFile();
    }
    return allAcqFiles;
  }

  public boolean keepRelativePath(){
    return true;
  }

  @Override
  public String getFileName() {
    if(zipFile == null)
      return analysisFile.getName();
    else
      return zipFile.getName();
  }

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
    if(dataFormat instanceof TimsTOFFormat )
        this.dataFormat = (TimsTOFFormat) dataFormat;
  }

  public void setDestination(String destinationDir) {
    super.setDestination(destinationDir);
    zipFile = new File(destinationDir,analysisFile.getName()+".zip");
  }

}
