/*
 * Created on Nov 26, 2004
 *
 * $Id: BackupParameters.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.collections4.CollectionUtils;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.perf4j.StopWatch;

import cea.edyp.epims.transfer.util.IEPSystemDataProvider;
import cea.edyp.epims.transfer.util.WSSystemDataProvider;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class BackupParameters implements PropertyChangeListener {

	/**
	 * Specify that transfer mode is set to copy. Transfer will copy data from
	 * source path (instruments PC) to SAN directory.
	 */
	public final static int TRANSFER_COPY_MODE = 0;

	/**
	 * Specify that transfer mode is set to clean. Transfer will remove data from
	 * source path (instruments PC). No modification will be done on SAN
	 */
	public final static int TRANSFER_CLEAN_MODE = 1;

	/**
	 * Property representing the mode of transfer to execute
	 */
	public final static String TRANSFERT_MODE_PROPERTY = "transfert mode";

	/**
	 * Property which specify if files should be removed after copy or not
	 */
	public final static String REMOVE_FILES_PARAMETER = "remove files after copy";

	/**
	 * Property representing the source path of data files
	 */
	public final static String SRC_PATH_PARAMETER = "source path";

	/**
	 * Property specifying the log file to use while transfering
	 */
	public final static String LOG_FILE_PARAMETER = "log file";

	/**
	 * Property representing the selected Instrument
	 */
	public final static String INSTRUMENT_CONFIGURATION_PROPERTY= "instrument configuration";
	
	/**
	 * Property representing the list of analysis
	 */
	public final static String ANALYSES_PROPERTY= "analyses";

	/**
	 * Property representing the list of analysis
	 */
	public final static String ANALYSES_REMOVED_PROPERTY= "removed analyses";
	
	/**
	 * Property representing the list of analysis
	 */
	public final static String ANALYSES_ADDED_PROPERTY= "added analyses";

	
	/**
	 * Property representing the DataFormat of the analysis
	 */
	public final static String DATA_FORMAT_PARAMETER = "data format";

	/**
	 * Property representing the configurations parameter of the DataFormat.
	 */
	public final static String DATA_FORMAT_CONFIGURATION_PARAMETER = "data format configuration";
	
	public final static String BACKUP_RUNNING_PROPERTY = "is running";

	private static final Logger logger = LoggerFactory.getLogger(BackupParameters.class);
	
	private File sourcePath;
	private File destinationRootPath;
	private int transferMode;
	private boolean removeFilesAfterCopy;
	private File logFile;
	private InstrumentConfiguration instrumentConfiguration;
	private DataFormat dataFormat;
	private IEPSystemDataProvider dataProvider;
	private boolean xferRunning;
   
	private PropertyChangeSupport propertiesSupport;
	private List<Analysis> analyses;


	public BackupParameters() throws InstantiationException {
		propertiesSupport = new PropertyChangeSupport(this);
		dataProvider = new WSSystemDataProvider();
//		dataProvider = new FakeDataProvider();
		
		destinationRootPath = new File(dataProvider.getPimsRootPath());
		analyses = null;
		xferRunning = false;
	}
	
	public void setIsRunning(boolean isRunning){
		if(isRunning != xferRunning){
			xferRunning	= isRunning; 
			propertiesSupport.firePropertyChange(BackupParameters.BACKUP_RUNNING_PROPERTY, !xferRunning, xferRunning);
		}
	}

	public void setInstrumentConfiguration(InstrumentConfiguration configuration) {
		InstrumentConfiguration prevInstrumentConfig = instrumentConfiguration;
		this.instrumentConfiguration = configuration;
		StringBuffer logFileBuffer = new StringBuffer(dataProvider.getPimsRootPath());
		logFileBuffer.append(File.separator);
		logFileBuffer.append(dataProvider.getPimsSystemRelativePath());
		File prevLogFile = logFile;
		logFile = new File(logFileBuffer.toString(), instrumentConfiguration.getName() + ".log");
		DataFormat prevDF = dataFormat;
		dataFormat = DataFormatFactory.getDataFormat(instrumentConfiguration.getFormat());
		sourcePath = new File(instrumentConfiguration.getSourcePath());
		analyses = null;
		logger.debug("Selected instrument : "+instrumentConfiguration.getName());
		propertiesSupport.firePropertyChange(INSTRUMENT_CONFIGURATION_PROPERTY, prevInstrumentConfig, instrumentConfiguration);
		propertiesSupport.firePropertyChange(LOG_FILE_PARAMETER, prevLogFile, logFile);
		dataFormat.addPropertyChangeListener(this);
		propertiesSupport.firePropertyChange(DATA_FORMAT_PARAMETER, prevDF, dataFormat);
	}
	
	
	public void loadAnalysesFromSourcePath() {
		if (sourcePath.exists() && sourcePath.isDirectory() && sourcePath.canRead()) {
			StopWatch stopWatch = new Slf4JStopWatch("analyses from SourcePath");
			logger.info("Loading Analysis from "+sourcePath.getAbsolutePath()+" for instrument "+instrumentConfiguration.getName());
			analyses = Arrays.asList(getDataFormat().getAnalysis(sourcePath));
			stopWatch.stop();
			logger.info("Loading Analysis done : "+analyses.size()+" analyses created");
			try {
				SwingUtilities.invokeAndWait(new Runnable() {				
					public void run() {
						propertiesSupport.firePropertyChange(ANALYSES_PROPERTY, null, analyses.toArray());
					}
				});
			} catch (InvocationTargetException e) {
				logger.error("property change event not fired", e);
			} catch (InterruptedException e) {
				logger.error("property change event not fired", e);
			}
		}
	}

	
	public void updateAnalysesFromSourcePath() {
		if (sourcePath.exists() && sourcePath.isDirectory() && sourcePath.canRead()) {
			StopWatch stopWatch = new Slf4JStopWatch("analyses from SourcePath");
			logger.info(" - Updating Analysis from "+sourcePath.getAbsolutePath()+" for instrument "+instrumentConfiguration.getName());
			List<Analysis> newAnalyses = Arrays.asList(getDataFormat().getAnalysis(sourcePath));
			List<Analysis> invalidAnalyses = new ArrayList<>();
			for(Analysis nextA : analyses){
				if(nextA.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE)
					invalidAnalyses.add(nextA);
			}
			Collection<Analysis> oldAnalyses = CollectionUtils.subtract(analyses, invalidAnalyses);;
			Collection<Analysis> removed = CollectionUtils.subtract(oldAnalyses, newAnalyses);
			Collection<Analysis> added = CollectionUtils.subtract(newAnalyses, oldAnalyses);
			if(!invalidAnalyses.isEmpty()){
				removed.addAll(invalidAnalyses);
			}
			analyses = newAnalyses;
			stopWatch.stop();
			logger.info("Updating Analysis done : "+analyses.size()+" analyses found");
			try {
				SwingUtilities.invokeAndWait(new Runnable() {				
					public void run() {
						if(!removed.isEmpty())
							propertiesSupport.firePropertyChange(ANALYSES_REMOVED_PROPERTY, null, removed.toArray());
						if (!added.isEmpty())
							propertiesSupport.firePropertyChange(ANALYSES_ADDED_PROPERTY, null, added.toArray());
						if(added.isEmpty() && removed.isEmpty())
							propertiesSupport.firePropertyChange(ANALYSES_PROPERTY, null, analyses);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error("property change event not fired", e);
			}
    }
	}


	public Analysis[] getAnalyses() {
		return analyses.toArray(new Analysis[analyses.size()]);
	}
	
	/**
	 * Add specified PropertyChangeListener for the specified property. The
	 * property should be one of those listed in this class.
	 * 
	 * @param propertyName
	 *           The name of the property the listener wants to listen to
	 * @param listener
	 *           The PropertyChangeListener to notify when specified property
	 *           changes.
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertiesSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Add specified PropertyChangeListerner in list of listeners for all
	 * properties.
	 * 
	 * @param listener
	 *           the PropertyChangeListener to notify.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertiesSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove the specified PropertyChangeListerner from the list of listener of
	 * the specified property.
	 * 
	 * @param propertyName
	 *           the property the listener don't want to listen to any more
	 * @param listener
	 *           the listener to remove for specified property
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertiesSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove the speicified PropertyChangeListener from the listeners list for
	 * every properties.
	 * 
	 * @param listener
	 *           The listener to stop notifying
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertiesSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Returns the source directory in which Analysis are searched.
	 * 
	 * @return
	 */
	public File getSourcePath() {
		return sourcePath;
	}

	public File getDestinationRootPath() {
		return destinationRootPath;
	}

	/**
	 * Return the type of the transfer to execute. The returned type will be one
	 * of
	 * <ul>
	 * <li><code>TRANSFER_COPY_MODE</code></li>
	 * <li><code>TRANSFER_CLEAN_MODE</code></li>
	 * </ul>
	 */
	public int getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(int mode) {
		if (mode != transferMode && (mode == TRANSFER_CLEAN_MODE || mode == TRANSFER_COPY_MODE)) {
			int prevVal = transferMode;
			transferMode = mode;
			propertiesSupport.firePropertyChange(TRANSFERT_MODE_PROPERTY, prevVal, transferMode);
		}
	}
	
	/**
	 * Returns true if the Analysis files must be removed after copy.
	 * 
	 * @return true if the Analysis files must be removed after copy.
	 */
	public boolean removeFilesAfterCopy() {
		return removeFilesAfterCopy;
	}

	public void setRemoveFilesAfterCopy(boolean remove) {
		boolean prevVal = removeFilesAfterCopy;
		removeFilesAfterCopy = remove;
		propertiesSupport.firePropertyChange(REMOVE_FILES_PARAMETER, prevVal, removeFilesAfterCopy);
	}

	/**
	 * Returns the name of the instrument.
	 * 
	 * @return the name of the instrument.
	 */
	public String getInstrumentName() {
		return instrumentConfiguration.getName();
	}

	/**
	 * Returns the name of the selected configuration.
	 * 
	 * @return the name of the configuration.
	 */
	public String getConfigurationName() {
		return instrumentConfiguration.getLabel();
	}

	/**
	 * Returns the DataFormat object that must be used to retrieve analysis.
	 * 
	 * @return a DataFormat.
	 */
	public DataFormat getDataFormat() {
		return dataFormat;
	}

	/**
	 * Returns the log file.
	 * 
	 * @return
	 */
	public File getLogFile() {
		return logFile;
	}

	/**
	 * Return ePimsSystemDataProvider providing methods to access data from ePims
	 * System
	 * 
	 * @return
	 */
	public IEPSystemDataProvider getEPimsDataProvider() {
		return dataProvider;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == dataFormat) {
			propertiesSupport.firePropertyChange(DATA_FORMAT_CONFIGURATION_PARAMETER, null, dataFormat);
		}
	}


}
