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
/*
 * Created on Dec 9, 2004
 *
 * $Id: AnalysisOperationMgr.java,v 1.2 2008-02-20 06:58:43 dupierris Exp $
 */
package fr.edyp.epims.transfer.model;

import java.util.Locale;
import java.util.ResourceBundle;

import fr.edyp.epims.transfer.dataformat.nems.NemsAnalysis;
import fr.edyp.epims.transfer.util.FileUtils;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.perf4j.StopWatch;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class AnalysisOperationMgr {

   private static final Logger logger = LoggerFactory.getLogger(AnalysisOperationMgr.class);
   private static final ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());
      
   private final BackupParameters parameters;
   
   public AnalysisOperationMgr(BackupParameters params) {
      parameters = params;   
   }
   
   public void transfer(Analysis a)throws BackupException {
     if(parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE)
       copy(a);
     else {
       if(!(a instanceof NemsAnalysis))// Nems special case: zip is already suppressed, and source files must not be suppressed
         parameters.getDataFormat().getFileTransfertManager().clean(a);
     }
   }
   
  public void copy(Analysis a) throws BackupException {
	  StopWatch stopWatch = new Slf4JStopWatch("analysis.transfert");
    logger.info(" Copie de {}", a.getName());
    verifyParameter(a);
    stopWatch.lap("analysis.transfert.verify");
    if (parameters.removeFilesAfterCopy() || a.isTransferFileTempo()) {
        parameters.getDataFormat().getFileTransfertManager().move(a, parameters.getEPimsDataProvider());
        if(parameters.removeFilesAfterCopy() && a.isTransferFileTempo() && a.getSourceFile().exists()) {//only temporary files were moved. remove source also
          boolean result = FileUtils.deleteFileOrDir(a.getSourceFile()); //Should not occur, option disabled. To be removed
          if(!result)
            logger.error("Failed to delete source file {}", a.getSourceFile().getAbsolutePath());
        }
    }
    else {
        parameters.getDataFormat().getFileTransfertManager().copyOnly(a, parameters.getEPimsDataProvider());
    }
    logger.debug("Copy Done, set analysis Saved");
    // Comment for tests
    createPimsObjects(a);
    stopWatch.lap("analysis.transfert.copy");
    parameters.getEPimsDataProvider().getAnalysisStatus(a,parameters);
    stopWatch.stop();
  }
   
     
  private void verifyParameter(Analysis a) throws BackupException {
    logger.debug(" verifyParameter for {}", a.getName());
  	
  	if(a.getType()==Analysis.AnalysisType.RESEARCH){       		
  		if(!parameters.getEPimsDataProvider().isSampleExist(a.getSample()) || (parameters.getEPimsDataProvider().getStudyNameFor(a.getSample()) == null)){
  			logger.debug(" Analyse and invalid sample => BackupExeption ");
  			throw new BackupException("Echantillon non spécifié ou invalide");
  		}
  	}
  	    
  	String instrumentName =parameters.getInstrumentName();  	
    if(!parameters.getEPimsDataProvider().isSpectrometerDefined(parameters.getInstrumentName())){
      logger.debug(" No spectro {} => BackupExeption ", instrumentName);
      throw new BackupException("L'instrument sur lequal a été réalisé l'analyse "+a.getName()+" n'est pas défini dans PIMS.");
    }

    if( (a.getDestination() == null || a.getDestination().equals(RSCS.getString("file.invalid.dest.path")))
    		&& parameters.getEPimsDataProvider().getDestinationDir(a, parameters) == null) {
    	logger.debug(" No dest file => BackupExeption ");
    	throw new BackupException("impossible de déterminer le répertoire de destination de l'analyse "+a.getName());
    }
    
    logger.debug(" verify DONE OK");
  }
  

  
  private void createPimsObjects(Analysis a) throws BackupException{

    logger.debug(" createPimsObjects {}", a.getName());
    
  	String spectroName = parameters.getInstrumentName();  	   
  	parameters.getEPimsDataProvider().createAcquisitionAndFilesFor(a, spectroName);
  }
     
   
}
