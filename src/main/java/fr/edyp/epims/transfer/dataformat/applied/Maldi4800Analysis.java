/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.edyp.epims.transfer.model.AbstractAnalysis;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.DataFormat;

/**
 *  Update whith code evolution but no tests done ...
 *  If ti be used again, make some tests
 * 
 * @author DB217215
 */
public class Maldi4800Analysis extends AbstractAnalysis {


   private static final Maldi4800Filter Filefilter = new Maldi4800Filter();

   private List<File> dataFileList;
   private String dataFileState;
   private File analysisDescriptionFile;
   private Maldi4800Format dataFormat;
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
      dataFileList = new ArrayList<>();
      dataFileList.add(analysisDescriptionFile);
      status = ANALYSIS_STATUS_UNKNOWN;
      analyseType = Analysis.AnalysisType.UNKNOWN;
      dataFormat = format;
      estimatedSize = -1;
      description = "";
      duration = null;
   }

  public void setDataFormat(DataFormat format){
  	if(format instanceof Maldi4800Format){
  		dataFormat = (Maldi4800Format) format;
  		associatedFiles = null;
  	}
  }

  public long getEstimatedSize() {
    if(estimatedSize == 0){
      for (int i=0; i<dataFileList.size(); i++){
        estimatedSize += dataFileList.get(i).length();
      }
    }
    return estimatedSize;
  }

  public File getFileToTransfer() {
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
          		 fileLogger.error(MessageFormat.format(msg, args));
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
             logger.info("File to add to zip : {}", currentFile.getName());
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
             fileLogger.error(MessageFormat.format(msg, args));
             errorExists = true;
           }
         }//END of for(dataFileList)
       
         // Complete the ZIP file
         zipOutput.close();
       } catch (IOException e) {
         logger.error("Error while compacting file into a zip. Trace : {}", e.getMessage());
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
  public File getSourceFile() {
     //VDS FIXME : analysisFile is not the source. Source is description File (common to multiple analyses)
     // and dataFiles . Which should be deleted when clean is asked ?
    return analysisFile;
  }


  @Override
  public boolean isTransferFileTempo() {
    return true; //it's the zip file
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

   public String getAssociatedFileType(File associatedFile) {
     return SPECTRA_FILETYPE;
   }

  public Float getDuration() {
	   //Check first if the duration is not null, then compare	   
	   if(duration == null || duration<1 )
		   return 0f;
	   else 
		   return duration;
   }

   public String toString(){
     return "Name :"+getName()+". Sample : "+sample;
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
	 * @param spotLabel The new label to add to the description
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
	 * @param isMS: if true add "MS" to the description else add "MSMS"
	 * @author vbouquet
	 */
	public void addAcqTypeDescription(boolean isMS){
		if (isMS){
			description = description+" AcqType:"+MS_LABEL+" ";
		}else{
			description = description+" AcqType:"+MSMS_LABEL+" ";
		}
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
    estimatedSize = 0;
    
    if(dataFileList == null)
      dataFileList = new ArrayList<>();
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
	  }else if(jobWideInterpretMethod.isEmpty()){
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
	  }else if(jobRunDescription.isEmpty()){
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
    return !fileName.startsWith(REJECT_PREFIX);
  }
   
}

