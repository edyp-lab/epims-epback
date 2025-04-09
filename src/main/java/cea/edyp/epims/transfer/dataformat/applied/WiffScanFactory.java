package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.wiff.reader.AppliedAnalysis;
import cea.edyp.wiff.reader.WiffFile;
import org.slf4j.LoggerFactory;


public class WiffScanFactory extends QTrapFactory {

	private static Logger logger = LoggerFactory.getLogger(WiffScanFactory.class);
	private static Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	public List<Analysis> createAnalysis(File file) {
		
		List<Analysis> analysis = new ArrayList<Analysis>();
		
		if(file != null && validAnalysisFile(file.getAbsolutePath()) ){
			logger.info("- WiffScanFactory createAnalysis");					
			String msg = RSCS.getString("analysis.creation.info");
			Object[] args = {file.getAbsolutePath()};
			msg = MessageFormat.format(msg, args);	
			mainLogger.info(msg);
			logger.info(msg);
			
			WiffFile wiffFile = new WiffFile(file.getAbsolutePath());
			wiffFile.openFS();
			
			//One wiff file can contain many analysis... SHOULD NOT OCCUR !
			int nbrAnalysis = wiffFile.getSamplesCount();
			for (int k = 1; k <= nbrAnalysis; k++) {
				AppliedAnalysis nextAppliedAnalysis = wiffFile.getAnalysisDeducingValues(k);
				if(nextAppliedAnalysis==null){
					msg = RSCS.getString("analysis.creation.error");
					Object[] twoArgs = {file.getAbsolutePath()+" index "+k, RSCS.getString("unknown.error.message")};
					msg = MessageFormat.format(msg, twoArgs);
					mainLogger.info(msg);
					continue;
				}
				
				List<File> analyseFile = wiffFile.getWiffAcquisitionFiles();				
				logger.debug("  analyseFile for current File "+nextAppliedAnalysis.getAnalysisName()+" => "+analyseFile.get(0).getAbsolutePath()+". Nbr files = "+analyseFile.size());
				
				WiffScanAnalysis epimsAnalysis = new WiffScanAnalysis(analyseFile, (WiffScanFormat) format);
				epimsAnalysis.setName(nextAppliedAnalysis.getAnalysisName());
				epimsAnalysis.setOperator(nextAppliedAnalysis.getOperator());	  
				epimsAnalysis.setSample(nextAppliedAnalysis.getSampleName());
				epimsAnalysis.setDescription(nextAppliedAnalysis.getDescription());

				analysis.add(epimsAnalysis);  
			} //End go through all analysis of current wiff file
			
			wiffFile.closeFS();     
			
		} 
	  
		return analysis;
	}

	
	private boolean validAnalysisFile(String fileName){
    int dotIndex = fileName.lastIndexOf('.');
    if(dotIndex == -1)
      return false;
    String extension = fileName.substring(dotIndex+1);
    if (extension != null) {
      return extension.equals(WiffScanFormat.ANALYSIS_FILE_EXT);
    }
    return false;
	}
}
