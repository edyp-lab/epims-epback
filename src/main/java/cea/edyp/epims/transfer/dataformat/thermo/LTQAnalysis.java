/*
 * Created on Dec 7, 2004
 *
 * $Id: LTQAnalysis.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.thermo;

import java.io.File;
import java.io.FileFilter;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import org.apache.commons.io.FilenameUtils;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.util.FileUtils;

/**
 * 
 * 
 * @author CB205360
 */
public class LTQAnalysis extends AbstractAnalysis {

  private static final String MGF_EXTENTION = "mgf";
  private static final String MGF_DIR = "dta";
  
  private LTQFormat dataFormat;

  public LTQAnalysis(File f, LTQFormat format) {
    analysisFile = f;
    estimatedSize = analysisFile.length();
    status = ANALYSIS_STATUS_UNKNOWN;
    dataFormat = format;
    analyseType = AnalysisType.UNKNOWN;
    setName( FilenameUtils.getBaseName(analysisFile.getName()));
  }
  
  public void setDataFormat(DataFormat format){
  	if(format instanceof LTQFormat)
  		dataFormat = (LTQFormat)format;  
  }

  @Override
  public String getName(){
    if(name == null && analysisFile!=null)
      setName(FilenameUtils.getBaseName(analysisFile.getName()));
    return name;
  }

  @Override
  public long getEstimatedSize() {
    if(estimatedSize == 0L && analysisFile!=null)
      estimatedSize = analysisFile.length();
    return estimatedSize;
  }

  public File getFile() {
    return analysisFile;
  }

  public String getFileName(){
    return analysisFile.getName();
  }

  public FileFilter getContentFilter() {
    return new LTQFilter();
  }

  public File[] getAssociatedFiles() {
    if(dataFormat.areSpectraHandled() && analyseType != Analysis.AnalysisType.BLANK &&
        analyseType  != Analysis.AnalysisType.CONTROL_INSTRUMENT 
        && analyseType  != Analysis.AnalysisType.CONTROL_LC){
    	
    	if(associatedFiles != null)
    		return associatedFiles.toArray(new File[0]);
    	
    	File dir = analysisFile.getParentFile();      
      StringBuilder nameSB = new StringBuilder(MGF_DIR);
      nameSB.append(File.separator);
      nameSB.append(getName());
      nameSB.append(".");
      nameSB.append(MGF_EXTENTION);
      
      File mgfFile = new File(dir, nameSB.toString());
      if(mgfFile.exists() && mgfFile.canRead()){
        associatedFiles.add(mgfFile);
        return associatedFiles.toArray(new File[0]);
      }
      
      //Try with UpperCase
      nameSB = new StringBuilder(MGF_DIR.toUpperCase());
      nameSB.append(File.separator);
      nameSB.append(getName());
      nameSB.append(".");
      nameSB.append(MGF_EXTENTION.toUpperCase());
      mgfFile = new File(dir, nameSB.toString());
      if(mgfFile.exists() && mgfFile.canRead()){
        associatedFiles.add(mgfFile);
        return associatedFiles.toArray(new File[0]);
      }
    }
    
    return new File[0];
  }

  public String getAssociatedFileType(File associatedFile) {
    if(!dataFormat.areSpectraHandled())
      return null;
    
    String ext = FileUtils.getExtension(associatedFile); 
    if(ext == null || !ext.equalsIgnoreCase(MGF_EXTENTION)){
      return null;
    }
    
    return SPECTRA_FILETYPE;
  }

  public String toString() {
    return getName();
  }

  class LTQFilter implements FileFilter {

    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname) {
      return pathname.equals(LTQAnalysis.this.analysisFile);
    }
  }

}
