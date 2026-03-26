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
package fr.edyp.epims.transfer.dataformat.thermo;

import cea.edyp.jxcalibur2.InvalidRawFormatException;
import cea.edyp.jxcalibur2.JXAnalysis21;
import fr.edyp.epims.transfer.log.LogTextPanel;
import fr.edyp.epims.transfer.model.AbstractCacheFactory;
import fr.edyp.epims.transfer.model.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Create Thermo Analysis from specified source path.
 * Analysis are created using ThermoAccess(based on ThermoFisher libraries) project
 *
 */
public class LTQFactoryOld extends AbstractCacheFactory {

	protected static Logger logger = LoggerFactory.getLogger(LTQFactoryOld.class);
  protected static Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  protected static ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());


	public LTQFactoryOld() {

	}


  @Override
  public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
    throw new UnsupportedOperationException("Not supported Operation");
  }


	public List<Analysis> createAnalysis(File f) {
	  String msg = RSCS.getString("analysis.creation.info");
    Object[] args = {f.getAbsolutePath()};
    msg = MessageFormat.format(msg, args);
    mainLogger.info(msg);
    logger.info(msg);
    
    if (!f.exists() || f.isDirectory()){
      logger.debug("Invalid LTQ File specified");
      throw new IllegalArgumentException("Invalid LTQ File specified");
    }

    LTQAnalysis analysis = new LTQAnalysis(f, (AbstractLTQFormat)format);
    try {
      fillAnalysisInfo(f, analysis);
    } catch (Exception exp) {
      logger.error("erreur de lecture du fichier " + f.getAbsolutePath());
      msg = RSCS.getString("analysis.creation.error");
      Object[] twoArgs = {f.getAbsolutePath(), exp.getMessage()};           
      msg = MessageFormat.format(msg, twoArgs);
      mainLogger.info(msg);
      
      analysis = null;
    }
    
    List<Analysis> a = new ArrayList<>();
    if(analysis != null)
      a.add(analysis);
    return a;
	}

  protected void fillAnalysisInfo(File f, LTQAnalysis analysis) throws InvalidRawFormatException {
    JXAnalysis21 rawAnalysis = new JXAnalysis21(f);
    analysis.setDate(rawAnalysis.getInformations().getAcquisitionDate());
    analysis.setSample(rawAnalysis.getInformations().getSampleName());
    analysis.setDescription(rawAnalysis.getInformations().getSampleDescription());
    analysis.setOperator(rawAnalysis.getInformations().getCustomLabels()[1]);
//    double d =   (rawAnalysis.getMSSurvey().getEndTime() - rawAnalysis.getMSSurvey().getStartTime());
//    if (d < 0)
//      d = 0.0;
    analysis.setDuration(Float.valueOf( "0.0"));

  }


}
