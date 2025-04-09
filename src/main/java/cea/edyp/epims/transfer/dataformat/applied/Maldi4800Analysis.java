package cea.edyp.epims.transfer.dataformat.applied;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author DB217215
 */
//TODO Use AbstractAnalysis & Verify Log Usage
public class Maldi4800Analysis implements Analysis, PropertyChangeListener {

   @SuppressWarnings("unused")
   private static final Logger logger = LoggerFactory.getLogger(Maldi4800Analysis.class);
   private static final Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
   private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

   
   private static final Maldi4800Filter Filefilter = new Maldi4800Filter();
   
   private String name;
   private List<File> dataFileList;
   private String dataFileState;
   private File analysisDescriptionFile;
   private File analysisFile;
   private String analysisDestination;
   private Analysis.AnalysisType analyseType;
   private String sample;
   private String description;
   private String operator;
   private Float duration;
   private long size;
   private Date acqDate;
   private Maldi4800Format dataFormat;
   private int statusMask;
   private List<File> associatedFiles;
   private String startSpotLabel;
   private String currentEndSpotLabel;
   
   /**
    * A specific Maldi4800Analysis attribute, relative to JobRun.jobWideInterpretMethod
    */
   private String jobWideInterpretMethod;
   /**
    * make the SpottedPlate as a attribute
    */
   private String spottedPlate;
   /**
    * make the acqType as an analysis attribute, it can be MS or MSMS
    */
   private String acqType;
   /**
    * The function addSpotDescription will fill the attribute to get the value in a specific field
    */
   private String spotDescription;
   /**
    * Attribute to store the jobRunDescription retrieve by the XML parser
    */
   private String jobRunDescription;
   
   private static final String DESC_PLATE_TAG = "Pl.:";
   private static final String DESC_SPOT_TAG = "Spots:";
   private static String ANALYSIS_FILE_EXT = "zip";
   private static final String MS_LABEL = "MS";
   private static final String MSMS_LABEL = "MSMS";
   
   
   public Maldi4800Analysis(File f, Maldi4800Format format) {
      analysisDescriptionFile = f;
      dataFileList = new ArrayList<File>();
      dataFileList.add(analysisDescriptionFile);
      statusMask = ANALYSIS_STATUS_UNKNOWN;
      analyseType = Analysis.AnalysisType.UNKNOWN;
      dataFormat = format;
      size = -1;
      description = "";
      duration = null;
   }

  public void setDataFormat(DataFormat format){
  	if(Maldi4800Format.class.isInstance(format)){
  		dataFormat = (Maldi4800Format) format;
  		associatedFiles = null;
  	}
  }
  
