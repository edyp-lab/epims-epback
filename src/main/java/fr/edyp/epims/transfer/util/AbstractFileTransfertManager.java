package fr.edyp.epims.transfer.util;

import fr.edyp.epims.transfer.log.LogTextPanel;
import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.IFileTransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class AbstractFileTransfertManager implements IFileTransferManager {

  protected static final Logger logger = LoggerFactory.getLogger(AbstractFileTransfertManager.class);
  protected static final Logger fileLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  protected static ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());

  public AbstractFileTransfertManager(){

  }

  protected abstract String getDestinationPath(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws IOException;
  protected abstract boolean destinationExist(String destPath);
  protected abstract String getAssociatedDataDestinationPath(Analysis a, File f, String fileType, IEPSystemDataProvider ePimsDataProvider)throws IOException;

  protected abstract void doAnalysisCopy(Analysis a, String destPath)throws IOException;
  protected abstract void doAssociatedDataCopy(File src, String destPath)throws IOException;
  protected abstract void afterCopy();

  public void copyOnly(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {
    //TODO VD : REMOVE copied file in case of error
    try {
      File analysisFile = a.getFileToTransfer();
      if(analysisFile == null) {
        throw new BackupException("Problem on analysisFile for analysis " + a.getName() + ". The file is undefined (null)");
      }

      logger.debug("Copy File for Analysis {}: {}", a.getName(), analysisFile.getAbsolutePath());
      long start = System.currentTimeMillis();
      String destinationPath = getDestinationPath(a,ePimsDataProvider);
      logger.debug("Analysis File destination= {}", destinationPath);

      if(destinationExist(destinationPath)){
        String warnMsg ="File for Analysis "+a.getName()+" already exist ! Can't copy acquisition ";
        logger.warn(warnMsg);
        fileLogger.warn(warnMsg);
        throw new BackupException("Analysis "+a.getName()+" already exist on PIMS-ROOT");
      }

      if(! analysisFile.exists()){
        String warnMsg ="File for analysis "+a.getName()+" can't be find/created ! Can't copy acquisition";
        logger.warn(warnMsg);
        fileLogger.warn(warnMsg);
        throw new BackupException("Problem on analysisFile "+analysisFile+" for analysis "+a.getName()+". The file can't be reached or is null");
      }

      logger.debug(" Copy only {} to {}", analysisFile, destinationPath);
      doAnalysisCopy(a, destinationPath);
      long end = System.currentTimeMillis();
      long duration = (end-start)/1000;
      String msg = RSCS.getString("copy.success");
      Object[] args = {a.getFileName(), destinationPath, duration};
      msg = MessageFormat.format(msg, args );
      fileLogger.info(msg);

      File [] associatedFiles = a.getAssociatedFiles();
      for (int i = 0; i < associatedFiles.length; i++) {
        start = System.currentTimeMillis();
        destinationPath = getAssociatedDataDestinationPath(a, associatedFiles[i], a.getAssociatedFileType(associatedFiles[i]), ePimsDataProvider);
        doAssociatedDataCopy(associatedFiles[i], destinationPath);
        end = System.currentTimeMillis();
        duration = (end-start)/1000;
        String assMsg = RSCS.getString("copy.success");
        Object[] assArgs = {associatedFiles[i].getName(), destinationPath, duration};
         assMsg = MessageFormat.format(assMsg, assArgs );
         fileLogger.info(assMsg);
      }

  	} catch (IOException e) {
			e.printStackTrace();
       throw new BackupException("Erreur lors de la copie de l'analyse "+a.getName(), e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BackupException("impossible d'ecrire le fichier de l'analyse "+a.getName(),e);
    }
    finally {
      afterCopy();
    }
  }

  public void move(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {
    try {

      File analysisFile = a.getFileToTransfer();
      if(analysisFile == null) {
        throw new BackupException("Problem on analysisFile for analysis " + a.getName() + ". The file is undefined (null)");
      }

      logger.debug("Move File for Analysis {}: {}", a.getName(), analysisFile.getAbsolutePath());
      long start = System.currentTimeMillis();
      String destinationPath = getDestinationPath(a,ePimsDataProvider);
      logger.debug("Analysis File destination= {}", destinationPath);

      if(destinationExist(destinationPath)){
        logger.warn("File for Analysis {} already exist ! Can't copy acquisition ", a.getName());
        throw new BackupException("Analysis "+a.getName()+" already exist on PIMS-ROOT");
      }

      if(! analysisFile.exists()){
        logger.warn("File for analysis {} can't be find/created ! Can't copy acquisition", a.getName());
        throw new BackupException("Problem on analysisFile "+analysisFile+" for analysis "+a.getName()+". The file can't be reached or is null");
      }


      doAnalysisCopy(a, destinationPath);
      long end = System.currentTimeMillis();
      long duration = (end-start)/1000;
      String msg = RSCS.getString("copy.success");
      Object[] args = {a.getFileName(), destinationPath, duration};
      msg = MessageFormat.format(msg, args );
      fileLogger.info(msg);

      ArrayList<File> filesToDel = new ArrayList<>();
      filesToDel.add(a.getFileToTransfer());

      File [] associatedFiles = a.getAssociatedFiles();
      for (int i = 0; i < associatedFiles.length; i++) {
        start = System.currentTimeMillis();
        destinationPath = getAssociatedDataDestinationPath(a, associatedFiles[i], a.getAssociatedFileType(associatedFiles[i]), ePimsDataProvider);
        doAssociatedDataCopy(associatedFiles[i], destinationPath);
        end = System.currentTimeMillis();
        duration = (end-start)/1000;
        String assMsg = RSCS.getString("copy.success");
        Object[] assArgs = {associatedFiles[i].getName(), destinationPath, duration};
        assMsg = MessageFormat.format(assMsg, assArgs );
        fileLogger.info(assMsg);
        filesToDel.add(associatedFiles[i]);
      }

      boolean result = FileUtils.deleteAllFilesOrNone(filesToDel);
      if(!result){
        String delMsg = RSCS.getString("delete.error");
        Object[] delArgs = {a.getName()};
        delMsg = MessageFormat.format(delMsg,delArgs );
        logger.debug(delMsg);
        fileLogger.info(delMsg);
      }

    } catch (FileNotFoundException e) {
      throw new BackupException("impossible de trouver le fichier pour l'analyse "+a.getName(), e);
    } catch (IOException e) {
      throw new BackupException("impossible d'ecrire le fichier de l'analyse "+a.getName(),e);
    }
    finally {
      afterCopy();
    }

  }


  public void clean(Analysis a) throws BackupException {
    try {

      logger.info(" Suppression de {}", a.getName());
      long start = System.currentTimeMillis();

      // Clean original analysis file
      ArrayList<File> filesToDel = new ArrayList<>();
      filesToDel.add(a.getSourceFile());
      boolean delresult = FileUtils.deleteAllFilesOrNone(filesToDel);
      long end = System.currentTimeMillis();
      long duration = (end - start) / 1000;
      if (delresult) {
        String msg = RSCS.getString("clean.success");
        Object[] args = {a.getName(), duration};
        msg = MessageFormat.format(msg, args);
        fileLogger.info(msg);
      } else {
        String msg = RSCS.getString("clean.error");
        Object[] args = {a.getName()};
        msg = MessageFormat.format(msg, args);
        fileLogger.info(msg);
        throw new BackupException("Error cleaning analysisFile " + a.getSourceFile() + " for analysis " + a.getName() + ". The file can't be deleted. It may be locked");
      }

      File[] associatedFiles = a.getAssociatedFiles();
      if (associatedFiles.length > 0) {
        start = System.currentTimeMillis();
        delresult = FileUtils.deleteAllFilesOrNone(Arrays.asList(associatedFiles));
        end = System.currentTimeMillis();
        duration = (end - start) / 1000;
        if (delresult) {
          String msg = RSCS.getString("clean.success");
          Object[] args = {associatedFiles.length + " file(s) associated to " + a.getName(), duration};
          msg = MessageFormat.format(msg, args);
          fileLogger.info(msg);
        } else {
          String msg = RSCS.getString("clean.error");
          Object[] args = {associatedFiles.length + " file(s) associated to " + a.getName()};
          msg = MessageFormat.format(msg, args);
          fileLogger.info(msg);
        }
      }
    } finally {
      afterCopy();
    }
  }

}
