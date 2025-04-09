/*
 * Created on 14 mars 2005
 *
 * $Id: BackPimsUtil.java,v 1.2 2008-02-20 07:00:50 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import fr.edyp.epims.json.*;
import fr.edyp.epims.json.AcquisitionFileDescriptorJson;
import fr.edyp.epims.json.AcquisitionFileMessageJson;
import cea.edyp.epims.transfer.task.AcquisitionServices;
import cea.edyp.epims.transfer.task.SystemServices;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.perf4j.StopWatch;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupException;
import cea.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.LoggerFactory;

/**
 * @author VDUPIERR
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class WSSystemDataProvider implements IEPSystemDataProvider {

	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
	private static final Logger logger = LoggerFactory.getLogger(WSSystemDataProvider.class);

	private String ePimsRootPath;

	/**
	 * Map used in function getDestinationDir, in order to reduce the number of
	 * WS request that slows down eP-Back, the map keep the relation
	 * sample-AcquPath
	 */
	private final Map<String, String> sampleNameToAcquPath = new HashMap<String, String>();

	public WSSystemDataProvider() throws InstantiationException {
		// Get Properties
		String wsURL = null;
		ePimsRootPath = null;

		try {
			ResourceBundle defaultBundle = new PropertyResourceBundle(new FileInputStream("./conf/eP-Back.properties"));
			wsURL = defaultBundle.getString("webservices.url");
			ePimsRootPath = defaultBundle.getString("epims.root"); //JPM.TODO
		} catch (MissingResourceException mre) {
			throw new InstantiationException(RSCS.getString("webservices.error"));
		} catch (Exception e) {
			throw new InstantiationException(RSCS.getString("webservices.error"));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cea.edyp.epims.transfer.util.IEPSystemDataProvider#getPimsRootPath()
	 */
	public String getPimsRootPath() {
		return ePimsRootPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#getPimsSystemRelativePath
	 * ()
	 */
	public String getPimsSystemRelativePath() {
		return SystemServices.getPimsSystemRelativePathTask();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#getStudyNameFor(java
	 * .lang.String)
	 */
	public String getStudyNameFor(String sampleName) {

		StudyJson studyJson = AcquisitionServices.getStudyPathJson(sampleName);
		if (studyJson != null) {
			return studyJson.getNomenclatureTitle();
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#isSampleExist(java.lang
	 * .String)
	 */
	public boolean isSampleExist(String sampleName) {
		SampleJson splDecr = AcquisitionServices.getSampleJson(sampleName);
		return (splDecr != null);
	}

	public boolean isAcquisitionExist(String acqName, String instrumentName) {
		try {
			ProtocolApplicationJson protocolApplicationJson = AcquisitionServices.getAcquisitionsDescriptors(acqName, instrumentName);

			return (protocolApplicationJson != null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#isSpectrometerDefined
	 * (java.lang.String)
	 */
	public boolean isSpectrometerDefined(String instrumentName) {
		InstrumentJson instDesc = AcquisitionServices.getInstrumentJson(instrumentName);
		return (instDesc != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cea.edyp.epims.transfer.util.IEPSystemDataProvider#
	 * createAcquisitionAndFilesFor (cea.edyp.epims.transfer.model.Analysis,
	 * java.lang.String)
	 */
	public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException {
		SampleJson splDesc = null;
		if (a.getType() == Analysis.AnalysisType.RESEARCH) {
				splDesc = AcquisitionServices.getSampleJson(a.getSample());
			if (splDesc == null) {
				throw new BackupException(RSCS.getString("sample.invalid"));
			}
		}

		// Instrument description
		InstrumentJson instrDescriptor = AcquisitionServices.getInstrumentJson(instrumentName);
		if (instrDescriptor == null) {
			instrDescriptor = new InstrumentJson();
			instrDescriptor.setName(instrumentName);
		}

		AcquisitionFileDescriptorJson acqFileDesc = createAcquisitionFileDescriptor(a);
		ProtocolApplicationJson protocolApplicationJson = acqFileDesc.getAcquisition();
		AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();
		acquisitionJson.setInstrumentId(instrDescriptor.getId());
		acquisitionJson.setInstrumentName(instrumentName);


		AcquisitionFileMessageJson acqFileMessage = new AcquisitionFileMessageJson();

		acqFileMessage.setAcquisitionFileDescriptor(acqFileDesc);
		acqFileMessage.setSampleDescriptor(splDesc);

		boolean success = AcquisitionServices.createAcquisition(acqFileMessage);
		if (!success ) {
			logger.debug("Error saving Acquisition");
			throw new BackupException("Error saving Acquisition");
		}
	}

	/**
	 * Create an AcquisitionFileDescriptor with the informations of the given
	 * Analysis. BEWARE : this AcquisitionFileDescriptor will not have an
	 * instrumentDescriptor!
	 * 
	 * @return : AcquisitionFileDescriptor
	 * @param : Analysis on which the AcquisitionFileDescriptor will be created
	 */
	private AcquisitionFileDescriptorJson createAcquisitionFileDescriptor(Analysis analysis) {
		// Acquisition and associated file descriptions
		AcquisitionFileDescriptorJson acquFileDesc = new AcquisitionFileDescriptorJson();
		ProtocolApplicationJson protocolApplicationJson = new ProtocolApplicationJson();
		AcquisitionJson acquisitionJson = new AcquisitionJson();
		protocolApplicationJson.setAcquisitionJson(acquisitionJson);

		protocolApplicationJson.setName(analysis.getName());
		switch (analysis.getType()) {
		case BLANK:
			acquisitionJson.setNature("Blanc");
			break;
		case CONTROL_INSTRUMENT:
			acquisitionJson.setNature("ControleInstrument");
			break;
		case CONTROL_LC:
			acquisitionJson.setNature("ControleLC");
			break;
		default:
			acquisitionJson.setNature("Recherche");
			break;
		}
		acquisitionJson.setDurationMin(analysis.getDuration());
		protocolApplicationJson.setActor(analysis.getOperator());
		protocolApplicationJson.setComment(analysis.getDescription());

		long size = analysis.getEstimatedSize();
		Double sizeInMo = ((double) size) / (1024 * 1024);
		acquFileDesc.setFileSize(sizeInMo);
		acquFileDesc.setFileName(analysis.getFileName());

		if (analysis.getDate() != null) {
			protocolApplicationJson.setDate(analysis.getDate());
			acquFileDesc.setDate(analysis.getDate());
		} else {
			protocolApplicationJson.setDate(new Date());
			acquFileDesc.setDate(new Date());
		}

		acquFileDesc.setAcquisition(protocolApplicationJson);

		return acquFileDesc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cea.edyp.epims.transfer.util.IEPSystemDataProvider#
	 * getAssociatedFileDestinationFile(cea.edyp.epims.transfer.model.Analysis,
	 * java.io.File, java.lang.String,
	 * cea.edyp.epims.transfer.model.BackupParameters)
	 */
	public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		File destination = null;
		Boolean dirCreationSucceed;
		if (Analysis.SPECTRA_FILETYPE.equals(fileType)) {
			// Only valid for research analysis
			if (!a.getType().equals(Analysis.AnalysisType.RESEARCH)) {
				String msg = RSCS.getString("analysis.invalid.associated.file.type");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			SampleJson splDescr = AcquisitionServices.getSampleJson(a.getSample());
			if (splDescr == null) {
				String msg = RSCS.getString("analysis.invalid.sample.description");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			StudyPathJson studyPathJson = AcquisitionServices.getStudyPathJson(a.getSample());

			try {
				File studyFullPath = new File(getPimsRootPath(), studyPathJson.getPath());

				if (studyFullPath.exists()) {
					destination = new File(studyFullPath, SystemServices.getSpectraRelativePath());

					// Destination dir not exist => it must be created
					if (!destination.exists()) {
						dirCreationSucceed = destination.mkdirs();

						// Directory creation failed => Error thrown
						if (!dirCreationSucceed) {
							String msg = RSCS.getString("cant.create.associated.file.directory");
							Object[] args = { destination.getAbsolutePath() };
							throw new BackupException(MessageFormat.format(msg, args));
						}
					}

				} else { // Study full path doesn't exists
					String msg = RSCS.getString("study.dir.notexist");
					Object[] args = { studyFullPath.getAbsolutePath() };
					throw new BackupException(MessageFormat.format(msg, args));
				}
			} catch (BackupException epce2) {
				String msg = RSCS.getString("epims.getinfo.error");
				Object[] args = { epce2.getMessage() };
				throw new BackupException(MessageFormat.format(msg, args));
			}
		}
		return destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#getDestinationFile(cea
	 * .edyp.epims.transfer.model.Analysis,
	 * cea.edyp.epims.transfer.model.BackupParameters)
	 */
	public File getDestinationDir(Analysis analysis, BackupParameters param) throws BackupException {
		
		StopWatch stopWatch = new Slf4JStopWatch("ePims getDestinationDir", analysis.getName());
		String tempAcqPath = null;
		File destination = null;

		AcquisitionFileMessageJson acqFileMsg = new AcquisitionFileMessageJson();

		InstrumentJson instrDescriptor = AcquisitionServices.getInstrumentJson(param.getInstrumentName());

		AcquisitionFileDescriptorJson acqFileDesc = createAcquisitionFileDescriptor(analysis);

		ProtocolApplicationJson protocolApplicationJson = acqFileDesc.getAcquisition();
		AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();
		acquisitionJson.setInstrumentId(instrDescriptor.getId());
		acquisitionJson.setInstrumentName(param.getInstrumentName());

		acqFileMsg.setAcquisitionFileDescriptor(acqFileDesc);

		if (analysis.getType() == Analysis.AnalysisType.RESEARCH) {
			SampleJson splDescr = new SampleJson();
			splDescr.setName(analysis.getSample());
			acqFileMsg.setSampleDescriptor(splDescr);

			// Try to get temporarily the acqPath from the research sample (if it's
			// not a research sample the path must be retrieve from the server)
			tempAcqPath = sampleNameToAcquPath.get(analysis.getSample());
		}

		if (tempAcqPath == null) {

			if (analysis.getType() == Analysis.AnalysisType.RESEARCH)
				logger.debug(analysis.getSample() + " not found in the cache");

			// So we must request threw WS to get the sample name and then add this
			// result in the hash map

			String acqPath = AcquisitionServices.getAcquisitionDestinationPath(acqFileMsg);
			if (acqPath == null) {
				String msg = RSCS.getString("epims.getinfo.error");
				//Object[] args = { epce2.getMessage() };
				throw new BackupException(MessageFormat.format(msg, ""));
			} else {
				logger.debug(" Get Destination for " + analysis.getName() + " => " + acqPath);
				destination = new File(getPimsRootPath(), acqPath);
				// add this result in the HashMap
				sampleNameToAcquPath.put(analysis.getSample(), acqPath);
				stopWatch.lap("ePims getDestinationDir.from WS");
			}

		} else {// we found the sampleName in the map so take the acqPath
					// associated
			logger.debug(" Get Destination from the hashMap " + analysis.getName() + " => " + tempAcqPath);
			destination = new File(getPimsRootPath(), tempAcqPath);
			stopWatch.lap("ePims getDestinationDir.from Cache");
		}

		analysis.setDestination(destination.getAbsolutePath());
		stopWatch.stop();
		return destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cea.edyp.epims.transfer.util.IEPSystemDataProvider#getAnalysisStatus(cea
	 * .edyp.epims.transfer.model.Analysis,
	 * cea.edyp.epims.transfer.model.BackupParameters)
	 */
	public int getAnalysisStatus(Analysis analysis, BackupParameters params) {

		logger.info("request analysis status for sample "+analysis.getName());
		StopWatch stopWatch = new Slf4JStopWatch("ePims getAnalysisStatus", analysis.getName());

		if(analysis.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE)
			return Analysis.ANALYSIS_STATUS_NOT_READABLE; // Do not change status until the acq. file is readable !

		// Test if already saved in DB
		int result = testAcquisitionExist(analysis, params.getInstrumentName());

		Analysis.AnalysisType analysisType = analysis.getType();

		SampleJson splDescriptor  = AcquisitionServices.getSampleJson(analysis.getSample());


		if (analysisType.equals(Analysis.AnalysisType.RESEARCH)) {

			if (splDescriptor == null)
				result = result | Analysis.ANALYSIS_INVALID_SAMPLE_MASK;
			else {

				StudyPathJson stdDescriptor = AcquisitionServices.getStudyPathJson(analysis.getSample());

				if (!stdDescriptor.isRunningStatus())
					result = result | Analysis.ANALYSIS_STUDY_CLOSED_MASK;
			} // END Sample valid name

		} // end analysis type = research

		analysis.setStatus(result);
		stopWatch.stop();
		return result;
	}

	private int testAcquisitionExist(Analysis analysis, String instrumentName) {
		StopWatch stopWatch = new Slf4JStopWatch("ePims testAcquisitionExists");
		int result = Analysis.ANALYSIS_OK_MASK;

		try {
			ProtocolApplicationJson protocolApplicationJson = AcquisitionServices.getAcquisitionsDescriptors(analysis.getName(), instrumentName);


			if (protocolApplicationJson != null) {
				result = Analysis.ANALYSIS_EXIST_MASK;
			} // End acquisition exist

		} catch (Exception e) {
			String msg = RSCS.getString("acq.more.than.one.exist");
			Object[] args = {analysis.getName()};
			logger.warn(MessageFormat.format(msg, args));
			logger.error(e.getMessage());
		}

		stopWatch.stop();
		return result;
	}

}
