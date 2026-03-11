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
 * Created on Dec 7, 2004
 *
 * $Id: LTQAnalysis.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package fr.edyp.epims.transfer.dataformat.thermo;

import java.io.File;
import java.io.FileFilter;

import fr.edyp.epims.transfer.model.AbstractAnalysis;
import org.apache.commons.io.FilenameUtils;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.DataFormat;
import fr.edyp.epims.transfer.util.FileUtils;

/**
 * 
 * 
 * @author CB205360
 */
public class LTQAnalysis extends AbstractAnalysis {

  private static final String MGF_EXTENTION = "mgf";
  private static final String MGF_DIR = "dta";
  
  private LTQFormat dataFormat;

  public LTQAnalysis(File f, LTQFormat format) {
    analysisFile = f;
    estimatedSize = analysisFile.length();
    status = ANALYSIS_STATUS_UNKNOWN;
    dataFormat = format;
    analyseType = AnalysisType.UNKNOWN;
    setName( FilenameUtils.getBaseName(analysisFile.getName()));
  }
  
  public void setDataFormat(DataFormat format){
  	if(format instanceof LTQFormat)
  		dataFormat = (LTQFormat)format;  
  }

  @Override
  public String getName(){
    if(name == null && analysisFile!=null)
      setName(FilenameUtils.getBaseName(analysisFile.getName()));
    return name;
  }

  @Override
  public long getEstimatedSize() {
    if(estimatedSize == 0L && analysisFile!=null)
      estimatedSize = analysisFile.length();
    return estimatedSize;
  }

  public String getFileName(){
    return analysisFile.getName();
  }

  public FileFilter getContentFilter() {
    return new LTQFilter();
  }

  public File[] getAssociatedFiles() {
    if(dataFormat.areSpectraHandled() && analyseType != Analysis.AnalysisType.BLANK &&
        analyseType  != Analysis.AnalysisType.CONTROL_INSTRUMENT 
        && analyseType  != Analysis.AnalysisType.CONTROL_LC){
    	
    	if(associatedFiles != null)
    		return associatedFiles.toArray(new File[0]);
    	
    	File dir = analysisFile.getParentFile();      
      StringBuilder nameSB = new StringBuilder(MGF_DIR);
      nameSB.append(File.separator);
      nameSB.append(getName());
      nameSB.append(".");
      nameSB.append(MGF_EXTENTION);
      
      File mgfFile = new File(dir, nameSB.toString());
      if(mgfFile.exists() && mgfFile.canRead()){
        associatedFiles.add(mgfFile);
        return associatedFiles.toArray(new File[0]);
      }
      
      //Try with UpperCase
      nameSB = new StringBuilder(MGF_DIR.toUpperCase());
      nameSB.append(File.separator);
      nameSB.append(getName());
      nameSB.append(".");
      nameSB.append(MGF_EXTENTION.toUpperCase());
      mgfFile = new File(dir, nameSB.toString());
      if(mgfFile.exists() && mgfFile.canRead()){
        associatedFiles.add(mgfFile);
        return associatedFiles.toArray(new File[0]);
      }
    }
    
    return new File[0];
  }

  public String getAssociatedFileType(File associatedFile) {
    if(!dataFormat.areSpectraHandled())
      return null;
    
    String ext = FileUtils.getExtension(associatedFile); 
    if(ext == null || !ext.equalsIgnoreCase(MGF_EXTENTION)){
      return null;
    }
    
    return SPECTRA_FILETYPE;
  }

  public String toString() {
    return getName();
  }

  class LTQFilter implements FileFilter {

    /* (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File pathname) {
      return pathname.equals(LTQAnalysis.this.analysisFile);
    }
  }

}
