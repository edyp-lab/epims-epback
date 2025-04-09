/*
 * Created on Dec 7, 2004
 *
 * $Id: Analysis.java,v 1.2 2008-02-20 06:59:03 dupierris Exp $
 */
package cea.edyp.epims.transfer.model;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

/**
 * 
 * 
 * @author CB205360
 */
public interface Analysis {

  String ANALYSIS = "analysis";
  String SPECTRA_FILETYPE = "spectra";
   
   
  String UNKNOWN_SAMPLE_CODE="old";

  String CTRL_INST_CODE= "ControleInst";
  String CTRL_LC_CODE = "ControleLC";
  String BLANK_CODE = "Blanc";
  String CYTC_CODE = "cytc";
  String BETAGAL_CODE = "bgal";
  String TEST_ANALYSIS_CODE="Test";
  
  /**
   * Available analysis types 
   */
	enum AnalysisType {
		UNKNOWN,
		RESEARCH,
		CONTROL_INSTRUMENT,
		CONTROL_LC,
		BLANK,
		TEST
  }

  /**
  *  Specify that the analysis file is unreadable : corrupted or in acquisition ....
  */
 int ANALYSIS_STATUS_NOT_READABLE = -2;

  /**
   *  Specify that the analysis status is unknown, BackPimsUtil.getAnalysisStatus
   *  method should be called.
   */
   int ANALYSIS_STATUS_UNKNOWN = -1;

   /**
    * Specify that this analysis is OK, no error status mask set
    */
   int ANALYSIS_OK_MASK = 0;

   /**
    * Status mask (status & this_mask = this_mask if set) set if
    * this analysis already exist in Pims. Analysis equality is done
    * on acquisition name.
    */
   int ANALYSIS_EXIST_MASK = 1;
   
   /**
    * Status mask (status & this_mask = this_mask if set) set if
    * this analysis is associated to a closed study 
    */  
   int ANALYSIS_STUDY_CLOSED_MASK = 2;

 
   /**
    * Status mask (status & this_mask = this_mask if set) set if
    * the sample this analysis is associated to is invalid (not defined
    * in Pims)
    */
   int ANALYSIS_INVALID_SAMPLE_MASK = 4;
   
   static boolean isAnalysisSaved(int status){
    return ((status == Analysis.ANALYSIS_STATUS_UNKNOWN)
            || (status & Analysis.ANALYSIS_EXIST_MASK) == Analysis.ANALYSIS_EXIST_MASK);
   }

  static boolean isAnalysisUnsaved(int status){
   return ((status == Analysis.ANALYSIS_STATUS_UNKNOWN) || (status == Analysis.ANALYSIS_STATUS_NOT_READABLE)
          || (status & Analysis.ANALYSIS_EXIST_MASK) != Analysis.ANALYSIS_EXIST_MASK);
  }

 static boolean isAnalysisOK(int status){
  return (status == Analysis.ANALYSIS_OK_MASK);
 }

 // return true if Analysis has status mask set for ANALYSIS_STUDY_CLOSED_MASK or ANALYSIS_INVALID_SAMPLE_MASK
 static boolean isAnalysisIncorrect(int status){
  return (status & Analysis.ANALYSIS_STUDY_CLOSED_MASK) == Analysis.ANALYSIS_STUDY_CLOSED_MASK
          || (status & Analysis.ANALYSIS_INVALID_SAMPLE_MASK) == Analysis.ANALYSIS_INVALID_SAMPLE_MASK;
 }


   /**
    * Return an Estimated size of the analysis file.  
    *  
    * @return the estimated size of this analysis file.
    */
   long getEstimatedSize();
   
   /**
    * Set an Estimated size of the analysis file.  
    *  
    */
   void setEstimatedSize(long size);
   
   /**
    * Return the name of this analysis
    * 
    * @return the name of this analysis
    */
   String getName();
   
   /**
    * Set the name of this analysis
    *
    */
   void setName(String name);
   
   /**
    * Return the sample ID this analysis correspond to.
    * The sample ID could be null. 
    * 
    * @return the sample ID this acquisition was done on
    */   
    String getSample();
   
   
   /**
    * Specify the sample corresponding to the analysis
    * 
    * @param sample the Sample this analysis was done on
    */
   void setSample(String sample);
      
