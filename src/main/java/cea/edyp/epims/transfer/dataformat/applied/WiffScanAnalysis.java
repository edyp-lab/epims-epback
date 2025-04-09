/*
 * Created on Dec 7, 2004
 *
 * 
 */
package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.MultiFilesAnalysis;

/** 
 *  s
 * 
 * @author CB205360
 */
public class WiffScanAnalysis extends  QTrapAnalysis implements MultiFilesAnalysis {

  private List<File> allAcqFiles;
  private File zipFile = null;
  
  public WiffScanAnalysis(File f, WiffScanFormat format) {
  	super(f, format);
  	allAcqFiles = new ArrayList<File>(0);
  	allAcqFiles.add(f);
	}
  
  public WiffScanAnalysis(List<File> f, WiffScanFormat format) {
  	this(f.get(0), format);
  	allAcqFiles = f;
  	if(f.size() > 1){
	  	estimatedSize = 0;
	  	for (File nextF : allAcqFiles){
				estimatedSize += nextF.length();
	  	}
	  	zipFile = new File(analysisFile.getParentFile(),analysisFile.getName()+".zip");
  	}
  }
  
  public File getFile() {
  	if(zipFile == null)
  		return super.getFile();
  	else
  		return zipFile;
  }

  public String getFileName(){
  	if(zipFile == null && analysisFile==null)
  		return "INVALID FILE";
  	else if(zipFile == null)
  		return analysisFile.getName();
  	else 
  		return zipFile.getName();
  }
  
  protected void init(QTrapFormat format){
  	setDataFormat(format);
  	status = ANALYSIS_STATUS_UNKNOWN;
  	if(analysisFile != null) {
  		name = analysisFile.getName().substring(0,analysisFile.getName().lastIndexOf('.'));
  		acqDate = new Date(analysisFile.lastModified());
  		estimatedSize = analysisFile.length();
  	} 
    determineType();
  }

  public void setDataFormat(DataFormat format){
  	if(format instanceof WiffScanFormat)
  		dataFormat = (WiffScanFormat)format;  
  }
  
	public void setDestination(String destinationDir) {
		super.setDestination(destinationDir);
		zipFile = new File(destinationDir,analysisFile.getName()+".zip");	
	}
	

	public List<File> getAllAcquisitionFile() {	
		return allAcqFiles;
	}

	public boolean keepRelativePath(){
		return false;
	}
}
