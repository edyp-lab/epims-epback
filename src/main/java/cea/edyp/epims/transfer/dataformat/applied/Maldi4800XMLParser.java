/**
 * 
 */
package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.slf4j.Logger;

/**
 * @author DB217215
 *
 */
public class Maldi4800XMLParser extends DefaultHandler {
  
	private static Logger logger = LoggerFactory.getLogger(Maldi4800XMLParser.class);
	private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	
  private List<Maldi4800Analysis> analysisList;
  private Map<String, String> jobRIdToJobRNumberMap; //A map which make the relation between a JobRunId and its JobRunNumber
  private Map<String, Float> jobRIdToJobRDurationMap; //The same as above but with Job Run Duration
  private Map<String, Date> jobRIdToJobRDateMap; //The same as above but with Job Run Date
  private Map<String, String> jobRidToJobRSubmittedUser; //A map which make the relation between a JobRunId and its submitted user
  private Map<String, String> jobRIdToJWIMMap; //A map which make the relation between a JobRunId and its jobWideInterpretMethod
  private Map<String, String> jobRIdToDJobRunDescriptionMap; //A map which make the relation between a JobRunId and its description
  private List<Map<String, String>> jobRunItemAttribList;
  private Maldi4800Analysis currentAnalysis;
  private String spotsetName;
  private String spotSetPath;
  private String maldiPlateName;
  private String currentJobRunNumber;
  private String currentSampleName;  
  private String currentAnalName;
  private String currentSpotLabel;
  private File file;
  private Maldi4800Format format; 
  private int currentJobRunItemRank; //Variable passed to extract4000SEDataFile method and used into it to determine a part of the dataFile name
  
  private static final String MS_LABEL = "MS";
  private static final String MSMS_LABEL = "MSMS";
  private static final String DATAFILE_EXT = "t2d";
  
  /**
   * Extension of calibration file
   */
  private static final String CALFILE_EXT = "cal";
  
  //XML tags and attributes label
  private static final String SPOTSET_TAG = "SpotSet";
  private static final String SPOTTEDPLATE_TAG = "SpottedPlate";
  private static final String SPOT_TAG = "Spot";
  private static final String JOBRUN_TAG = "JobRun";
  private static final String JOBRUNITEM_TAG = "JobRunItem";
  private static final String SPOTSET_NAME_LABEL = "name";
  private static final String SPOTTEDPLATE_NAME_LABEL = "name";
  private static final String JOBRUN_ID_LABEL = "id";
  private static final String JOBRUN_START_LABEL = "startDT";
  private static final String JOBRUN_FINISH_LABEL = "finishDT";
  private static final String JOBRUN_NUMBER_LABEL = "jobRunNumber";
  private static final String SPOT_NAME_LABEL = "name";
  protected static final String SPOT_LABEL_LABEL = "label";
  private static final String JOBRUNITEM_ID_LABEL = "id";
  private static final String JOBRUNITEM_JOBRUNID_LABEL = "jobRunID";
  private static final String JOBRUNITEM_PRECURSOR_LABEL = "precursorMass";
  private static final String JOBRUNITEM_DATEFILEPATH_LABEL = "dataFilePath";
  private static final String JOBRUNITEM_INTERP_PARENT_LABEL = "interpParentJobRunID";
  private static final String SPOT_TYPE_LABEL = "spotType";
  private static final String SPOT_TYPE_VAL_U = "U";
  private static final String SPOT_ISALIGN_LABEL = "isAlignSpot";
  private static final String SPOT_ISALIGN_VAL_YES = "Y";
  
  /**
   * @author vbouquet<br>
   * During the acquisition some analyses are stopped due to low intensity<br>
   * In this case the tag <code>JobRunItem.id</code> (JOBRUNITEM_ID_LABEL)<br>
   * is egal to 0, so the sort is not correct.<br>
   * Because the old algorithm use this tag to sort the JobRunItem, 
   * it's better to use instead the tag <code>JobRunItem.jobItemID</code> (JOBRUNITEM_JOBITEMID_LABEL)<br>
   * Indeed this tag never egals to 0, it egals the real rank ok the JobRunItem<br>
   * 
   */
  private static final String JOBRUNITEM_JOBITEMID_LABEL = "jobItemID";

