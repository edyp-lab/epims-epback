/*
 * Created on Nov 26, 2004
 *
 * $Id: MLFormat.java,v 1.1 2007-09-14 09:37:31 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.applied;


import java.awt.GridBagLayout;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import cea.edyp.epims.transfer.util.ExtensionFileFilter;
import org.slf4j.LoggerFactory;

/**
 * @author DB217215
 */
public class Maldi4800Format extends JPanel implements DataFormat {

	private static final long serialVersionUID = -5532035357716130392L;
	
	private static Logger logger = LoggerFactory.getLogger(Maldi4800Format.class);
	private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	
	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	private static final String DESCRIPTION_PROP = "Description";
	private static final String USER_PROP = "Resp. instrument";
	private static final String ZIP_FILE_PROP = "Fichier zip";
	
	//Maldi4800 format specific propertie titles
	//the JWIM that stands for jobWideInterpretMethod
	private static final String JWIM = "jobWideInterpretMethod";
	//make the SpottedPlate information appear in a column
	private static final String SPOTTED_PLATE = "Spotted Plate";
	//make the acq type appear in a column
	private static final String ACQ_TYPE = "Acq Type"; 
	//make the spotDescription appear in a column
	private static final String SPOTS = "Spots";
	//make the jobRunDescription appear in a column
	private static final String JOBRUN_DESCRIPTION = "JobRun Description";	
	
	private static final String ANAL_DESC_FILE_EXT = "xml";
	private static final String DATA_FILES_DIR = "t2d";
	
	//former list of properties
	//private static final String[] PROPERTIES = { DESCRIPTION_PROP, USER_PROP, ZIP_FILE_PROP };
	private static final String[] PROPERTIES = { DESCRIPTION_PROP, USER_PROP, ZIP_FILE_PROP, SPOTTED_PLATE, SPOTS, ACQ_TYPE, JOBRUN_DESCRIPTION, JWIM };

	private FileFilter dataFilter;
	protected Maldi4800Factory analysisFactory;
	private File srcDir;


	public Maldi4800Format() {
		String[] ext = {ANAL_DESC_FILE_EXT};
		dataFilter = new ExtensionFileFilter(ext);
		analysisFactory = new Maldi4800Factory();
		
		
		setLayout(new GridBagLayout());
   }

   ////////////////////
   // DATAFORMAT methods
   ////////////////////
	public IFileTransfertManager getFileTransfertManager() {
		return new DefaultFileTransfertManager(false );
	}
	
	public Analysis[] getAnalysis(File srcDir) {
		this.srcDir = srcDir;
		logger.info("reading directory "+srcDir.getAbsolutePath());
		File[] files = srcDir.listFiles(dataFilter);
		
		//if the listFiles() return null the directory doesn't exist (or there is an I/O error)
		if(files == null){
			String msg = RSCS.getString("acq.dir.notexist");
			Object[] args = {srcDir}; 
			logPaneLogger.error(MessageFormat.format(msg, args));
			return new Analysis[0];
		}
		return analysisFactory.getAnalyses(this, Arrays.asList(files));
	}

	public int getPropertyCount() {    
		return PROPERTIES.length;
	}

	@SuppressWarnings("rawtypes")
	public Class getPropertyClass(int propertyIdx) {
		return String.class;
	}

	public String getPropertyLabel(int propertyIdx) {
		if (propertyIdx < PROPERTIES.length)
			return PROPERTIES[propertyIdx];
		return "";
	}

	public Object getProperty(int propertyIdx, Analysis analysis) {
		Maldi4800Analysis maldi4800Analysis = (Maldi4800Analysis) analysis;
		if (propertyIdx < PROPERTIES.length) {
			String propertyName = PROPERTIES[propertyIdx];
			if (propertyName.equals(DESCRIPTION_PROP))
				return maldi4800Analysis.getDescription();
			if(propertyName.equals(USER_PROP))
				return maldi4800Analysis.getOperator();
			if(propertyName.equals(ZIP_FILE_PROP))
				return maldi4800Analysis.getDataFileState();
			//set way to retrieve JWIM information
			if(propertyName.equals(JWIM))
				return maldi4800Analysis.getJobWideInterpretMethod();
			//set way to retrieve spotted plate name
			if(propertyName.equals(SPOTTED_PLATE))
				return maldi4800Analysis.getSpottedPlate();
			//set way to retrieve acqType
			if(propertyName.equals(ACQ_TYPE))
				return maldi4800Analysis.getAcqType();
			//set way to retrieve Spots
			if(propertyName.equals(SPOTS))
				return maldi4800Analysis.getSpotDescription();
			//set way to retrieve jobrun descripiton
			if(propertyName.equals(JOBRUN_DESCRIPTION))
				return maldi4800Analysis.getJobRunDescription();
		}
		return null;
	}

	public JComponent getConfigurator() {
		return this;
	}

	public String getDataFilesDir(){
		return DATA_FILES_DIR;
	}
   
	public File getSrcDir() {
		return srcDir;
	}

}