   /**
    * Return the analysis file, which could be a directory.
    * 
    * @return
    */
   File getFile();

   /**
    * Must return true if there is a temporary zip file to delete
    * @return
    */
   boolean removeTemporaryZipFile();
   
   /**
    * Return the analysis file name.
    * 
    * @return
    */
   String getFileName();
   
   /**
    * Return the path to the directory on ePims system where the analysis file associated
    * to this Analysis should be saved. This path is relative to ePims root.
    * 
    * @return
    */
   String getDestination();
   
   /**
    * set the directory destination path on ePims system where the analysis file associated
    * to this Analysis should be saved. The path should be relative to ePims root.
    * 
    * @return
    */
   void setDestination(String destinationDir);
   
   /**
    * Return the contentFilter to get only valid files for this analysis.
    *  
    * @return a FileFilter to get only valid files from directory of this analysis.
    */
   FileFilter getContentFilter();
 
 
  /**
   * Return the type of the analysis. The type is one of AnalysisType value
   * @return AnalysisType
   */
  AnalysisType getType();

   void setType(AnalysisType type);
   
   /**
    * Return the status of the Analysis. This status is a combination of
    * <UL>
    * <LI><code> ANALYSIS_STATUS_UNKNOWN</code> if analysis status has not been set. To set 
    * analysis status <code	IEPSystemDataProvider.getAnalysisStatus()</code> method should be called.</LI>
    * <LI><code> ANALYSIS_OK</code> if analysis is not saved in ePims and is correct</LI>
    * <LI><code> ANALYSIS_EXIST_MASK</code> if analysis is already saved in ePims</LI> 
    * <LI><code> ANALYSIS_STUDY_CLOSED</code> if analysis is associated to a closed study
    * <LI><code> ANALYSIS_INVALID_SAMPLE_MASK</code> if associated sample is invalid (not found in ePims)</LI> 
    * </UL> 
    */
   int getStatus();
   
   /**
    * Set the Analysis status. The specified status should be <code>ANALYSIS_STATUS_UNKNOWN</code>
    * or a combination (using '|') of
    * <ul>
    * <LI><code> ANALYSIS_OK</code> if analysis is not saved in ePims and is correct</LI>
    * <LI><code> ANALYSIS_EXIST_MASK</code> if analysis is already saved in ePims</LI> 
    * <LI><code> ANALYSIS_STUDY_CLOSED</code> if analysis is associated to a closed study
    * <LI><code> ANALYSIS_INVALID_SAMPLE_MASK</code> if associated sample is invalid (not found in ePims)</LI> 
    *  </ul>
    *  
    * @param status Analysis status
    * @see <code>IEPSystemDataProvider.getAnalysisStatus()</code> method
    */
   void setStatus(int status);
  
   
   /**
    * Return an array of all the files associated to this Analysis, if
    * the analysis has been configured to show its analysis files.
    * The array may be empty if there is no file associated to this 
    * analysis or if they are hidden.
    * 
    * @return an Array of all the files associated to this Analysis or 
    * an empty array if this analysis has no associated file or if they are hidden.
    */
   File[] getAssociatedFiles();
   
   /**
    * Return a String specifying the type of the file associated to
    * this Analysis. If specified file isn't valid for this analysis
    * null is returned.
    *  
    * @param associatedFile The file associated to this Analysis and to look
    * the type for
    * @return String representation of the specified associated file or null
    * if not applicable to the file.
    */
   String getAssociatedFileType(File associatedFile);
  
   /**
    * Return the analysis creation date
    *  
    * @return the analysis creation date
    */
   Date getDate();
   
   void setDate(Date date);
   
   /**
    * Return the name of the person who run the 
    * analysis.
    * 
    * @return the user name or null if none was specified
    */
   String getOperator();
   
   void setOperator(String operator);

   
   /**
    * Return the description of the analysis.
    * 
    * @return the analysis description or null if none was specified
    */
   String getDescription();
   
   void setDescription(String desc);
   
   /**
    * Return the duration, in minutes, of the analysis.
    * 
    * @return the analysis duration in minutes.
    */
   Float getDuration();
   void setDuration(Float duration);
   
   void setDataFormat(DataFormat dataFormat);
   
}