  /**
   * The tag JobRunItem.acqType can take only two value:<br>
   * 	> 2 if it's a MS acquisition<br>
   * 	> 4 if it's a MSMS acquisition<br>
   * This tag is used in the function extract4000SEDataFile <br>
   * instead of the tag JobRunItem.interpParentJobRunID <br>
   * because this tag could be egals to 0 even if the acquisition is a MSMS<br>
   */
  private static final String JOBRUNITEM_ACQTYPE_LABEL = "acqType";
  /**
   * Possible values of the tag JobRunItem.acqType
   */
  private static final String JOBRUNITEM_ACQTYPE_MS = "2";
  private static final String JOBRUNITEM_ACQTYPE_MSMS = "4";
    
  /** 
   * @author vbouquet<br>
   * In certain use of the 4000SE software pilote <br>
   * The names of analysed samples are writen in the tag <code>Spot.locationInContainer</code><br>
   * Use case found in Platform 3P5
   */
  private static final String SPOT_LOCATION_IN_CONTAINER_LABEL = "locationInContainer";  
  
  /**
   * The xml label of the submitted user, there is a submitted user per jobrun
   */
  private static final String JOBRUN_SUBMITTED_USER_LABEL = "submittedUser";
  
  /**
   * This tag contents informations important for 3P5, the content of the tag will be added to the analyze description
   */
  private static final String JOBRUN_JOBWIDEINTERPRETMETHOD_LABEL = "jobWideInterpretMethod";
  
  
  /**
   * This tag is the description of jobRun, platform 3P5 is interested in save it into analyze description  
   */
  private static final String JOBRUN_DESCRIPTION_LABEL = "description";
  
  /**
   * in case of digest analysis we must have the SpotSet.originalTemplateName
   */
  private static final String SPOTSET_ORIGINALTEMPLATENAME_LABEL = "originalTemplateName";
  
