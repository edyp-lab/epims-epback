/*
 * Created on Dec 7, 2004
 *
 * $Id: MLAnalysis.java,v 1.1 2007-09-14 09:37:31 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.waters;

import cea.edyp.epims.transfer.model.AbstractAnalysis;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.util.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * 
 * 
 * @author CB205360
 */
public class MLAnalysis extends AbstractAnalysis implements PropertyChangeListener {

   private static final MLFilter Filefilter = new MLFilter();
   private MLFormat dataFormat;

   public MLAnalysis(File f, MLFormat format) {
      analysisFile = f;
      status = ANALYSIS_STATUS_UNKNOWN;
      analyseType = Analysis.AnalysisType.UNKNOWN;
      dataFormat = format;
      format.addPropertyChangeListener(MLFormat.PKL_PATH, this);
      estimatedSize = -1;
      duration = null;
      name= FilenameUtils.getBaseName(analysisFile.getName());
   }

  public void setDataFormat(DataFormat format){
  	if(format instanceof MLFormat){
  		dataFormat = (MLFormat) format;
  		associatedFiles = null;
  	}
  }


  public long getEstimatedSize() {
  	if (estimatedSize < 0) {
  		//  	Get size
      estimatedSize = MLRawInfo.getAnalysisSize(this);
  	}
     return estimatedSize;
  }

   public File getFile() {
      return analysisFile;
   }

   public String getFileName(){
     return analysisFile.getName();
   }

   public FileFilter getContentFilter() {
      return Filefilter;
   }

   public File[] getAssociatedFiles() {
     if(dataFormat.areSpectraHandled()){
    	 
    	 if(analyseType == Analysis.AnalysisType.BLANK || analyseType  == Analysis.AnalysisType.CONTROL_INSTRUMENT 
    			 || analyseType  == Analysis.AnalysisType.CONTROL_LC){
    		 return new File[0];
    	 }
    	 
    	 //	don't search again 
    	 if(associatedFiles != null){
    		 return associatedFiles.toArray(new File[0]);
    	 } else {
    		 setAssociatedFiles();

         return associatedFiles.toArray(new File[0]);
    	 }
     } else { 
       return new File[0]; 
     }
   }
   
   private void setAssociatedFiles(){
  	 associatedFiles = new ArrayList<>();
  	 File pkl = getPKL();
  	 File prp = getPRP();
  	 
  	 if (pkl != null)
  		 associatedFiles.add(pkl);
  	 if(prp != null)
  		 associatedFiles.add(prp);
   }

   public String getAssociatedFileType(File associatedFile) {
     
     if(!dataFormat.areSpectraHandled())
    	 return null;
      
     String ext = FileUtils.getExtension(associatedFile); 
     if(ext == null || (!ext.equalsIgnoreCase("prp") && !ext.equalsIgnoreCase("pkl"))){
       return null;
     }
     
      return SPECTRA_FILETYPE;
   }

   public Float getDuration() {   
     //Get duration
  	 if (duration == null) {
  		 duration = (float) MLRawInfo.getAnalysisDuration(this);
  	 }
     return duration;
   }

   public File getPKL() {
      File pklFile = new File(dataFormat.getPKLPath(), getName()+".pkl");
      if (pklFile.exists() && pklFile.canRead())
         return pklFile;
      return null;
   }
   
   public File getPRP() {
     File prpFile = new File(analysisFile.getParentFile(), getName()+".prp");
     if (prpFile.exists() && prpFile.canRead())
        return prpFile;
     return null;
   }
   
   public String toString(){
     return getName();
   }

	public void propertyChange(PropertyChangeEvent evt) {
		this.associatedFiles = null;
		if(evt.getNewValue() != null)
			setAssociatedFiles(); 
	}

	static class MLFilter implements FileFilter {
    private static final String REJECT_PREFIX = "_PROC";

    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname) {
      if (pathname.isDirectory())
        return true;

      String fileName = pathname.getName();
      return !fileName.startsWith(REJECT_PREFIX);
    }
  }
}

