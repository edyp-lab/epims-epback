/*
 * Created on Dec 7, 2004
 *
 * $Id: QTrapAnalysis.java,v 1.1 2008-02-20 06:56:22 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.applied;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Date;


/**
 * 
 * 
 * @author CB205360
 */
//TODO Use AbstractAnalysis
public class QTrapAnalysis extends AbstractAnalysis {

  private static final String SPECTRA_DIR = "mgf";
  private static final String SPECTRA_EXT = "mgf";

  protected QTrapFormat dataFormat;


  public QTrapAnalysis(File f, QTrapFormat format) {
    analysisFile = f;
    init(format);
  }
  
  public QTrapAnalysis(String filePath, QTrapFormat format) {
    analysisFile = new File(filePath);
    init(format);
  }
    
  public void setDataFormat(DataFormat format){
  	if(format instanceof QTrapFormat)
  		dataFormat = (QTrapFormat)format;  
  }
   
  protected void init(QTrapFormat format){
    status = ANALYSIS_STATUS_UNKNOWN;
    setDataFormat(format);
    name = analysisFile.getName().substring(0,analysisFile.getName().lastIndexOf('.'));
    acqDate = new Date(analysisFile.lastModified());
    estimatedSize = analysisFile.length();
    determineType();
  }

  protected void determineType() {
    if (CTRL_INST_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_INSTRUMENT;
    else if (CTRL_LC_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_LC;
    else if (BLANK_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.BLANK;
    else if (TEST_ANALYSIS_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.TEST;
    else if (CYTC_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_LC; 
    else if (BETAGAL_CODE.equalsIgnoreCase(sample))
       analyseType = Analysis.AnalysisType.CONTROL_LC; 
    else if (sample == null || sample.trim().isEmpty())
      analyseType = Analysis.AnalysisType.UNKNOWN;
    else
      analyseType = Analysis.AnalysisType.RESEARCH;
  }

  public File getFile() {
    return analysisFile;
  }

  public String getFileName(){
    return analysisFile.getName();
  }
  
  public FileFilter getContentFilter() {
    return new QTrapFilter();
  }

  public File[] getAssociatedFiles() {
  	if (dataFormat.areSpectraHandled()) {

  		if (analyseType == Analysis.AnalysisType.BLANK
	  || analyseType == Analysis.AnalysisType.CONTROL_INSTRUMENT
	  || analyseType == Analysis.AnalysisType.CONTROL_LC
	  || analyseType == Analysis.AnalysisType.TEST) {
  			return new File[0];
  		}
  		
  		if(associatedFiles != null)
  			return associatedFiles.toArray(new File[0]);

  		File dir = analysisFile.getParentFile();
  		File[] assocFilesDir = dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name) {
					if(name.equalsIgnoreCase(SPECTRA_DIR))
						return true;
					return false;
      }});
  		  		
  		if(assocFilesDir != null && assocFilesDir.length >= 1){
        associatedFiles = Arrays.asList(assocFilesDir[0].listFiles(new FilenameFilter(){
  				public boolean accept(File dir, String name) {
  					StringBuffer nameSB = new StringBuffer(getName());
  		      nameSB.append(".");
  		      nameSB.append(SPECTRA_EXT);
  					String searchedFile = nameSB.toString();
  				
  					if(name.equalsIgnoreCase(searchedFile))
  						return true;
  					return false;
  				}}));



  			if (associatedFiles != null) {
  				return associatedFiles.toArray(new File[0]);
  			}
  		}
    }
    return new File[0];
  }

  public String getAssociatedFileType(File associatedFile) {

    if (!dataFormat.areSpectraHandled())
      return null;

    String ext = FileUtils.getExtension(associatedFile);
    if (ext == null || (!ext.equalsIgnoreCase(SPECTRA_EXT))) {
      return null;
    }

    return SPECTRA_FILETYPE;
  }

   public String toString() {
    return getName();
  }


  class QTrapFilter implements FileFilter {

    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
  	public boolean accept(File pathname) {
      return pathname.equals(QTrapAnalysis.this.analysisFile);
    }
  }
}