  private void setType(){
    if( CTRL_INST_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_INSTRUMENT;
    else if(CTRL_LC_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_LC;
    else if(BLANK_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.BLANK;        
    else if(TEST_ANALYSIS_CODE.equalsIgnoreCase(sample))
    	analyseType = Analysis.AnalysisType.TEST;
    else if(sample == null || sample.trim()=="")
      analyseType = Analysis.AnalysisType.UNKNOWN;
    else 
      analyseType = Analysis.AnalysisType.RESEARCH;     
  }
  
  public String getSample(){
    return sample;
  }

  public Analysis.AnalysisType getType(){
    return analyseType;
  }
  
  public long getEstimatedSize() {
    if(size == 0){
      for (int i=0; i<dataFileList.size(); i++){
        size += dataFileList.get(i).length();
      }
    }
    return size;
  }

   public String getName() {
      return name;
   }

   public File getFile() {
     File currentFile;
     ZipOutputStream zipOutput;
     boolean errorExists = false;
     if(analysisFile == null)
     {
       try {
           //test first if all the file in the dataFileList exist before creating the zip file
           //if one file is missing set errorExists to true and put a error message
    	   //TODO: print in the logpane all the file missing
           for(int j=0; j<dataFileList.size(); j++){
          	 currentFile = dataFileList.get(j);        	 
          	 //if a file doesn't exist
          	 if(!currentFile.exists()){
          		 dataFileState = RSCS.getString("datafile.state.error");
          		 String msg = RSCS.getString("datafile.not.found");
          		 Object[] args = {currentFile}; 
          		 logPaneLogger.error(MessageFormat.format(msg, args));
          		 errorExists = true;        		
          		 throw new IOException(msg);
          	 }//else do nothing
           }

         //TODO changer la façon de faire pour le répertoire temp (pas hardcodé déjà si possible)
         analysisFile = new File("temp/"+getFileName());
         zipOutput = new ZipOutputStream(new FileOutputStream(analysisFile));

         // Create a buffer for reading the files
         byte[] buf = new byte[1024];    
       
         // Compress the files
         for (int i=0; i<dataFileList.size(); i++) {
           currentFile = dataFileList.get(i);
           
           if(currentFile.exists()){
             FileInputStream inputStream = new FileInputStream(currentFile);
             logger.info("File to add to zip : "+currentFile.getName());
             // Add ZIP entry to output stream.
             zipOutput.putNextEntry(new ZipEntry(currentFile.getName()));
     
             // Transfer bytes from the file to the ZIP file
             int len;
             while ((len = inputStream.read(buf)) > 0) {
               zipOutput.write(buf, 0, len);
             }
     
             // Complete the entry
             zipOutput.closeEntry();
             inputStream.close();
           }
           else{
             dataFileState = RSCS.getString("datafile.state.error");
             String msg = RSCS.getString("datafile.not.found");
             Object[] args = {currentFile}; 
             logPaneLogger.error(MessageFormat.format(msg, args));
             errorExists = true;
           }
         }//END of for(dataFileList)
       
         // Complete the ZIP file
         zipOutput.close();
       } catch (IOException e) {
         logger.error("Error while compacting file into a zip. Trace : "+e);
         dataFileState = RSCS.getString("datafile.state.error");
         return null;
       }
       
     }//end if(analysisFile == null)
     
     if(errorExists){
       return null;
     }
     else{
       dataFileState = RSCS.getString("datafile.state.ok");
       return analysisFile;
     }
   }

    @Override
    public boolean removeTemporaryZipFile() {
        return false;
    }

   public String getFileName(){
     return name+"."+ANALYSIS_FILE_EXT;
   }

   public FileFilter getContentFilter() {
      return Filefilter;
   }

   public File[] getAssociatedFiles() {
      return new File[0]; 
   }
   
   private void setAssociatedFiles(){
  	 associatedFiles = new ArrayList<File>();
   }

   public String getAssociatedFileType(File associatedFile) {
     return SPECTRA_FILETYPE;
   }

   public Date getDate() {
      return acqDate;
   }
   
   public void setSample(String spl){
     sample = spl;
     setType();
   }
   
   public int getStatus(){
  	 return statusMask;
   }
   
   public void setStatus(int status){
     statusMask = status;
   }   

   public String getDescription() {
      return description;
   }

   public String getOperator() {
     return operator;
  }

   public Float getDuration() {
	   //Check first if the duration is not null, then compare	   
	   if(duration == null || duration<1 )
		   return 1f;
	   else 
		   return duration;
   }

   public String toString(){
     return "Name :"+getName()+". Sample : "+sample;
   }

	public String getDestination() {
		return this.analysisDestination;
	}

	public void setDestination(String destinationDir) {
		this.analysisDestination = destinationDir;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		this.associatedFiles = null;
		if(evt.getNewValue() != null)
			setAssociatedFiles(); 
	}

	public void setDate(Date date) {
		this.acqDate = date;		
	}

	public void setDescription(String desc) {
		this.description = desc;		
	}
	
	/**
	 * Update the description of this analysis. It add to it the name of the maldi plate
	 */
	public void addPlateDescription(String plateName){
	  description = description+DESC_PLATE_TAG+plateName+". ";
	}
	
	/**
	 * Update the description of the analysis by add a spot label to it.
	 * The update consist in searching of the pattern: DESC_SPOT_TAG[a spot label]-[a spot label]
	 * If is find, it will replace the last spot label by the one given in parameter
	 * If is not, it add the string DESC_SPOT_TAG+startSpotLabel+"-"+currentEndSpotLabel to the description
	 * @param String spotLabel The new label to add to the description
	 */
	public void addSpotDescription(String spotLabel){
	  String newSpotDescription;
	  String spotDescRegexp;
	  
	  if(startSpotLabel == null)
	    startSpotLabel = spotLabel;
	  
	  currentEndSpotLabel = spotLabel;

	  newSpotDescription = DESC_SPOT_TAG+startSpotLabel+"-"+currentEndSpotLabel;
	  spotDescRegexp = DESC_SPOT_TAG+"[a-z|A-Z|0-9]+\\-[a-z|A-Z|0-9]+";
	  Pattern p = Pattern.compile(spotDescRegexp);
	  Matcher m = p.matcher(description);

	  //add the spotDescription in attribute
	  spotDescription = startSpotLabel+"-"+currentEndSpotLabel;
	  
	  if(m.find()){
	    description = description.replace(description.subSequence(m.start(), m.end()), newSpotDescription);
	  }
	  else
	    description = description+newSpotDescription;
	}
	
	/**
	 * This function can add a information concerning the acquisition type (whether MS or MSMS) into the description
	 * @param boolean: if true add "MS" to the description else add "MSMS"
	 * @author vbouquet
	 */
	public void addAcqTypeDescription(boolean isMS){
		if (isMS){
			description = description+" AcqType:"+MS_LABEL+" ";
		}else{
			description = description+" AcqType:"+MSMS_LABEL+" ";
		}
	}

	public void setDuration(Float duration) {
		this.duration = duration;		
	}

	public void setEstimatedSize(long size) {
		this.size =size;
		
	}

	public void setName(String name) {
	  this.name = name;
	}

	public void setOperator(String operator) {
		this.operator =operator;		
	}

	public void setType(AnalysisType type) {
		this.analyseType = type;		
	}

  public List<File> getDataFileList() {
    return dataFileList;
  }

  public void setDataFileList(List<File> dataFileList) {
    this.dataFileList = dataFileList;
  }
  
  /**
   * Add a file to the list of data file of the analysis
   */
  public void addDataFile(File dataFile){
    //reset of the size of the analysis to force to recalculate it
    size = 0;
    
    if(dataFileList == null)
      dataFileList = new ArrayList<File>();
    dataFileList.add(dataFile);
  }
  
  public String getDataFileState(){
    return dataFileState;
  }
  
  /**
   * this function add to analyze description: "JWIM: + the content of jobWideInterpretMethod"<br>
   * JWIM means jobWideInterpretMethod
   * @param jobWideInterpretMethod: the content of tag JobRun.jobWideInterpretMethod
   * @author vbouquet
   */
  public void addJobWideInterpretMethodDescription(String jobWideInterpretMethod){
	  if(jobWideInterpretMethod == null){
		  description = description+" JWIM:"+null+" ";
	  }else if(jobWideInterpretMethod.equals("")){
		  description = description+" JWIM:"+null+" ";
	  }else{
		  description = description+" JWIM:"+jobWideInterpretMethod+" ";
	  }
  }
	
  /**
   * This function ad to the analyze description: "JRD: + the content of the JobRun.Description"<br>
   * JRD means JobRunDescription 
   * @author vbouquet
   */
  public void addJobRunDescription(String jobRunDescription){
	  if(jobRunDescription == null){
		  description = description+" JRD:"+null+" ";
	  }else if(jobRunDescription.equals("")){
		  description = description+" JRD:"+null+" ";
	  }else{
		  description = description+" JRD:"+jobRunDescription+" ";
	  }
  }
  
  
  public String getJobWideInterpretMethod() {
	  return jobWideInterpretMethod;
  }

  public void setJobWideInterpretMethod(String jobWideInterpretMethod) {
	  this.jobWideInterpretMethod = jobWideInterpretMethod;
  }

  public String getSpottedPlate() {
	  return spottedPlate;
  }

  public void setSpottedPlate(String spottedPlate) {
	  this.spottedPlate = spottedPlate;
  }

  public String getAcqType() {
	  return acqType;
  }

  public void setAcqType(String acqType) {
	  this.acqType = acqType;
  }

  public String getSpotDescription() {
	  return spotDescription;
  }

  public void setSpotDescription(String spotDescription) {
	  this.spotDescription = spotDescription;
  }

  public String getJobRunDescription() {
	  return jobRunDescription;
  }

  public void setJobRunDescription(String jobRunDescription) {
	  this.jobRunDescription = jobRunDescription;
  }

}

class Maldi4800Filter implements FileFilter {
  private static final String REJECT_PREFIX = "_PROC"; 
  
  /* (non-Javadoc)
   * @see java.io.FileFilter#accept(java.io.File)
   */
  public boolean accept(File pathname) {
    if(pathname.isDirectory())
      return true;
      
    String fileName = pathname.getName();
    if(fileName.startsWith(REJECT_PREFIX))
      return false;
      
    return true;
  }
   
}