  public void extractAnalysis(File file, Maldi4800Format format) {
    
    //init
    this.file = file;
    this.format = format;
    analysisList = new ArrayList<Maldi4800Analysis>();
    jobRunItemAttribList = new ArrayList<Map<String, String>>();
    jobRIdToJobRNumberMap = new HashMap<String, String>();
    jobRIdToJobRDurationMap = new HashMap<String, Float>();
    jobRIdToJobRDateMap = new HashMap<String, Date>();
    //init map to store submitted user
    jobRidToJobRSubmittedUser = new HashMap<String, String>();
    //Initialize the map to store the jobWideInterpretMethod of each jobRun
    jobRIdToJWIMMap = new HashMap<String, String>();    
    //Map to store the description of each JobRun
    jobRIdToDJobRunDescriptionMap = new HashMap<String, String>();
    
    //get a factory
    SAXParserFactory spf = SAXParserFactory.newInstance();
    try {

      //get a new instance of parser
      SAXParser sp = spf.newSAXParser();

      //parse the file and also register this class for call backs
      sp.parse(file, this);

    }catch(SAXException se) {
      se.printStackTrace();
    }catch(ParserConfigurationException pce) {
      pce.printStackTrace();
    }catch (IOException ie) {
      ie.printStackTrace();
    }
  }
  
  
  //Event Handlers
  public void startElement(String uri, String localName, String qName,
    Attributes attributes) throws SAXException {
      
    //SpotSet event
    if(qName.equalsIgnoreCase(SPOTSET_TAG)) {
      //Take the name of the spotset, in the xml its of the form [path]\spotsetName so the two part of the string must be split
      String tempSpotsetName = attributes.getValue(SPOTSET_NAME_LABEL);
      spotSetPath = tempSpotsetName.substring(0,tempSpotsetName.lastIndexOf("\\")+1);
      spotsetName = tempSpotsetName.substring(tempSpotsetName.lastIndexOf("\\")+1);
     
      //treat the Spotset.OriginalTemplateName value
      workWithOriginalTemplateName(attributes.getValue(SPOTSET_ORIGINALTEMPLATENAME_LABEL),spotsetName);
    }
    
    
    //SpottedPlate event
    if(qName.equalsIgnoreCase(SPOTTEDPLATE_TAG)) {
      maldiPlateName = attributes.getValue(SPOTTEDPLATE_NAME_LABEL);
    }
    
        
    //JobRun event
    if(qName.equalsIgnoreCase(JOBRUN_TAG)){
      Float jobRunDuration;
      String idString = attributes.getValue(JOBRUN_ID_LABEL);
      
      Date startDate = getDateFromAttribute(attributes.getValue(JOBRUN_START_LABEL));
      Date finishDate = getDateFromAttribute(attributes.getValue(JOBRUN_FINISH_LABEL));
      if(startDate == null || finishDate == null)
        jobRunDuration = null;
      else
        jobRunDuration = (float) ((finishDate.getTime() - startDate.getTime()) / (1000 * 60));
      
      //retrieve the jobrun.submitteduser
      String submittedUser = attributes.getValue(JOBRUN_SUBMITTED_USER_LABEL);
      //storing submitted user
      jobRidToJobRSubmittedUser.put(idString, submittedUser);
      
      jobRIdToJobRNumberMap.put(idString, attributes.getValue(JOBRUN_NUMBER_LABEL));
      jobRIdToJobRDurationMap.put(idString, jobRunDuration);
      jobRIdToJobRDateMap.put(idString, startDate);
      
      //retrieve and store jobWideInterpretMethod of current jobrun
      String jobWideInterpretMethod = attributes.getValue(JOBRUN_JOBWIDEINTERPRETMETHOD_LABEL);
      jobRIdToJWIMMap.put(idString, jobWideInterpretMethod);  
      //retrieve and store jobWideInterpretMethod of current jobrun
      String jobRunDescription = attributes.getValue(JOBRUN_DESCRIPTION_LABEL);
      jobRIdToDJobRunDescriptionMap.put(idString, jobRunDescription);
    }
    
    
    //Spot event
    if(qName.equalsIgnoreCase(SPOT_TAG)) {
      
      //Just treat the spot which are spot of type "U" and not align spot
      if(attributes.getValue(SPOT_TYPE_LABEL).equalsIgnoreCase(SPOT_TYPE_VAL_U) 
          && ! attributes.getValue(SPOT_ISALIGN_LABEL).equalsIgnoreCase(SPOT_ISALIGN_VAL_YES))
      {
    	  currentSampleName = retrieveSampleNameFromAttributes(attributes);		
    	  logger.debug("cuurent sample Name: "+currentSampleName);
    	  //spot label retrieve
    	  currentSpotLabel = attributes.getValue(SPOT_LABEL_LABEL);
      }
    }
    
    
    //JobRunItem Event :
    //saving all attribute of interest for further action (in Spot endElement)
    if(qName.equalsIgnoreCase(JOBRUNITEM_TAG)){
      
      if(currentSampleName != null && currentSpotLabel != null)
      {
        Map<String, String> currentAttribMap = new HashMap<String, String>();
        currentAttribMap.put(JOBRUNITEM_ID_LABEL, attributes.getValue(JOBRUNITEM_ID_LABEL));
        currentAttribMap.put(JOBRUNITEM_JOBRUNID_LABEL, attributes.getValue(JOBRUNITEM_JOBRUNID_LABEL));
        currentAttribMap.put(JOBRUNITEM_PRECURSOR_LABEL, attributes.getValue(JOBRUNITEM_PRECURSOR_LABEL));
        currentAttribMap.put(JOBRUNITEM_INTERP_PARENT_LABEL, attributes.getValue(JOBRUNITEM_INTERP_PARENT_LABEL));
        currentAttribMap.put(JOBRUNITEM_DATEFILEPATH_LABEL, attributes.getValue(JOBRUNITEM_DATEFILEPATH_LABEL));
        
        //saving the value of the attribute JobRunItem.jobItemID for the sorting
        currentAttribMap.put(JOBRUNITEM_JOBITEMID_LABEL, attributes.getValue(JOBRUNITEM_JOBITEMID_LABEL));
        
        //saving the vale of the tag JobRunItem.acqType
        currentAttribMap.put(JOBRUNITEM_ACQTYPE_LABEL, attributes.getValue(JOBRUNITEM_ACQTYPE_LABEL));
  
        jobRunItemAttribList.add(currentAttribMap);
      }
    }
   }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    Map<String, String> currentAttrib;
    String currentJobRunId;
    String currentJobRunItemID; //local variable to store current JobRunItem.jobItemID (JOBRUNITEM_JOBITEMID_LABEL)
    int i;
    int j;

