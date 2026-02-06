/*
 * Created on Dec 7, 2004
 *
 * 
 */
package fr.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.util.Date;
import java.util.List;

import fr.edyp.epims.transfer.model.DataFormat;

/** 
 *  s
 * 
 * @author CB205360
 */
public class WiffScanAnalysis extends  QTrapAnalysis  {

  private List<File> allAcqFiles;
  private String zipFilename = null;
	private File zipFile = null;
  
  public WiffScanAnalysis(File f, WiffScanFormat format) {
  	super(f, format);
		initZipFile(List.of(f));
	}
  
  public WiffScanAnalysis(List<File> f, WiffScanFormat format) {
  	this(f.get(0), format);
  	initZipFile(f);
		zipFilename =analysisFile.getName()+".zip";
  }

	private void initZipFile(List<File> f)  {
		allAcqFiles = f;
		if(!allAcqFiles.isEmpty()){
			estimatedSize = 0;
			for (File nextF : allAcqFiles){
				estimatedSize += nextF.length();
			}
		}
	}

	public String getZipFilename() {
		return zipFilename;
	}
	@Override
	public File getFileToTransfer() {
  	if(zipFilename == null)
  		return super.getFileToTransfer();
  	else {
			if(zipFile == null ) {
				try {
					if (!allAcqFiles.isEmpty()) {
						zipFile = new File(analysisFile.getParentFile(),zipFilename);
						boolean zipCreateSucess = fr.edyp.epims.transfer.util.FileUtils.copyFilesToOneZip(zipFile, allAcqFiles, analysisFile.getParentFile(), true);
						if(zipCreateSucess) {
							estimatedSize = zipFile.length();
						} else {
							zipFile = null;
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return zipFile;
		}
  }


  @Override
  public boolean isTransferFileTempo() {
    return zipFile != null;
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

}
