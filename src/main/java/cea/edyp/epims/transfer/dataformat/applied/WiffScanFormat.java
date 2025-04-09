package cea.edyp.epims.transfer.dataformat.applied;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.ExtensionFileFilterName;
import cea.edyp.epims.transfer.util.ZipFileTransfertManager;

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
	@Override
	public IFileTransfertManager getFileTransfertManager() {
		return new ZipFileTransfertManager(false );
	}
     
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
