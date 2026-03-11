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

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.util.ExtensionFileFilterName;

public class WiffScanFormat  extends QTrapFormat {
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ANALYSIS_FILE_EXT2="scan";
	@SuppressWarnings("unused")
	private static Log logger = LogFactory.getLog(WiffScanFormat.class);
	
	
  public WiffScanFormat(){
  	super();
    String[] ext = { ANALYSIS_FILE_EXT,ANALYSIS_FILE_EXT2 };
    dataFilter = new ExtensionFileFilterName(ext);
    analysisFactory = new WiffScanFactory();

  }

  ////////////////////
  // DATAFORMAT methods
  ////////////////////
//	@Override
//	public IFileTransferManager getFileTransfertManager() {
//		return new ZipFileTransferManager( );
//	}
     
  public JComponent getConfigurator() {
    return this;
  }

  public Object getProperty(int propertyIdx, Analysis analysis) {
  	
    WiffScanAnalysis wiffScanAnalysis = (WiffScanAnalysis) analysis;
    if (propertyIdx < FORMAT_PROPERTIES.length && FORMAT_PROPERTIES[propertyIdx].equals(FILE_PROPERTY)) {
    		return wiffScanAnalysis.getFileName();
    } else
    	return super.getProperty(propertyIdx, analysis);    
  }



  
}