    //End of a spot tag => linked Analysis creation
    if(qName.equalsIgnoreCase(SPOT_TAG)){
      currentJobRunNumber = "";
      currentAnalName = "";
      currentJobRunItemRank = 0;
      
      //if there is some acquisition (JobRunItem) and the spot are available continue
      if(jobRunItemAttribList.size() != 0 && currentSampleName != null && currentSpotLabel != null)
      {
        
      	/**
      	 * @author vbouquet<BR>
      	 * Collections.sort of a list of jobRunItem coming from a Spot<br>
      	 * The sort is based on the value of tag <code>JobRunItem.jobItemID</code><br>
      	 * to put in first those which are created first (Ascending sort)
      	 */
          Collections.sort(jobRunItemAttribList, new Comparator<Map<String, String>>(){
         	 public int compare(Map<String, String> attributes1, Map<String, String> attributes2){
         		 if(Integer.valueOf(attributes1.get(JOBRUNITEM_JOBITEMID_LABEL)) 
         				 < Integer.valueOf(attributes2.get(JOBRUNITEM_JOBITEMID_LABEL)))
         			 return -1;
         		 else if (Integer.valueOf(attributes1.get(JOBRUNITEM_JOBITEMID_LABEL)) 
         				 == Integer.valueOf(attributes2.get(JOBRUNITEM_JOBITEMID_LABEL)))
         			 return 0;
         		 else return 1;
         	 }
         }
         ); //end of Collections.sort
  
       
        //Spot's JobRunItem Attributes List traversing to create corresponding Analysis or complete it if its already exist
        for(i = 0; i < jobRunItemAttribList.size(); i++)
        {
        	//increment the currentJobRunItemRank whatever the jobRunItem's status
        	currentJobRunItemRank++;
        	
        	//get the ieme JobRunItem
        	currentAttrib = jobRunItemAttribList.get(i);       	
        	
        	//get the value of the JobRunItem.id
        	currentJobRunItemID = currentAttrib.get(JOBRUNITEM_ID_LABEL);
        	
        	//If the currentJobRunItemID = 0, that means this JobRunItem was Stopped
        	if (currentJobRunItemID.equals("0")){        		
        		logger.debug("This stopped jobRunItem will not be added to the analysis.");        		
        	
        	}else { //The currentJobRunItemID of the JobRunItem doesn't egal 0
        		//Get the value of JobRunItem.jobRunID
        		currentJobRunId = currentAttrib.get(JOBRUNITEM_JOBRUNID_LABEL);
        	
        		//Retrieve of the JobRunNumber corresponding to the JobRunId of this JobRunItem
        		currentJobRunNumber = jobRIdToJobRNumberMap.get(currentJobRunId);          
        		
        		//analysis' name construction
        		currentAnalName = spotsetName+"-"+currentJobRunNumber+"-"+currentSampleName; 
            
        		//test if the analysis name is already in the list
        		j = 0;
        		while(j < analysisList.size() && ! analysisList.get(j).getName().equals(currentAnalName))
        			j++;
            
        		//Analysis name not found in analysis list => Analysis creation
        		if(j == analysisList.size()) 
        		{
        			logger.debug("New analysis found and created : "+currentAnalName+". Affected sample : "+currentSampleName);
        			currentAnalysis = new Maldi4800Analysis(file, (Maldi4800Format)format);
        			currentAnalysis.setName(currentAnalName);
        			currentAnalysis.setSample(currentSampleName);
        			currentAnalysis.setDate(jobRIdToJobRDateMap.get(currentJobRunId));
        			currentAnalysis.setDuration(jobRIdToJobRDurationMap.get(currentJobRunId));
        			currentAnalysis.addPlateDescription(maldiPlateName);
        			//add also the spottedPlate in a analysis attribute
        			currentAnalysis.setSpottedPlate(maldiPlateName);
        			//adding submitted user in current analysis
        			currentAnalysis.setOperator(jobRidToJobRSubmittedUser.get(currentJobRunId));
        			
        			//add to description the acquisition nature
              	  	String acqType = currentAttrib.get(JOBRUNITEM_ACQTYPE_LABEL);
              	    //test value of String acqType
              	    if(acqType.equals(JOBRUNITEM_ACQTYPE_MSMS)){
              	    	//changing the analyze description for a MSMS
              	    	currentAnalysis.addAcqTypeDescription(false);
              	    	//add fill in the acqType attribute
              	    	currentAnalysis.setAcqType(MSMS_LABEL);
              	    }else{
              	    	//changing the analyze description for a MS
              	    	currentAnalysis.addAcqTypeDescription(true);
              	    	//add fill in the acqType attribute
              	    	currentAnalysis.setAcqType(MS_LABEL);
              	    }    
        			
              	    //add to description the jobWideInterpretMethod content
              	    currentAnalysis.addJobWideInterpretMethodDescription(jobRIdToJWIMMap.get(currentJobRunId));
              	    //set the jobWideInterpretMethod analysis attribute too:
              	    currentAnalysis.setJobWideInterpretMethod(jobRIdToJWIMMap.get(currentJobRunId));
              
              	    //add to analyze description the JobRun.Description content
              	    currentAnalysis.addJobRunDescription(jobRIdToDJobRunDescriptionMap.get(currentJobRunId));
              	    //set the jobRunDescription analysis attribute too:
              	    currentAnalysis.setJobRunDescription(jobRIdToDJobRunDescriptionMap.get(currentJobRunId));
        			
        			analysisList.add(currentAnalysis);
        		}
        		else
        			currentAnalysis = analysisList.get(j);
          
        		//Analysis' Data adding
        		//first add the t2d file
        		currentAnalysis.addDataFile(extract4000SEDataFile(currentAttrib, currentJobRunItemRank, false));
        		
        		//Second test if the cal file is not null and then add the cal file
        		File calFile = extract4000SEDataFile(currentAttrib, currentJobRunItemRank,true);
        		
          	  	if (calFile.exists()){
          	  		currentAnalysis.addDataFile(calFile);
          	  	}else{
          	  		//Nothing to do, don't add the null file
            		//Send a message to the log and logPane to notice user the .cal was not found
            		//TODO: mettre un bundle pour assurer la traduction FR/EN
            		logPaneLogger.error("Erreur pour l'analyse : \""+ currentAnalysis.getName() +"\" Impossible de trouver le fichier de calibration: " + calFile.getName() );
            		logger.debug("Erreur pour l'analyse : \""+ currentAnalysis.getName() +"\" Impossible de trouver le fichier de calibration: " + calFile.getName());
                    //TODO: Faire apparaitre qq part qu'il manque des fichiers
          	  	}
        		
        		
        		currentAnalysis.addSpotDescription(currentSpotLabel);
        		
        	}//end else: currentJobRunItemID of the JobRunItem doesn't egal 0
        }//end for
        
        //End of a Spot tag : re-init of the variable link to it
        currentSampleName = null;
        currentSpotLabel = null;
        jobRunItemAttribList.clear();
      }//end if jobRunItemAttribList is not empty and if spot is available (jobRunItemAttribList.size() != 0 && currentSampleName != null && currentSpotLabel != null)
    }//end if endElement = Spot
    
  }
  
  /**
   * Create a File with informations given in params
   */
  private File extract4000SEDataFile(Map<String,String> jobRunItemAttributes, int jobRunItemRank, boolean isCal){
    String analysisType;
    String precursorMassTag;
    //String interpretationParentId; //this variable is useless 
    String acqType; //use this local variable instead of interpretationParentId
    Double precursorMass;
    boolean isMSMS; //boolean to know if the acquisition is of the MSMS type or not
    NumberFormat formatter = new DecimalFormat("#.0000", new DecimalFormatSymbols(Locale.ENGLISH));
    
    //get the value of tag JibRunItem.acqType of the current JobRunItem
    acqType = jobRunItemAttributes.get(JOBRUNITEM_ACQTYPE_LABEL);
    
    //test value of String acqType
    if(acqType.equals(JOBRUNITEM_ACQTYPE_MSMS)){
    	isMSMS = true;
    }else{
    	isMSMS = false;
    }
    
    precursorMass = Math.round(Double.valueOf(jobRunItemAttributes.get(JOBRUNITEM_PRECURSOR_LABEL))*10000.0)/10000.0;
    analysisType = (isMSMS)? MSMS_LABEL: MS_LABEL;
    precursorMassTag = (isMSMS)? formatter.format(precursorMass)+"_" : "";
    
    String fileDir = format.getSrcDir()+"//"+format.getDataFilesDir()+"//"+spotSetPath+spotsetName+"\\";
    
    if(!isCal){
    	String fileName = currentSpotLabel+"_"+analysisType+"_"+precursorMassTag+jobRunItemRank+"."+DATAFILE_EXT;   
    	return new File(fileDir,fileName);
    
    }else{ //Construction of the calibration file path 
    	String fileName = currentSpotLabel+"_"+analysisType+"_"+precursorMassTag+jobRunItemRank+"-1."+CALFILE_EXT; 
    	File FileTemp = new File(fileDir,fileName);
    	//Return this contructed file without cheked if exist, this is done at the call of the function.
    	return FileTemp;
    }  	
  }

  

  /**
   * Extract the date from an attribute xxxxDT find in a spotset XML (startDT, finishDT, acqDT, procDT, interpDT, etc...)
   * The format is : "MM/DD/YYYY hh:mm:ss" example : "07/15/2008 08:42:18"
   * index of each ch:0123456789012345678
   * 
   * @return Date the date extract from the String passed in argument
   * @param The String representing the attribute xxxxDT extract from the spotset XML
   */
  private Date getDateFromAttribute(String dateTimeAttribute){
    if(dateTimeAttribute == null || dateTimeAttribute.equals("")){
      return null;
    }else{
      int month = Integer.parseInt(dateTimeAttribute.substring(0,2))-1; //Month-1 : Because of an error in cal.set() below
      int day = Integer.parseInt(dateTimeAttribute.substring(3,5));
      int year = Integer.parseInt(dateTimeAttribute.substring(6,10));
      int hour = Integer.parseInt(dateTimeAttribute.substring(11,13));
      int min = Integer.parseInt(dateTimeAttribute.substring(14,16));
      int sec = Integer.parseInt(dateTimeAttribute.substring(17));
      
      Calendar cal = Calendar.getInstance();
      cal.set(year, month, day, hour, min, sec);
      
      return cal.getTime();
    }
  }

  public List<Maldi4800Analysis> getAnalysisList() {
    return analysisList;
  }

  public String retrieveSampleNameFromAttributes(Attributes attributes){
	  
	  logger.debug("Retrieve Sample Name by nanoLC way !!!!!!");
	  String SpotName = attributes.getValue(SPOT_NAME_LABEL);
	  String SpotLocationInContainer = attributes.getValue(SPOT_LOCATION_IN_CONTAINER_LABEL);
	  
	  String sampleName = "";
	  
	  if(SpotName.equals("")){
			//If the value Spot.name is empty get the sample name in Spot.locationIncontainer
			if( SpotLocationInContainer == null){
				sampleName = "";
			}else{
				sampleName = SpotLocationInContainer;
			}
		}else{
			sampleName = SpotName;
		}
	  return sampleName;
  }
  
  public void workWithOriginalTemplateName(String spotsetOriginalTemplateName, String spotsetName  ){
	  
	  logger.debug("Use the spotset value according nanoLC method : do nothing");
	  //do nothing in this case
	  
  }


}
