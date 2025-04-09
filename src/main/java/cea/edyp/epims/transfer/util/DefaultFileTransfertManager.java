package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupException;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import org.slf4j.LoggerFactory;

public class DefaultFileTransfertManager implements IFileTransfertManager {

	protected static final Logger logger = LoggerFactory.getLogger(DefaultFileTransfertManager.class);
	protected static final Logger fileLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	protected boolean allowManyAcquisitionInOneFile;
	protected static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
	
	public DefaultFileTransfertManager(boolean allowManyAcqsInOneFile ){
		allowManyAcquisitionInOneFile = allowManyAcqsInOneFile;  
	}
	
	
	public void copyOnly(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {
		//TODO VD : REMOVE copied file in case of error 
  	try {
  	  File analysisFile = a.getFile();
  		long start = System.currentTimeMillis();
//  		a.getDestination();
  		File destination = new File(a.getDestination(), a.getFileName());

  		boolean skipCopy = false;
  		if(destination.exists()){
  			if(allowManyAcquisitionInOneFile){
  				skipCopy = true;
  				logger.info("File for Analysis "+a.getName()+" already exist");
  			} else {
					String warnMsg ="File for Analysis "+a.getName()+" already exist ! Can't copy acquisition ";
  				logger.warn(warnMsg);
					fileLogger.warn(warnMsg);
  				throw new BackupException("Analysis "+a.getName()+" already exist on PIMS-ROOT");
  			}
  		}
      
  		if(analysisFile == null || ! analysisFile.exists()){
        skipCopy = true;
				String warnMsg ="File for analysis "+a.getName()+" can't be find/created ! Can't copy acquisition";
        logger.warn(warnMsg);
				fileLogger.warn(warnMsg);
        throw new BackupException("Problem on analysisFile "+analysisFile+" for analysis "+a.getName()+". The file can't be reached or is null");
      }
       
  		if(! skipCopy){
  			logger.debug(" Copy only "+analysisFile+" to "+destination.getAbsolutePath());
  			FileUtils.secureCopy(analysisFile, destination, a.getContentFilter());
  			long end = System.currentTimeMillis();
  			long duration = (end-start)/1000;
  			String msg = RSCS.getString("copy.success");
  			Object[] args = {a.getFileName(), destination.getAbsolutePath(), duration};
  			msg = MessageFormat.format(msg, args );
  			fileLogger.info(msg);
       
  			File [] associatedFiles = a.getAssociatedFiles();
  			for (int i = 0; i < associatedFiles.length; i++) {
  				start = System.currentTimeMillis();
  				destination = ePimsDataProvider.getAssociatedFileDestinationDir(a, associatedFiles[i], a.getAssociatedFileType(associatedFiles[i]));
  				destination =  new File(destination, associatedFiles[i].getName());  	  		
  				FileUtils.secureCopy(associatedFiles[i], destination);
  				end = System.currentTimeMillis();
  				duration = (end-start)/1000;
  				String assMsg = RSCS.getString("copy.success");
  				Object[] assArgs = {associatedFiles[i].getName(), destination.getAbsolutePath(), duration};
      		 assMsg = MessageFormat.format(assMsg, assArgs );
      		 fileLogger.info(assMsg);
  			}	
  		}
  	} catch (FileNotFoundException e) {
       throw new BackupException("impossible de trouver le fichier pour l'analyse "+a.getName(), e);
    } catch (IOException e) {
       throw new BackupException("impossible d'ecrire le fichier de l'analyse "+a.getName(),e);
    }
		
	}

	
	public void move(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {
  	try {
  		
      File analysisFile = a.getFile();
  		long start = System.currentTimeMillis();
  		File destination = new File(a.getDestination(), a.getFileName());
  		      
  		boolean skipCopy = false;
  		if(destination.exists()){
  			if(allowManyAcquisitionInOneFile){
  				skipCopy = true;
  				logger.info("File for Analysis "+a.getName()+" already exist");
  			} else {
  				logger.warn("File for Analysis "+a.getName()+" already exist ! Can't copy acquisition ");
  				throw new BackupException("Analysis "+a.getName()+" already exist on PIMS-ROOT");
  			}
        if(analysisFile == null || ! analysisFile.exists()){
					logger.warn("File for analysis "+a.getName()+" can't be find/created ! Can't copy acquisition");
          throw new BackupException("Problem on analysisFile "+analysisFile+" for analysis "+a.getName()+". The file can't be reached or is null");
        }
  		}
      
  		if(! skipCopy){
           
  			ArrayList<File> filesToDel = new ArrayList<File>();
  			FileUtils.secureCopy(analysisFile, destination, a.getContentFilter());         
  			long end = System.currentTimeMillis();
  			long duration = (end-start)/1000;
      	String msg = RSCS.getString("copy.success");
      	Object[] args = {a.getFileName(), destination.getAbsolutePath(), duration};
      	msg = MessageFormat.format(msg, args );
      	fileLogger.info(msg);
      
      	filesToDel.add(analysisFile);
       
      	File [] associatedFiles = a.getAssociatedFiles();
      	for (int i = 0; i < associatedFiles.length; i++) {
      		start = System.currentTimeMillis();
      		destination = ePimsDataProvider.getAssociatedFileDestinationDir(a, associatedFiles[i], a.getAssociatedFileType(associatedFiles[i]));
      		destination =  new File(destination, associatedFiles[i].getName());  	  	
      		FileUtils.secureCopy(associatedFiles[i], destination);
      		end = System.currentTimeMillis();
      		duration = (end-start)/1000;
      		String assMsg = RSCS.getString("copy.success");
      		Object[] assArgs = {associatedFiles[i].getName(), destination.getAbsolutePath(), duration};
      		assMsg = MessageFormat.format(assMsg, assArgs );
      		fileLogger.info(assMsg);
      		filesToDel.add(associatedFiles[i]);
      	}
      
      	boolean result = delete(filesToDel);
      	if(!result){
      		String delMsg = RSCS.getString("delete.error");
      		Object[] delArgs = {a.getName()};
      		delMsg = MessageFormat.format(delMsg,delArgs );       
      		logger.debug(delMsg);
      		fileLogger.info(delMsg);
      	}
  		}
  	} catch (FileNotFoundException e) {
      throw new BackupException("impossible de trouver le fichier pour l'analyse "+a.getName(), e);
    } catch (IOException e) {
      throw new BackupException("impossible d'ecrire le fichier de l'analyse "+a.getName(),e);
    }

		
	}

	
	public void clean(Analysis a) throws BackupException {
    logger.info(" Suppression de "+a.getName());
    long start = System.currentTimeMillis();
    File analysisSrc = a.getFile();
    boolean delresult = deleteFile(analysisSrc);
    long end = System.currentTimeMillis();
    long duration = (end-start)/1000;
    if(delresult){
      String msg = RSCS.getString("clean.success");
      Object[] args = {a.getFileName(), duration};
      msg = MessageFormat.format(msg, args );
      fileLogger.info(msg);
    } else{
      String msg = RSCS.getString("clean.error");
      Object[] args = {a.getFileName()};
      msg = MessageFormat.format(msg, args );
      fileLogger.info(msg);
			throw new BackupException("Error cleaning analysisFile "+a.getFileName()+" for analysis "+a.getName()+". The file can't be deleted. It may be locked");
    }
    
    File [] associatedFiles = a.getAssociatedFiles();
		if(associatedFiles.length >0) {
			start = System.currentTimeMillis();
			delresult = delete(Arrays.asList(associatedFiles));
			end = System.currentTimeMillis();
			duration = (end - start) / 1000;
			if (delresult) {
				String msg = RSCS.getString("clean.success");
				Object[] args = {associatedFiles.length+" file(s) associated to "+a.getName(), duration};
				msg = MessageFormat.format(msg, args);
				fileLogger.info(msg);
			} else {
				String msg = RSCS.getString("clean.error");
				Object[] args = {associatedFiles.length+" file(s) associated to "+a.getName()};
				msg = MessageFormat.format(msg, args);
				fileLogger.info(msg);
			}
		}
	}


  private boolean delete(List<File> files){
  	boolean allDeletable = true;
  	for(int i=0; i< files.size(); i++){
  		File f = files.get(i);
  		allDeletable = allDeletable && checkDeletable(f);
  		logger.debug(" Result check File "+f.getName()+" deletable "+allDeletable);
  	}
     
  	if(!allDeletable){
  		return false;
  	}	
  	
  	logger.debug(" Start DELETE ");

  	boolean succes = true;
  	for(int i=0; i< files.size(); i++){
  		logger.debug(" Delete file "+files.get(i).getName());
  		succes = succes && deleteFile(files.get(i));
  	}
     
  	return succes;
  }
   
  private boolean deleteFile(File file){
  	boolean result= true; 
    if(file.isDirectory()){
      File[] files = file.listFiles();
      for(int i=0; i<files.length; i++){
        result = result && deleteFile(files[i]);
      }
    }
          
    result = result && file.delete();
    return result;
  }
   
  private boolean checkDeletable(File f){
    boolean deletable = true;
    if(f.isDirectory()){
      File[] files = f.listFiles();
      for(int i=0; i<files.length; i++){
        deletable = deletable && checkDeletable(files[i]);
      }
    }else
      deletable = f.canWrite();
    return deletable;
  }	
	
}
