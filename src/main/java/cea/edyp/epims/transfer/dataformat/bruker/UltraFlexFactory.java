package cea.edyp.epims.transfer.dataformat.bruker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;
import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.util.FileUtils;
import org.slf4j.LoggerFactory;

public class UltraFlexFactory extends AbstractCacheFactory{  
        
	private static Logger logger = LoggerFactory.getLogger(UltraFlexFactory.class);
	private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
        
	//All tags used when parsing "acqu" file
	private static final String sample_name = "##$CMT1";                   
	private static final String acq_date = "##$AQ_DATE";
	private static final String acq_operator = "##OWNER";
	private static final String acq_nb_shot = "##$NoSHOTS";
	private static final String acq_method = "##$AXMeth";     
	
	//Tag use to fill the acquisition type column
	private static final String MS_LABEL = "MS";
	private static final String MSMS_LABEL = "MSMS";
	
        
	/**
	 * This method scans the directories in the parent directory passed in parameter.
	 * All directories with a name that correspond to analysisName_DX, where X can be 1, 2, 3 or 4
	 * are associate to the analysis
	 * <br>
	 * To retrieve all necessary information, the method reads the acqu file of the first directory found.
	 * Normally all the other acqu are the same.
	 * <br>
	 * @param analysisName: the name of the analysis we wanted to create
	 * @param depot: the directory where all the analysis are stored
	 * @return
	 */
	public UltraFlexAnalysis createAnalysis(String analysisName , File depot) {     
                
		// 1 - Find all the replicates (folders) corresponding to the analysisName
		File[] listReplicatePerAnalysis = depot.listFiles(new AcquFilter(analysisName));   
		
		// 2 - Create the analysis
		UltraFlexAnalysis newAnalysis = new UltraFlexAnalysis(listReplicatePerAnalysis, (UltraFlexFormat)format);
		logPaneLogger.info("Creation de l analyse " + analysisName + " avec "+listReplicatePerAnalysis.length + " replicats");
		
		// 3 - Set the number of replicates
		newAnalysis.setNbReplicat(listReplicatePerAnalysis.length);
        	
		// 4 - Set the analysis name
		newAnalysis.setName(analysisName);               

		// 5 - Set the approximative memory size
		for (int g = 0; g< listReplicatePerAnalysis.length; g++){
			newAnalysis.setEstimatedSize(newAnalysis.getEstimatedSize()+getSizeDir(listReplicatePerAnalysis[g]));
		}    
		
		// 6 - Get All the "acqu" file in a replicate
		ArrayList<File> acquResults = new ArrayList<File>();
		getOnlyPathAcqu(listReplicatePerAnalysis[0], acquResults);
		
		// 7 - Set the number of acquisition ("acqu" file) per replicate
		newAnalysis.setNbAcqPerReplicate(acquResults.size());
		
		// 8 - Verify that replicate contains "acqu" file.
		
		if(acquResults.size() <= 0){//8.1 - no "acqu" file => stop the factory
			
			String msg = RSCS.getString("bruker.acq.badformat");
			logPaneLogger.error("Analyse " + "\"" + analysisName + "\"" + ": " +msg);  
		
		}else{// 8.2 - acquResults.size() > 0, it's possible to keep on
			
	        // 8.2.1 - Set the Number of shots
			setAverageNbOfShots(listReplicatePerAnalysis, newAnalysis);
			
			// 8.2.2 - Set the acquisition type: MS ou MSMS
			setAcquisitionType( listReplicatePerAnalysis[0], newAnalysis);
			
			// 8.2.3 - Retrieve informations from the first "acqu" file and set them to the analysis : user, acquisition date, AutoXMehthod, and the sample name
			retrieveInfoFromAcquFile(acquResults.get(0),  newAnalysis);
		}

		//9 - return the created analysis even if it is not completely filled analysis
		return newAnalysis;
	}

	/**
	 * Recursive function to get size of a folder
	 * @param dir
	 * @return the approximative size of a directory
	 */
	public static long getSizeDir(File dir){               
		if ( dir.isDirectory ( ) ) {
			File[] list = dir.listFiles();
			long size_int=0;
			for ( int i = 0; i < list.length; i++) {                    
				size_int += getSizeDir(list[i]);
			}                
			return size_int;
		}else{          
			return dir.length();
		}                       
	} 

	/**
	 * Function that convert a string in date
	 * @param sDate
	 * @param sFormat
	 * @return a date
	 * @throws Exception
	 */
	public static Date stringToDate(String sDate, String sFormat) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.parse(sDate);
	}
	
	/**
	 * This function set the acquisition Type : MS or MSMS
	 * Actualy, just MS acquisition arer supported, so the type is always MS
	 * 
	 * @param firstReplicate
	 * @param analysis
	 */
	private static void setAcquisitionType(File firstReplicate, UltraFlexAnalysis analysis ){
		//TODO: gerer aussi les acquisition MSMS, pour l'instant ce n'est que des acquisitions MS
		analysis.setAcqType(MS_LABEL);
		analysis.addDescription(" Analyse "+ MS_LABEL+",");
	}
	

	/**
	 * Recursive function that lists:
	 * <li> in the parameter acquResults all the files with the name "acqu" </li>
	 * <li> in the parameter msmsResults all the folder with the name ".lift" </li>
	 * <br>
	 * in the folder "repertoire" passed in parameter and its sub-folders
	 */
	private static void getPathAcqu( File repertoire, ArrayList<File> acquResults, ArrayList<File> msmsResults ) {
		if ( repertoire.isDirectory ( ) ) {
			//Stop condition 1: if it's a lift folder
			String ext = FileUtils.getExtension(repertoire); 
			if (ext != null && ext.equalsIgnoreCase("LIFT")){
				//add the folder in the list
				msmsResults.add(repertoire);
			} 
			//else scans the folder
			else{   	
				File[] list = repertoire.listFiles();
				for ( int i = 0; i < list.length; i++) {
					//recursive call
					getPathAcqu(list[i], acquResults,msmsResults);
				}  
			}        	
		}// Stop condition 2: if it's a file named "acqu"
		else if (repertoire.getName().equals("acqu")){
			acquResults.add(repertoire);	
		}        	            	
	} 
	
	/**
	 * Recursive function that lists in the parameter acquResults all the files named "acqu"
	 * in the folder "repertoire" passed in parameter, and it's sub folder
	 */
	private static void getOnlyPathAcqu( File repertoire, ArrayList<File> acquResults ) {    		
		if ( repertoire.isDirectory ( ) ) {
			File[] list = repertoire.listFiles();
			for ( int i = 0; i < list.length; i++) {
				getOnlyPathAcqu(list[i], acquResults);
			}        	
		}//Stop condition
		else if (repertoire.getName().equals("acqu")){
			acquResults.add(repertoire);	
		}        	            	
	} 
        
	/**
	 * This is the main function of the factory, the function that read the acqu file and retrieve
	 * information to complete a analysis passed in parameter:
	 * the tas sherch are : user, acquisition date, AutoXMehthod, and the sample name
	 */
	private static void retrieveInfoFromAcquFile(File acqu, UltraFlexAnalysis analysis){

		try{
			logPaneLogger.info("reading "+ acqu.getAbsolutePath());
			Scanner scanner=new Scanner(acqu);
			// loop one each libe
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine(); 
				
				//encounter user tag  				
				if(line.contains(acq_operator)){
					String value = line.split("=")[1].trim();
					analysis.setOperator(value);
				}                                    
    				
				//encounter sample tag (comment 1)
				else if(line.contains(sample_name) ){
					String value = line.split("=")[1].trim();
					String nom_echantillon = value.substring(1, value.length()-1);			 				
					analysis.setSample(nom_echantillon);    					
				}
    				   
				//encounter acquisition date tag
				else if (line.contains(acq_date)){
					String value = line.split("=")[1].trim();
					try {
						Date d = stringToDate(value.substring(1, 11), "yyyy-MM-dd");
						analysis.setDate(d);
					} catch(Exception e) {
						System.err.print("Exception :");
						e.printStackTrace();
					}                                 
				}                                                                            

				//encounter acquisition method, the autoXmethod
				else if (line.contains(acq_method)){
					String value = line.split("=")[1].trim();
					//set specific analysis property
					analysis.setAutoXMethod((value.substring(1, value.length()-1)));
				}                                       
			}
			
			scanner.close();
			logPaneLogger.info("readind file Done.");

		}catch (FileNotFoundException fnfe) {
			logPaneLogger.info("Erreur, impossible de lire le fichier "+ acqu.getAbsolutePath() + " pour l'analysis "+analysis.getName()+", le disque de l'instrument est-il correctement accessible");
			System.err.print("Exception :");        			
			fnfe.printStackTrace();      				    			
		}        	
	}
	
	/**
	 * This function is designed to retrieve each Nb of shots from replicates and make an average. 
	 * But only when there is one "acqu" file per replicate.
	 * 
	 * If there is more than one "acqu" file per replicate, no average is done. 
	 * Just the first value of the first "acqu" file is taken in account in this case.
	 * 
	 * @param replicatesList
	 * @param analysis
	 */
	public static void setAverageNbOfShots(File replicatesList[], UltraFlexAnalysis analysis){
		
		logger.debug("setAverageNbOfShots : nb de rep: " + replicatesList.length);
		
		int newNbShots;
		int currentNbShots = 0;
		int i;
		boolean isOneAcquFile =false;		
		
		// 1 - find all the "acqu" file fr one replicate		
		ArrayList<File> acquFileList = new ArrayList<File>(); 
		logger.debug("Get all files named 'acqu' for the replicate "+replicatesList[0].getName());
		getOnlyPathAcqu(replicatesList[0], acquFileList);
		logger.debug(" Found "+acquFileList.size() + " acqu file");
		
		// 2 - determine  the case
		if ( acquFileList.size() == 1){
			isOneAcquFile = true;
		}else if (acquFileList.size() > 1){
			isOneAcquFile = false;
		}else{
			//TODO: raise an exception du style c'est pas bon...
		}
		
		// 3 - in case we have more than one "acqu" file.
		if(isOneAcquFile == false){
			logger.debug("Case n2 : No average done");
			analysis.setNbShoots(scanAcquFileAndRetrieveParameter(acquFileList.get(0), acq_nb_shot));
		}
		
		// 4 - in the case there is one "acqu" file
		if ( isOneAcquFile == true){
			logger.debug("Cas n1 : Average in progress");
			
			// 4.1 - get all acqu file from replicates
			for(i = 1; i < replicatesList.length ; i++){
				getOnlyPathAcqu(replicatesList[i], acquFileList);
			}
			logger.debug("we got in total "+acquFileList.size() + " acqu file" );
			
			// 4.2 - retrieve form "acqu" files the nb of shots
			for (i =0; i < acquFileList.size(); i++){
				newNbShots = Integer.parseInt(scanAcquFileAndRetrieveParameter(acquFileList.get(i), acq_nb_shot));
				currentNbShots = currentNbShots + newNbShots;
				logger.debug("current number of shots: " + currentNbShots );
			}//end for
			
			// 4.3 - make the average and set to analysis attribute
			float average =  currentNbShots/acquFileList.size();
			analysis.setNbShoots(Float.toString(average));
			logger.debug("average : " + average);
			
		}
	}
	
	/**
	 * This function search the value of a parameter, done in parameter, in a "acqu" file done also in parameter. 
	 * 
	 * @param acqu
	 * @param parameter
	 * @return String : the value of the parameter in the "acqu" file
	 */
	private static String scanAcquFileAndRetrieveParameter(File acqu, String parameter){
		String value = null;
		try{
			Scanner scanner=new Scanner(acqu);
			// loop one each line
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine(); 
			
				//encounter acquisition method, the number of shots
				if(line.contains(parameter)){
					value = line.split("=")[1].trim();
					logger.debug("Parameter "+parameter+ " = " + value);
				}     
			}
			scanner.close();
		}catch (FileNotFoundException fnfe) {
			logPaneLogger.info("Erreur de lecture du fichier "+acqu.getName()+" le disque de l'instrument est-il correctement accessible");
			System.err.print("Exception :");        			
			fnfe.printStackTrace();      				    			
		}
		return value;
	}
	

	@Override
	/**
	 * Implementation of function createAnalysis declared in AbstractCacheFactory
	 * the file parameter is the repository of analysis on the instrument
	 * 
	 * In this function we identify the analysis according to the folder name.
	 */
	public List<Analysis> createAnalysis(File file) {
		
		//initiate the returned list
		List<Analysis> analysis = new ArrayList<Analysis>();
		
		logger.info("reading directory "+file.getAbsolutePath());
		
		//get all the analysis files thanks to the data filter
		File[] files = file.listFiles(new BrukerFileFilter());
		logger.info("Number of total analysis (with replicates): "+files.length);   

		//if the listFiles() return null => the directory doesn't exists (or there is an I/O error)
		if(files == null){
			String msg = RSCS.getString("acq.dir.notexist");
			Object[] args = {file};
			logger.error(MessageFormat.format(msg, args));
			//return an empty analysis list
			return new ArrayList<Analysis>();
		}//I got analysis folder
		else{ 
			//I regroup the analysis I found in epims analysis, taking account of replicates	
			//this array list the epims analysis
			ArrayList<String> listEpimsAnalysisName =  new ArrayList<String>();                    
        		
			for (int i=0; i < files.length; i++){
				//get the epims analysis name contained in folder name
				String[] decoupPath = files[i].getName().split("_D");             
				String epimsAnalysisName = decoupPath[0];
				
				//if it's not already in, add it    			
				if (!listEpimsAnalysisName.contains(epimsAnalysisName)){
					listEpimsAnalysisName.add(epimsAnalysisName);
				}//else nothing        
			}

			logger.info("Number of total ePims analysis: "+listEpimsAnalysisName.size());

			//for each ePims analysis name, I created the related analysis
			for (int j=0; j < listEpimsAnalysisName.size(); j++){
				analysis.add(createAnalysis(listEpimsAnalysisName.get(j), file));
			}
			return analysis;                        
		} 
	}

	@Override
	public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
		throw new UnsupportedOperationException("Not supported Operation");
	}
}

class AcquFilter implements FilenameFilter {
	String afn;
	AcquFilter(String afn) { 
		this.afn = afn; 
	}
	public boolean accept(File dir, String file) {
		return file.split("_D")[0].equalsIgnoreCase(this.afn);
	}
}


